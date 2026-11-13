package perondi.BeShuffle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.dtos.reviews.NotaRequest;
import perondi.BeShuffle.dtos.reviews.NotasDoDia;
import perondi.BeShuffle.services.ReviewService;

/** Notas e comentários do Álbum do Dia. Só para conta logada (ver SecurityConfig). */
@RestController
@RequestMapping("/api/reviews/today")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public NotasDoDia notas(@AuthenticationPrincipal UsuarioLogado usuario) {
        return reviewService.notasDeHoje(usuario.id());
    }

    /** Cria ou edita a nota de quem está logado. */
    @PutMapping
    public NotasDoDia avaliar(@AuthenticationPrincipal UsuarioLogado usuario, @RequestBody NotaRequest body) {
        return reviewService.avaliarHoje(usuario.id(), body);
    }
}
