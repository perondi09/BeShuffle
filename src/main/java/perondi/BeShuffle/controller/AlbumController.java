package perondi.BeShuffle.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.services.AlbumService;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;
    private final DailyAlbumService dailyAlbumService;

        @GetMapping("/random")
    public ResponseEntity<Album> getRandomAlbum() {
        String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
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