package perondi.BeShuffle.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import perondi.BeShuffle.services.DailyAlbumService;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAlbumScheduler {

    private final DailyAlbumService dailyAlbumService;

    @Scheduled(cron = "0 0 0 * * *")
    public void gerarAlbumDiarioAutomaticamente() {
        log.info("⏰ [SCHEDULER] Iniciando geração automática do Álbum do Dia...");
        try {
            dailyAlbumService.gerarESalvarAlbumDoDia(LocalDate.now());
            log.info("✅ [SCHEDULER] Álbum do dia gerado e salvo com sucesso!");
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Falha ao gerar o álbum do dia: ", e);
        }
    }
}