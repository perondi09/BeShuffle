package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.spotify.SpotifyClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final SpotifyClient spotifyClient;

    public Album getAlbumById(String albumId) {
        String normalizedAlbumId = normalizeAlbumId(albumId);
        if (normalizedAlbumId.isBlank()) {
            log.warn("Tentativa de busca por álbum com id vazio");
            throw new ValidationException("Album ID não pode ser vazio", "albumId");
        }

        try {
            Album album = spotifyClient.getAlbum(normalizedAlbumId);
            if (album == null) {
                log.warn("Álbum não encontrado no Spotify para o id {}", normalizedAlbumId);
                throw new SpotifyApiException("Álbum não encontrado: " + normalizedAlbumId, 404);
            }

            log.debug("Álbum encontrado com sucesso: {} ({})", album.getName(), album.getId());
            return album;
        } catch (SpotifyApiException | SpotifyAuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar álbum {} no Spotify", normalizedAlbumId, e);
            throw new SpotifyApiException("Erro ao buscar álbum: " + e.getMessage(), 503, e);
        }
    }

    private String normalizeAlbumId(String albumId) {
        return albumId == null ? "" : albumId.trim();
    }
}
