package perondi.BeShuffle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.draws.EstiloResposta;
import perondi.BeShuffle.services.Estilo;

import java.util.Arrays;
import java.util.List;

/** Estilos musicais que podem ser escolhidos no sorteio (escolher exige login; listar é público). */
@RestController
public class EstiloController {

    @GetMapping("/api/genres")
    public List<EstiloResposta> estilos() {
        return Arrays.stream(Estilo.values()).map(e -> new EstiloResposta(e.id(), e.nome())).toList();
    }
}
