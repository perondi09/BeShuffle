package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.draws.SaldoDeFichas;
import perondi.BeShuffle.exceptions.LimiteDeSorteiosException;
import perondi.BeShuffle.repository.DrawQuotaRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Sorteios do dia: quantos restam, gastar um e devolver. O "dia" segue o fuso de beshuffle.timezone.
 * Visitante gasta duas cotas juntas: a do cookie (1 por dia) e a da rede (Plano.VISITANTES_POR_REDE por dia),
 * para quem apaga os cookies não sortear sem limite.
 */
@Service
@RequiredArgsConstructor
public class DrawAllowanceService {

    private final DrawQuotaRepository quotaRepository;
    private final Clock clock;

    public SaldoDeFichas consultar(Dono dono) {
        LocalDate hoje = LocalDate.now(clock);
        int limite = dono.plano().sorteiosPorDia();
        int restantes = limite - quotaRepository.usados(dono.chave(), hoje);
        String rede = dono.chaveDaRede();
        if (rede != null) {
            restantes = Math.min(restantes, Plano.VISITANTES_POR_REDE - quotaRepository.usados(rede, hoje));
        }
        return new SaldoDeFichas(dono.plano(), Math.max(0, restantes), limite, proximaMeiaNoite(hoje));
    }

    /** Gasta um sorteio do dia ou lança LimiteDeSorteiosException (429). Devolve o dia em que gastou. */
    public LocalDate gastar(Dono dono) {
        LocalDate hoje = LocalDate.now(clock);
        if (!quotaRepository.tentarGastar(dono.chave(), hoje, dono.plano().sorteiosPorDia())) {
            throw new LimiteDeSorteiosException(dono.plano());
        }
        String rede = dono.chaveDaRede();
        if (rede != null && !quotaRepository.tentarGastar(rede, hoje, Plano.VISITANTES_POR_REDE)) {
            // A rede já passou do teto: devolve a ficha do cookie, que não chegou a ser usada.
            quotaRepository.devolver(dono.chave(), hoje);
            throw LimiteDeSorteiosException.daRede();
        }
        return hoje;
    }

    /** Devolve o sorteio gasto no dia informado (o que gastar() devolveu, mesmo se já passou da meia-noite). */
    public void devolver(Dono dono, LocalDate dia) {
        quotaRepository.devolver(dono.chave(), dia);
        String rede = dono.chaveDaRede();
        if (rede != null) {
            quotaRepository.devolver(rede, dia);
        }
    }

    private Instant proximaMeiaNoite(LocalDate hoje) {
        return hoje.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
    }
}
