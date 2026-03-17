package perondi.BeShuffle.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@Slf4j
@Component
public class DailyAlbumScheduler {

    private final DailyAlbumService dailyAlbumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;

    public DailyAlbumScheduler(
            DailyAlbumService dailyAlbumService,
            SpotifyRandomAlbumService spotifyRandomAlbumService
    ) {
        this.dailyAlbumService = dailyAlbumService;
        this.spotifyRandomAlbumService = spotifyRandomAlbumService;
    }

    /**
     * Executa todo dia à meia-noite (00:00)
     * Busca um álbum aleatório DIRETO da API do Spotify
     * Cron: 0 0 0 * * * = 00:00 todo dia
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void setRandomDailyAlbum() {
        try {
            log.info("🎵 ========== Iniciando busca de álbum aleatório ==========");

            // Buscar um ID aleatório DA API DO SPOTIFY
            String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();

            if (randomAlbumId == null) {
                log.error("❌ Não foi possível buscar álbum aleatório da API");
                return;
            }

            log.info("🎲 Álbum ID obtido: {}", randomAlbumId);

            // Tentar definir como álbum do dia
            try {
                dailyAlbumService.setDailyAlbum(randomAlbumId);
                log.info("✅ Álbum de hoje definido com sucesso!");
                log.info("🎵 ========== Fim do processo ==========");
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Álbum já foi usado, tentando outro...");
                retryWithDifferentAlbum();
            }

        } catch (Exception e) {
            log.error("❌ Erro ao executar scheduler", e);
        }
    }

    /**
     * Tenta novamente com outro álbum aleatório
     */
    private void retryWithDifferentAlbum() {
        try {
            log.info("🔄 Segunda tentativa com outro álbum...");

            String differentAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();

            if (differentAlbumId == null) {
                log.error("❌ Não foi possível obter álbum na segunda tentativa");
                return;
            }

            dailyAlbumService.setDailyAlbum(differentAlbumId);
            log.info("✅ Álbum de hoje definido com sucesso na segunda tentativa!");

        } catch (Exception e) {
            log.error("❌ Erro na segunda tentativa", e);
        }
    }
}
