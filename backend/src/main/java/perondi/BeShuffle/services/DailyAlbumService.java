package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.repository.DailyAlbumRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAlbumService {

    private static final int MAX_TENTATIVAS_ALBUM_INEDITO = 3;

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPicker albumPicker;
    private final AlbumService albumService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    /** Data de hoje no fuso da aplicação (beshuffle.timezone), não no fuso do servidor. */
    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public Album getAlbumDoDia() {
        LocalDate hoje = hoje();
        return dailyAlbumRepository.findByDisplayDate(hoje)
                .map(this::converterJsonParaAlbum)
                .orElseGet(() -> {
                    log.warn("Álbum do dia não encontrado para {}. Gerando como fallback...", hoje);
                    return gerarESalvarAlbumDoDia(hoje);
                });
    }

    /** Registro do Álbum do Dia de hoje no banco (gera e salva se ainda não existe). Usado pelas notas. */
    public DailyAlbum registroDeHoje() {
        LocalDate hoje = hoje();
        return dailyAlbumRepository.findByDisplayDate(hoje).orElseGet(() -> {
            gerarESalvarAlbumDoDia(hoje);
            return dailyAlbumRepository.findByDisplayDate(hoje)
                    .orElseThrow(() -> new IllegalStateException("Álbum do dia não foi salvo"));
        });
    }

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

        String randomAlbumId = sortearAlbumInedito();
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
        } catch (JacksonException e) {
            log.error("Erro ao serializar o álbum do dia {} em JSON", spotifyAlbum.getId(), e);
            throw new IllegalStateException("Falha ao serializar o álbum do dia", e);
        }

        DailyAlbum savedAlbum;
        try {
            savedAlbum = dailyAlbumRepository.save(dailyAlbum);
        } catch (DataIntegrityViolationException e) {
            // Se outra execução (agendador ou outra requisição) salvou o álbum deste dia primeiro, usa o dela.
            // Qualquer outra violação (álbum repetido salvo em paralelo, campo obrigatório nulo) é repassada.
            var salvoPorOutraExecucao = dailyAlbumRepository.findByDisplayDate(data);
            if (salvoPorOutraExecucao.isEmpty()) {
                throw e;
            }
            log.warn("Álbum do dia de {} já foi salvo por outra execução; usando o registro existente", data);
            return converterJsonParaAlbum(salvoPorOutraExecucao.get());
        }

        log.info("Álbum do dia salvo com sucesso para {}: {} ({})", data, savedAlbum.getAlbumName(), savedAlbum.getSpotifyAlbumId());
        return spotifyAlbum;
    }

    /** Sorteia um álbum que ainda não foi álbum do dia (a coluna spotify_album_id é única). */
    private String sortearAlbumInedito() {
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS_ALBUM_INEDITO; tentativa++) {
            String albumId = albumPicker.pickAlbumId();
            if (albumId == null || albumId.isBlank()) {
                throw new IllegalStateException("Não foi possível gerar um álbum do dia no momento");
            }
            if (!dailyAlbumRepository.existsBySpotifyAlbumId(albumId)) {
                return albumId;
            }
            log.info("Álbum {} já foi álbum do dia; sorteando outro (tentativa {} de {})",
                    albumId, tentativa, MAX_TENTATIVAS_ALBUM_INEDITO);
        }
        throw new IllegalStateException("Todos os álbuns sorteados já foram álbum do dia; tente novamente");
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
        } catch (JacksonException e) {
            log.error("Erro ao ler JSON do álbum do dia do banco", e);
            throw new IllegalStateException("Erro ao carregar álbum do dia", e);
        }
    }
}
