package perondi.BeShuffle.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import perondi.BeShuffle.controller.AlbumController;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AlbumImage;
import perondi.BeShuffle.entity.Artist;
import perondi.BeShuffle.services.DailyAlbumService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Testes de integração do controller de álbuns")
class AlbumIntegrationTest {

    private final DailyAlbumService dailyAlbumService = mock(DailyAlbumService.class);

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new AlbumController(dailyAlbumService)
    ).build();

    @Test
    @DisplayName("Deve retornar o álbum do dia")
    void shouldReturnDailyAlbum() throws Exception {
        Album album = createAlbum();
        when(dailyAlbumService.getAlbumDoDia()).thenReturn(album);

        mockMvc.perform(get("/api/albums/daily").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.artists[0].name").value("Artist Name"));
    }

    @Test
    @DisplayName("O sorteio saiu de /api/albums/random e foi para POST /api/draws")
    void randomNaoExisteMais() throws Exception {
        mockMvc.perform(get("/api/albums/random")).andExpect(status().isNotFound());
    }

    private Album createAlbum() {
        Album album = new Album();
        album.setId("123");
        album.setName("Test Album");
        album.setReleaseDate("2024-01-01");
        album.setUri("spotify:album:123");

        Artist artist = new Artist();
        artist.setName("Artist Name");
        album.setArtists(List.of(artist));

        AlbumImage image = new AlbumImage();
        image.setUrl("https://example.com/image.jpg");
        album.setImages(List.of(image));

        return album;
    }
}
