package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import perondi.BeShuffle.client.AlbumSpotifyClient;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AlbumImage;
import perondi.BeShuffle.entity.Artist;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Testes unitários do serviço de álbum")
class ServiceUnitTests {

    @Mock
    private AlbumSpotifyClient albumSpotifyClient;

    @Mock
    private AuthService authService;

    private AlbumService albumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        albumService = new AlbumService(albumSpotifyClient, authService);
    }

    @Test
    @DisplayName("Deve rejeitar albumId vazio ou nulo")
    void shouldRejectEmptyAlbumId() {
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(""));
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(null));
    }

    @Test
    @DisplayName("Deve buscar álbum com token válido")
    void shouldReturnAlbumWhenTokenIsValid() {
        when(authService.getAccessToken()).thenReturn("token");
        Album album = createAlbum();
        when(albumSpotifyClient.getAlbum(eq("Bearer token"), eq("123"))).thenReturn(album);

        Album result = albumService.getAlbumById("123");

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals("123", result.getId());
    }

    @Test
    @DisplayName("Deve lançar erro quando o token for nulo ou vazio")
    void shouldFailWhenAccessTokenIsInvalid() {
        when(authService.getAccessToken()).thenReturn(null);
        assertThrows(SpotifyAuthenticationException.class, () -> albumService.getAlbumById("123"));

        when(authService.getAccessToken()).thenReturn(" ");
        assertThrows(SpotifyAuthenticationException.class, () -> albumService.getAlbumById("123"));
    }

    @Test
    @DisplayName("Deve lançar erro quando a API do Spotify retornar álbum nulo")
    void shouldFailWhenSpotifyReturnsNullAlbum() {
        when(authService.getAccessToken()).thenReturn("token");
        when(albumSpotifyClient.getAlbum(eq("Bearer token"), eq("123"))).thenReturn(null);

        assertThrows(SpotifyApiException.class, () -> albumService.getAlbumById("123"));
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
