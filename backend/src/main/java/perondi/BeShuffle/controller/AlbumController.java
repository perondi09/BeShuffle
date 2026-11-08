package perondi.BeShuffle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.services.AlbumPicker;
import perondi.BeShuffle.services.AlbumService;
import perondi.BeShuffle.services.DailyAlbumService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final AlbumPicker albumPicker;
    private final DailyAlbumService dailyAlbumService;

    @GetMapping("/random")
    public ResponseEntity<Album> getRandomAlbum() {
        String randomAlbumId = albumPicker.pickAlbumId();
        if (randomAlbumId == null) {
            throw new SpotifyApiException("Não foi possível obter um álbum aleatório do Spotify", 503);
        }

        Album album = albumService.getAlbumById(randomAlbumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/daily")
    public ResponseEntity<Album> getDailyAlbum() {
        Album album = dailyAlbumService.getAlbumDoDia();
        return ResponseEntity.ok(album);
    }
}
