package perondi.BeShuffle.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import perondi.BeShuffle.services.DailyAlbumService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAlbumScheduler {

    private final DailyAlbumService dailyAlbumService;

    @Scheduled(cron = "0 0 0 * * *", zone = "${beshuffle.timezone:America/Sao_Paulo}")
    public void gerarAlbumDiarioAutomaticamente() {
        log.info("⏰ [SCHEDULER] Iniciando geração automática do Álbum do Dia...");
        try {
            dailyAlbumService.gerarESalvarAlbumDoDia(dailyAlbumService.hoje());
            log.info("✅ [SCHEDULER] Álbum do dia gerado e salvo com sucesso!");
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Falha ao gerar o álbum do dia: ", e);
        }
    }
}