package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.client.AlbumSpotifyClient;
import perondi.BeShuffle.dtos.album.Album;
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
        if (albumId == null || albumId.trim().isEmpty()) {
            throw new ValidationException("Album ID não pode ser vazio", "albumId");
        }

        String token;
        try {
            token = authService.getAccessToken();
        } catch (Exception e) {
            log.error("Erro ao obter token", e);
            throw new SpotifyAuthenticationException("Falha ao obter token de autenticação", e);
        }

        if (token == null || token.isEmpty()) {
            throw new SpotifyAuthenticationException("Token de autenticação inválido", 401);
        }

        try {
            Album album = albumSpotifyClient.getAlbum("Bearer " + token, albumId);
            if (album == null) {
                throw new SpotifyApiException("Álbum não encontrado: " + albumId, 404);
            }
            return album;
        } catch (SpotifyApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar álbum: {}", albumId, e);
            throw new SpotifyApiException("Erro ao buscar álbum: " + e.getMessage(), 503, e);
        }
    }
}