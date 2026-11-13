package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.draws.SaldoDeFichas;
import perondi.BeShuffle.exceptions.LimiteDeSorteiosException;
import perondi.BeShuffle.repository.DrawQuotaRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/** Sorteios do dia: quantos restam, gastar um e devolver. O "dia" segue o fuso de beshuffle.timezone. */
@Service
@RequiredArgsConstructor
public class DrawAllowanceService {

    private final DrawQuotaRepository quotaRepository;
    private final Clock clock;

    public SaldoDeFichas consultar(Dono dono) {
        LocalDate hoje = LocalDate.now(clock);
        int limite = dono.plano().sorteiosPorDia();
        int restantes = Math.max(0, limite - quotaRepository.usados(dono.chave(), hoje));
        return new SaldoDeFichas(dono.plano(), restantes, limite, proximaMeiaNoite(hoje));
    }

    /** Gasta um sorteio do dia ou lança LimiteDeSorteiosException (429). Devolve o dia em que gastou. */
    public LocalDate gastar(Dono dono) {
        LocalDate hoje = LocalDate.now(clock);
        if (!quotaRepository.tentarGastar(dono.chave(), hoje, dono.plano().sorteiosPorDia())) {
            throw new LimiteDeSorteiosException(dono.plano());
        }
        return hoje;
    }

    /** Devolve o sorteio gasto no dia informado (o que gastar() devolveu, mesmo se já passou da meia-noite). */
    public void devolver(Dono dono, LocalDate dia) {
        quotaRepository.devolver(dono.chave(), dia);
    }

    private Instant proximaMeiaNoite(LocalDate hoje) {
        return hoje.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
    }
}
