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
import java.util.Optional;

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
        Optional<DailyAlbum> albumSalvo = dailyAlbumRepository.findByDisplayDate(hoje);

        if (albumSalvo.isPresent()) {
            return converterJsonParaAlbum(albumSalvo.get().getFullAlbumJson());
        }
        log.warn("Álbum do dia não encontrado no banco. Gerando agora como fallback...");
        return gerarESalvarAlbumDoDia(hoje);
    }

    @Transactional
    public Album gerarESalvarAlbumDoDia(LocalDate data) {
        Optional<DailyAlbum> existente = dailyAlbumRepository.findByDisplayDate(data);
        if (existente.isPresent()) {
            return converterJsonParaAlbum(existente.get().getFullAlbumJson());
        }

        log.info("Gerando novo álbum do dia para a data: {}", data);
        String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
        Album spotifyAlbum = albumService.getAlbumById(randomAlbumId);

        DailyAlbum dailyAlbum = new DailyAlbum();
        dailyAlbum.setSpotifyAlbumId(spotifyAlbum.getId());
        dailyAlbum.setAlbumName(spotifyAlbum.getName());
        dailyAlbum.setDisplayDate(data);
        dailyAlbum.setAlbumUrl(spotifyAlbum.getUri());

        if (spotifyAlbum.getArtists() != null && !spotifyAlbum.getArtists().isEmpty()) {
            dailyAlbum.setArtistName(spotifyAlbum.getArtists().get(0).getName());
        }
        if (spotifyAlbum.getImages() != null && !spotifyAlbum.getImages().isEmpty()) {
            dailyAlbum.setImageUrl(spotifyAlbum.getImages().get(0).getUrl());
        }

        try {
            dailyAlbum.setFullAlbumJson(objectMapper.writeValueAsString(spotifyAlbum));
        } catch (JsonProcessingException e) {
            log.error("Erro ao transformar álbum em JSON", e);
        }

        dailyAlbumRepository.save(dailyAlbum);
        log.info("Álbum do dia salvo com sucesso no banco de dados!");
        return spotifyAlbum;
    }

    private Album converterJsonParaAlbum(String json) {
        try {
            return objectMapper.readValue(json, Album.class);
        } catch (JsonProcessingException e) {
            log.error("Erro ao ler JSON do banco", e);
            throw new RuntimeException("Erro ao carregar álbum do dia");
        }
    }
}