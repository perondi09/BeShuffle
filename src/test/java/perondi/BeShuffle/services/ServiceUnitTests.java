package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import perondi.BeShuffle.client.AlbumSpotifyClient;
import perondi.BeShuffle.dtos.album.Album;
import perondi.BeShuffle.dtos.album.AlbumImage;
import perondi.BeShuffle.dtos.album.Artist;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Testes unitários de Services")
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
    @DisplayName("Validar ID vazio")
    void testValidateEmptyId() {
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(""));
        assertThrows(ValidationException.class, () -> albumService.getAlbumById(null));
    }

    @Test
    @DisplayName("Obter token válido e retornar album")
    void testGetValidToken() {
        when(authService.getAccessToken()).thenReturn("token");
        Album album = createAlbum();
        when(albumSpotifyClient.getAlbum("Bearer token", "123")).thenReturn(album);

        Album result = albumService.getAlbumById("123");
        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    @DisplayName("Erro com token nulo")
    void testNullToken() {
        when(authService.getAccessToken()).thenReturn(null);
        assertThrows(SpotifyAuthenticationException.class, () -> albumService.getAlbumById("123"));
    }

    @Test
    @DisplayName("Erro com album nulo")
    void testNullAlbum() {
        when(authService.getAccessToken()).thenReturn("token");
        when(albumSpotifyClient.getAlbum("Bearer token", "123")).thenReturn(null);
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

