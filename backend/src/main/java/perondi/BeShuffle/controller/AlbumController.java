package perondi.BeShuffle.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.services.AcessoService;
import perondi.BeShuffle.services.DailyAlbumService;

/**
 * Álbum do Dia, igual para todo mundo. Os sorteios com limite diário ficam no DrawController.
 * Toda abertura da home passa por aqui, então é aqui que o acesso do dia é contado (AcessoService).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final DailyAlbumService dailyAlbumService;
    private final AcessoService acessoService;

    @GetMapping("/daily")
    public ResponseEntity<Album> getDailyAlbum(@AuthenticationPrincipal UsuarioLogado usuario,
                                               HttpServletRequest request) {
        acessoService.registrar(request, usuario, AcessoService.MUSICA);
        Album album = dailyAlbumService.getAlbumDoDia();
        return ResponseEntity.ok(album);
    }
}
