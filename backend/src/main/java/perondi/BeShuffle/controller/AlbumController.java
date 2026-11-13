package perondi.BeShuffle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.services.DailyAlbumService;

/** Álbum do Dia, igual para todo mundo. Os sorteios com limite diário ficam no DrawController. */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final DailyAlbumService dailyAlbumService;

    @GetMapping("/daily")
    public ResponseEntity<Album> getDailyAlbum() {
        Album album = dailyAlbumService.getAlbumDoDia();
        return ResponseEntity.ok(album);
    }
}
