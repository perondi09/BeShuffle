package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AlbumImage;
import perondi.BeShuffle.entity.Artist;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.spotify.SpotifyClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Testes unitários do serviço de álbum")
class AlbumServiceTest {

    private SpotifyClient spotifyClient;
    private AlbumService albumService;

    @BeforeEach
    void setUp() {
        spotifyClient = mock(SpotifyClient.class);
        albumService = new AlbumService(spotifyClient);
    }

    @Test
    @DisplayName("Deve rejeitar albumId vazio ou nulo")
    void shouldRejectEmptyAlbumId() {
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(""));
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(null));
    }

    @Test
    @DisplayName("Deve buscar o álbum pelo id sem espaços")
    void shouldReturnAlbum() {
        when(spotifyClient.getAlbum("123")).thenReturn(createAlbum());

        Album result = albumService.getAlbumById(" 123 ");

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals("123", result.getId());
    }

    @Test
    @DisplayName("Deve repassar falha de autenticação do Spotify")
    void shouldPropagateAuthenticationFailure() {
        when(spotifyClient.getAlbum("123")).thenThrow(new SpotifyAuthenticationException("Token de acesso vazio", 401));

        assertThrows(SpotifyAuthenticationException.class, () -> albumService.getAlbumById("123"));
    }

    @Test
    @DisplayName("Deve lançar 404 quando a API do Spotify retornar álbum nulo")
    void shouldFailWhenSpotifyReturnsNullAlbum() {
        when(spotifyClient.getAlbum("123")).thenReturn(null);

        SpotifyApiException ex = assertThrows(SpotifyApiException.class, () -> albumService.getAlbumById("123"));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Deve converter erro inesperado em 503")
    void shouldWrapUnexpectedErrors() {
        when(spotifyClient.getAlbum("123")).thenThrow(new IllegalStateException("timeout"));

        SpotifyApiException ex = assertThrows(SpotifyApiException.class, () -> albumService.getAlbumById("123"));
        assertEquals(503, ex.getHttpStatus());
    }

    private Album createAlbum() {
        Album album = new Album();
        album.setId("123");
        album.setName("Test");
        album.setReleaseDate("2020-01-01");
        album.setUri("spotify:album:123");

        Artist artist = new Artist();
        artist.setName("Artist");
        album.setArtists(List.of(artist));

        AlbumImage image = new AlbumImage();
        image.setUrl("https://example.com/img.jpg");
        album.setImages(List.of(image));

        return album;
    }
}
