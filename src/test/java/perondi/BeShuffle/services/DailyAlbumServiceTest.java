package perondi.BeShuffle.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AlbumImage;
import perondi.BeShuffle.entity.Artist;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.repository.DailyAlbumRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Testes do serviço de álbum diário")
class DailyAlbumServiceTest {

    @Mock
    private DailyAlbumRepository dailyAlbumRepository;

    @Mock
    private SpotifyRandomAlbumService spotifyRandomAlbumService;

    @Mock
    private AlbumService albumService;

    private ObjectMapper objectMapper;
    private DailyAlbumService dailyAlbumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        dailyAlbumService = new DailyAlbumService(dailyAlbumRepository, spotifyRandomAlbumService, albumService, objectMapper);
    }

    @Test
    @DisplayName("Deve retornar o álbum do dia salvo quando ele já existe")
    void shouldReturnExistingDailyAlbum() throws Exception {
        LocalDate date = LocalDate.now();
        Album expected = createAlbum();
        DailyAlbum stored = new DailyAlbum();
        stored.setId(1L);
        stored.setDisplayDate(date);
        stored.setFullAlbumJson(objectMapper.writeValueAsString(expected));

        when(dailyAlbumRepository.findByDisplayDate(date)).thenReturn(Optional.of(stored));

        Album result = dailyAlbumService.getAlbumDoDia();

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
    }

    @Test
    @DisplayName("Deve gerar e salvar um álbum do dia quando não existir")
    void shouldGenerateAndPersistDailyAlbum() throws Exception {
        LocalDate date = LocalDate.now();
        Album album = createAlbum();

        when(dailyAlbumRepository.findByDisplayDate(date)).thenReturn(Optional.empty());
        when(spotifyRandomAlbumService.getRandomAlbumIdFromSpotify()).thenReturn("123");
        when(albumService.getAlbumById("123")).thenReturn(album);
        when(dailyAlbumRepository.save(any(DailyAlbum.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album result = dailyAlbumService.gerarESalvarAlbumDoDia(date);

        assertNotNull(result);
        assertEquals("123", result.getId());
        verify(dailyAlbumRepository).save(any(DailyAlbum.class));
    }

    private Album createAlbum() {
        Album album = new Album();
        album.setId("123");
        album.setName("Daily Album");
        album.setReleaseDate("2025-01-15");
        album.setUri("spotify:album:123");

        Artist artist = new Artist();
        artist.setName("Daily Artist");
        album.setArtists(List.of(artist));

        AlbumImage image = new AlbumImage();
        image.setUrl("https://example.com/daily.jpg");
        album.setImages(List.of(image));

        return album;
    }
}
