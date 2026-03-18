package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.album.Album;
import perondi.BeShuffle.entities.DailyAlbum;
import perondi.BeShuffle.exceptions.AlbumAlreadyUsedException;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.repositories.DailyAlbumRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@Slf4j
@Service
public class DailyAlbumService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumService albumService;
    private final ObjectMapper objectMapper;

    public DailyAlbumService(DailyAlbumRepository dailyAlbumRepository, AlbumService albumService, ObjectMapper objectMapper) {
        this.dailyAlbumRepository = dailyAlbumRepository;
        this.albumService = albumService;
        this.objectMapper = objectMapper;
    }

    public DailyAlbum getTodayAlbum() {
        return dailyAlbumRepository.findByDisplayDate(LocalDate.now()).orElse(null);
    }

    public DailyAlbum setDailyAlbum(String spotifyAlbumId) {
        if (spotifyAlbumId == null || spotifyAlbumId.trim().isEmpty()) {
            throw new ValidationException("Spotify Album ID não pode ser vazio", "spotifyAlbumId");
        }

        var existing = dailyAlbumRepository.findBySpotifyAlbumId(spotifyAlbumId);
        if (existing.isPresent()) {
            throw new AlbumAlreadyUsedException(spotifyAlbumId, existing.get().getAlbumName());
        }

        Album spotifyAlbum = albumService.getAlbumById(spotifyAlbumId);
        
        if (spotifyAlbum == null) {
            throw new SpotifyApiException("API Spotify retornou dados vazios", 503);
        }

        if (spotifyAlbum.getArtists() == null || spotifyAlbum.getArtists().isEmpty()) {
            throw new SpotifyApiException("Álbum não possui informações de artista", 503);
        }

        if (spotifyAlbum.getImages() == null || spotifyAlbum.getImages().isEmpty()) {
            throw new SpotifyApiException("Álbum não possui imagem", 503);
        }

        DailyAlbum dailyAlbum = new DailyAlbum();
        dailyAlbum.setSpotifyAlbumId(spotifyAlbum.getId());
        dailyAlbum.setAlbumName(spotifyAlbum.getName());
        dailyAlbum.setArtistName(spotifyAlbum.getArtists().getFirst().getName());
        dailyAlbum.setImageUrl(spotifyAlbum.getImages().getFirst().getUrl());
        dailyAlbum.setAlbumUrl(spotifyAlbum.getUri());
        dailyAlbum.setReleaseDate(spotifyAlbum.getReleaseDate());
        dailyAlbum.setDisplayDate(LocalDate.now());

        try {
            dailyAlbum.setFullAlbumJson(objectMapper.writeValueAsString(spotifyAlbum));
        } catch (Exception e) {
            log.debug("Erro ao serializar álbum: {}", e.getMessage());
        }

        try {
            return dailyAlbumRepository.save(dailyAlbum);
        } catch (Exception e) {
            log.error("Erro ao salvar álbum no banco: {}", e.getMessage());
            throw new SpotifyApiException("Erro ao salvar álbum no banco de dados", 500, e);
        }
    }
}
