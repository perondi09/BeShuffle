package perondi.BeShuffle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.entities.DailyAlbum;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.exceptions.AlbumAlreadyUsedException;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final DailyAlbumService dailyAlbumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;

    public AlbumController(DailyAlbumService dailyAlbumService, SpotifyRandomAlbumService spotifyRandomAlbumService) {
        this.dailyAlbumService = dailyAlbumService;
        this.spotifyRandomAlbumService = spotifyRandomAlbumService;
    }

    @GetMapping("/today")
    public ResponseEntity<DailyAlbum> getTodayAlbum() {
        DailyAlbum album = dailyAlbumService.getTodayAlbum();
        return album != null ? ResponseEntity.ok(album) : ResponseEntity.noContent().build();
    }

    @PostMapping("/set-daily")
    public ResponseEntity<DailyAlbum> setDailyAlbum(@RequestParam("id") String spotifyAlbumId) {
        if (spotifyAlbumId == null || spotifyAlbumId.trim().isEmpty()) {
            throw new ValidationException("Album ID não pode ser vazio", "id");
        }
        return ResponseEntity.ok(dailyAlbumService.setDailyAlbum(spotifyAlbumId));
    }

    @PostMapping("/random")
    public ResponseEntity<DailyAlbum> setRandomDailyAlbum() {
        String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
        if (randomAlbumId == null) {
            throw new SpotifyApiException("Não foi possível obter um álbum aleatório do Spotify", 503);
        }

        try {
            return ResponseEntity.ok(dailyAlbumService.setDailyAlbum(randomAlbumId));
        } catch (AlbumAlreadyUsedException e) {
            String differentAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
            if (differentAlbumId == null) {
                throw new SpotifyApiException("Não foi possível obter um álbum alternativo do Spotify", 503);
            }
            return ResponseEntity.ok(dailyAlbumService.setDailyAlbum(differentAlbumId));
        }
    }
}