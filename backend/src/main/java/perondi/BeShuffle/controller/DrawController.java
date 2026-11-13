package perondi.BeShuffle.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.dtos.draws.AlbumSorteado;
import perondi.BeShuffle.dtos.draws.SaldoDeFichas;
import perondi.BeShuffle.dtos.draws.SorteioFeito;
import perondi.BeShuffle.dtos.draws.SorteioRequest;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.services.Dono;
import perondi.BeShuffle.services.DrawAllowanceService;
import perondi.BeShuffle.services.DrawService;
import perondi.BeShuffle.services.Estilo;

import java.util.List;
import java.util.UUID;

/**
 * Sorteios com limite diário no servidor: visitante 1 por dia (cookie bs_anon), conta logada 10 por dia
 * e com estilos musicais. O Álbum do Dia (GET /api/albums/daily) é à parte e não gasta sorteio.
 */
@RestController
@RequestMapping("/api/draws")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;
    private final DrawAllowanceService allowanceService;
    private final VisitanteCookie visitanteCookie;

    @PostMapping
    public SorteioFeito sortear(@RequestBody(required = false) SorteioRequest body,
                                @AuthenticationPrincipal UsuarioLogado usuario,
                                HttpServletRequest request, HttpServletResponse response) {
        List<Estilo> estilos = estilos(body);
        Dono dono = usuario != null
                ? Dono.usuario(usuario.id())
                : Dono.visitante(visitanteCookie.lerOuCriar(request, response));
        return drawService.sortear(dono, estilos);
    }

    /** Quantos sorteios restam hoje. Não cria cookie: visitante novo ainda tem o sorteio do dia. */
    @GetMapping("/allowance")
    public SaldoDeFichas saldo(@AuthenticationPrincipal UsuarioLogado usuario, HttpServletRequest request) {
        Dono dono = usuario != null
                ? Dono.usuario(usuario.id())
                : Dono.visitante(visitanteCookie.ler(request).orElseGet(() -> UUID.randomUUID().toString()));
        return allowanceService.consultar(dono);
    }

    /** Sorteados de hoje por toda a comunidade (só para conta logada, ver SecurityConfig). */
    @GetMapping("/today")
    public List<AlbumSorteado> sorteadosDeHoje() {
        return drawService.sorteadosDeHoje();
    }

    private static List<Estilo> estilos(SorteioRequest body) {
        if (body == null || body.genres() == null) {
            return List.of();
        }
        return body.genres().stream().distinct()
                .map(id -> Estilo.porId(id)
                        .orElseThrow(() -> new ValidationException("estilo desconhecido: " + id, "genres")))
                .toList();
    }
}
