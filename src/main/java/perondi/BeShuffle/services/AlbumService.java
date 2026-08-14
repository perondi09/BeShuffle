package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.client.AlbumSpotifyClient;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

@Slf4j
@Service
public class AlbumService {

    private final AlbumSpotifyClient albumSpotifyClient;
    private final AuthService authService;

    public AlbumService(AlbumSpotifyClient albumSpotifyClient, AuthService authService) {
        this.albumSpotifyClient = albumSpotifyClient;
        this.authService = authService;
    }

    public Album getAlbumById(String albumId) {
        String normalizedAlbumId = normalizeAlbumId(albumId);
        if (normalizedAlbumId.isBlank()) {
            log.warn("Tentativa de busca por álbum com id vazio");
            throw new ValidationException("Album ID não pode ser vazio", "albumId");
        }

        String token;
        try {
            token = authService.getAccessToken();
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter token de autenticação para o álbum {}", normalizedAlbumId, e);
            throw new SpotifyAuthenticationException("Falha ao obter token de autenticação", e);
        }

        if (token == null || token.isBlank()) {
            log.warn("Token de autenticação inválido para o álbum {}", normalizedAlbumId);
            throw new SpotifyAuthenticationException("Token de autenticação inválido", 401);
        }

        try {
            Album album = albumSpotifyClient.getAlbum("Bearer " + token, normalizedAlbumId);
            if (album == null) {
                log.warn("Álbum não encontrado no Spotify para o id {}", normalizedAlbumId);
                throw new SpotifyApiException("Álbum não encontrado: " + normalizedAlbumId, 404);
            }

            log.debug("Álbum encontrado com sucesso: {} ({})", album.getName(), album.getId());
            return album;
        } catch (SpotifyApiException | SpotifyAuthenticationException e) {
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