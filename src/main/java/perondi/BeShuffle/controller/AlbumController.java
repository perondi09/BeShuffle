
package perondi.BeShuffle.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import perondi.BeShuffle.dtos.album.Album;
import perondi.BeShuffle.client.AlbumSpotifyClient;
import perondi.BeShuffle.client.AuthSpotifyClient;
import perondi.BeShuffle.dtos.login.LoginRequest;
import perondi.BeShuffle.entities.DailyAlbum;
import perondi.BeShuffle.services.DailyAlbumService;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final DailyAlbumService dailyAlbumService;

    public AlbumController(DailyAlbumService dailyAlbumService) {
        this.dailyAlbumService = dailyAlbumService;
    }

    /**
     * GET /api/albums/today
     * Retorna o álbum de hoje (para exibir na homepage)
     */
    @GetMapping("/today")
    public ResponseEntity<DailyAlbum> getTodayAlbum() {
        DailyAlbum album = dailyAlbumService.getTodayAlbum();

        if (album == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(album);
    }

    /**
     * POST /api/albums/set-daily?id=SPOTIFY_ALBUM_ID
     * Define um novo álbum para o dia de hoje (admin endpoint)
     */
    @PostMapping("/set-daily")
    public ResponseEntity<DailyAlbum> setDailyAlbum(@RequestParam("id") String spotifyAlbumId) {
        try {
            DailyAlbum dailyAlbum = dailyAlbumService.setDailyAlbum(spotifyAlbumId);
            return ResponseEntity.ok(dailyAlbum);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
