package perondi.BeShuffle.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import perondi.BeShuffle.entities.DailyAlbum;
import perondi.BeShuffle.exceptions.AlbumAlreadyUsedException;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.services.DailyAlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Testes do Controller")
class AlbumControllerUnitTest {

    @Mock
    private DailyAlbumService dailyAlbumService;

    @Mock
    private SpotifyRandomAlbumService spotifyRandomAlbumService;

    private AlbumController albumController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        albumController = new AlbumController(dailyAlbumService, spotifyRandomAlbumService);
    }

    @Test
    @DisplayName("GET /today retorna 200")
    void testGetTodayFound() {
        DailyAlbum album = new DailyAlbum();
        album.setAlbumName("Test");
        when(dailyAlbumService.getTodayAlbum()).thenReturn(album);

        ResponseEntity<DailyAlbum> response = albumController.getTodayAlbum();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /today retorna 204")
    void testGetTodayNotFound() {
        when(dailyAlbumService.getTodayAlbum()).thenReturn(null);
        ResponseEntity<DailyAlbum> response = albumController.getTodayAlbum();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /set-daily valida ID")
    void testSetValidation() {
        assertThrows(ValidationException.class, () -> albumController.setDailyAlbum(""));
        assertThrows(ValidationException.class, () -> albumController.setDailyAlbum(null));
    }

    @Test
    @DisplayName("POST /set-daily retorna 200")
    void testSetSuccess() {
        DailyAlbum album = new DailyAlbum();
        when(dailyAlbumService.setDailyAlbum("123")).thenReturn(album);

        ResponseEntity<DailyAlbum> response = albumController.setDailyAlbum("123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /random valida resultado")
    void testRandomValidation() {
        when(spotifyRandomAlbumService.getRandomAlbumIdFromSpotify()).thenReturn(null);
        assertThrows(SpotifyApiException.class, () -> albumController.setRandomDailyAlbum());
    }

    @Test
    @DisplayName("POST /random faz retry")
    void testRandomRetry() {
        DailyAlbum album = new DailyAlbum();
        when(spotifyRandomAlbumService.getRandomAlbumIdFromSpotify())
                .thenReturn("123")
                .thenReturn("456");
        when(dailyAlbumService.setDailyAlbum("123"))
                .thenThrow(new AlbumAlreadyUsedException("123"));
        when(dailyAlbumService.setDailyAlbum("456"))
                .thenReturn(album);

        ResponseEntity<DailyAlbum> response = albumController.setRandomDailyAlbum();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

