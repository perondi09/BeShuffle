package perondi.BeShuffle.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import perondi.BeShuffle.exceptions.AlbumAlreadyUsedException;
import perondi.BeShuffle.exceptions.AlbumException;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@Slf4j
@Component
public class DailyAlbumScheduler {

    private static final int MAX_ATTEMPTS = 5;
    private static final int INITIAL_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 10000;

    private final DailyAlbumService dailyAlbumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;

    public DailyAlbumScheduler(DailyAlbumService dailyAlbumService, SpotifyRandomAlbumService spotifyRandomAlbumService) {
        this.dailyAlbumService = dailyAlbumService;
        this.spotifyRandomAlbumService = spotifyRandomAlbumService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void setRandomDailyAlbum() {
        boolean success = executeWithRetry(1);
        if (!success) {
            log.error("Falha ao definir álbum após {} tentativas", MAX_ATTEMPTS);
        }
    }

    private boolean executeWithRetry(int attemptNumber) {
        try {
            String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
            if (randomAlbumId == null) {
                return retryIfPossible(attemptNumber);
            }

            try {
                dailyAlbumService.setDailyAlbum(randomAlbumId);
                return true;
            } catch (AlbumException e) {
                return retryIfPossible(attemptNumber);
            }
        } catch (Exception e) {
            log.error("Erro na tentativa {}: {}", attemptNumber, e.getMessage());
            return retryIfPossible(attemptNumber);
        }
    }

    private boolean retryIfPossible(int attemptNumber) {
        if (attemptNumber >= MAX_ATTEMPTS) {
            return false;
        }

        int delayMs = Math.min(INITIAL_DELAY_MS * (int) Math.pow(2, attemptNumber - 1), MAX_DELAY_MS);
        try {
            Thread.sleep(delayMs);
            return executeWithRetry(attemptNumber + 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
