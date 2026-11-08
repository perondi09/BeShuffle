package perondi.BeShuffle.services;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Testes do serviço de álbum diário")
class DailyAlbumServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Mock
    private DailyAlbumRepository dailyAlbumRepository;

    @Mock
    private AlbumPicker albumPicker;

    @Mock
    private AlbumService albumService;

    private ObjectMapper objectMapper;
    private Clock clock;
    private DailyAlbumService dailyAlbumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = JsonMapper.builder().build();
        // 02/01/2026 01:00 em UTC = 01/01/2026 22:00 em São Paulo
        clock = Clock.fixed(Instant.parse("2026-01-02T01:00:00Z"), SAO_PAULO);
        dailyAlbumService = new DailyAlbumService(dailyAlbumRepository, albumPicker, albumService, objectMapper, clock);
    }

    @Test
    @DisplayName("Deve retornar o álbum do dia salvo quando ele já existe")
    void shouldReturnExistingDailyAlbum() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        Album expected = createAlbum();
        when(dailyAlbumRepository.findByDisplayDate(date)).thenReturn(Optional.of(stored(date, expected)));

        Album result = dailyAlbumService.getAlbumDoDia();

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
    }

    @Test
    @DisplayName("Usa a data de São Paulo, e não a do servidor em UTC, para achar o álbum do dia")
    void shouldUseSaoPauloDateForToday() {
        LocalDate saoPauloToday = LocalDate.of(2026, 1, 1);
        when(dailyAlbumRepository.findByDisplayDate(saoPauloToday))
                .thenReturn(Optional.of(stored(saoPauloToday, createAlbum())));

        dailyAlbumService.getAlbumDoDia();

        verify(dailyAlbumRepository).findByDisplayDate(saoPauloToday);
    }

    @Test
    @DisplayName("Expõe a data de hoje no fuso configurado para o agendador")
    void shouldExposeTodayInConfiguredZone() {
        assertEquals(LocalDate.of(2026, 1, 1), dailyAlbumService.hoje());
    }

    @Test
    @DisplayName("Deve gerar e salvar um álbum do dia quando não existir")
    void shouldGenerateAndPersistDailyAlbum() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        Album album = createAlbum();

        when(dailyAlbumRepository.findByDisplayDate(date)).thenReturn(Optional.empty());
        when(albumPicker.pickAlbumId()).thenReturn("123");
        when(albumService.getAlbumById("123")).thenReturn(album);
        when(dailyAlbumRepository.save(any(DailyAlbum.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album result = dailyAlbumService.gerarESalvarAlbumDoDia(date);

        assertNotNull(result);
        assertEquals("123", result.getId());
        verify(dailyAlbumRepository).save(any(DailyAlbum.class));
    }

    private DailyAlbum stored(LocalDate date, Album album) {
        DailyAlbum stored = new DailyAlbum();
        stored.setId(1L);
        stored.setSpotifyAlbumId(album.getId());
        stored.setDisplayDate(date);
        stored.setFullAlbumJson(objectMapper.writeValueAsString(album));
        return stored;
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
