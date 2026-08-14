package perondi.BeShuffle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.repository.DailyAlbumRepository;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAlbumService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;
    private final AlbumService albumService;
    private final ObjectMapper objectMapper;

    public Album getAlbumDoDia() {
        LocalDate hoje = LocalDate.now();
        return dailyAlbumRepository.findByDisplayDate(hoje)
                .map(this::converterJsonParaAlbum)
                .orElseGet(() -> {
                    log.warn("Álbum do dia não encontrado para {}. Gerando como fallback...", hoje);
                    return gerarESalvarAlbumDoDia(hoje);
                });
    }

    @Transactional
    public Album gerarESalvarAlbumDoDia(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data do álbum do dia não pode ser nula");
        }

        return dailyAlbumRepository.findByDisplayDate(data)
                .map(this::converterJsonParaAlbum)
                .orElseGet(() -> persistirNovoAlbumDoDia(data));
    }

    private Album persistirNovoAlbumDoDia(LocalDate data) {
        log.info("Gerando novo álbum do dia para a data: {}", data);

        String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
        if (randomAlbumId == null || randomAlbumId.isBlank()) {
            throw new IllegalStateException("Não foi possível gerar um álbum do dia no momento");
        }

        Album spotifyAlbum = albumService.getAlbumById(randomAlbumId);
        DailyAlbum dailyAlbum = new DailyAlbum();
        dailyAlbum.setSpotifyAlbumId(spotifyAlbum.getId());
        dailyAlbum.setAlbumName(spotifyAlbum.getName());
        dailyAlbum.setDisplayDate(data);
        dailyAlbum.setAlbumUrl(spotifyAlbum.getUri());
        dailyAlbum.setReleaseDate(spotifyAlbum.getReleaseDate());

        if (spotifyAlbum.getArtists() != null && !spotifyAlbum.getArtists().isEmpty()) {
            dailyAlbum.setArtistName(spotifyAlbum.getArtists().getFirst().getName());
        }
        if (spotifyAlbum.getImages() != null && !spotifyAlbum.getImages().isEmpty()) {
            dailyAlbum.setImageUrl(spotifyAlbum.getImages().getFirst().getUrl());
        }

        try {
            dailyAlbum.setFullAlbumJson(objectMapper.writeValueAsString(spotifyAlbum));
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar o álbum do dia {} em JSON", spotifyAlbum.getId(), e);
            throw new IllegalStateException("Falha ao serializar o álbum do dia", e);
        }

        DailyAlbum savedAlbum = dailyAlbumRepository.save(dailyAlbum);
        log.info("Álbum do dia salvo com sucesso para {}: {} ({})", data, savedAlbum.getAlbumName(), savedAlbum.getSpotifyAlbumId());
        return spotifyAlbum;
    }

    private Album converterJsonParaAlbum(DailyAlbum dailyAlbum) {
        if (dailyAlbum == null || dailyAlbum.getFullAlbumJson() == null || dailyAlbum.getFullAlbumJson().isBlank()) {
            log.warn("JSON do álbum do dia está ausente para o registro {}", dailyAlbum == null ? "nulo" : dailyAlbum.getId());
            throw new IllegalStateException("JSON do álbum do dia está ausente");
        }

        return converterJsonParaAlbum(dailyAlbum.getFullAlbumJson());
    }

    private Album converterJsonParaAlbum(String json) {
        try {
            return objectMapper.readValue(json, Album.class);
        } catch (JsonProcessingException e) {
            log.error("Erro ao ler JSON do álbum do dia do banco", e);
            throw new IllegalStateException("Erro ao carregar álbum do dia", e);
        }
    }
}