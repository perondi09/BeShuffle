package perondi.BeShuffle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.entities.DailyAlbum;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final DailyAlbumService dailyAlbumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;

    public AlbumController(
            DailyAlbumService dailyAlbumService,
            SpotifyRandomAlbumService spotifyRandomAlbumService
    ) {
        this.dailyAlbumService = dailyAlbumService;
        this.spotifyRandomAlbumService = spotifyRandomAlbumService;
    }

    /**
     * GET /api/albums/today
     * Retorna o álbum definido para hoje
     * Status 200 - Álbum encontrado
     * Status 204 - Nenhum álbum definido
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
     * POST /api/albums/set-daily
     * Define um álbum específico para hoje
     * Parâmetro: id (Spotify Album ID)
     * Status 200 - Álbum definido com sucesso
     * Status 400 - Álbum já foi usado antes
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

    /**
     * POST /api/albums/random
     * Define um álbum ALEATÓRIO da API do Spotify para hoje
     * NÃO precisa de nenhum parâmetro
     * O álbum é totalmente aleatório, buscado direto do Spotify
     *
     * Status 200 - Álbum aleatório definido com sucesso
     * Status 400 - Álbum já foi usado (tenta outro automaticamente)
     * Status 500 - Erro ao buscar da API do Spotify
     */
    @PostMapping("/random")
    public ResponseEntity<DailyAlbum> setRandomDailyAlbum() {
        try {
            // Buscar álbum ALEATÓRIO direto da API do Spotify
            String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();

            if (randomAlbumId == null) {
                return ResponseEntity.internalServerError().build();
            }

            // Tentar definir como álbum do dia
            DailyAlbum dailyAlbum = dailyAlbumService.setDailyAlbum(randomAlbumId);

            return ResponseEntity.ok(dailyAlbum);

        } catch (IllegalArgumentException e) {
            // Se o álbum já foi usado, tenta outro
            try {
                String differentAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();

                if (differentAlbumId == null) {
                    return ResponseEntity.internalServerError().build();
                }

                DailyAlbum dailyAlbum = dailyAlbumService.setDailyAlbum(differentAlbumId);
                return ResponseEntity.ok(dailyAlbum);

            } catch (Exception ex) {
                return ResponseEntity.badRequest().build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}