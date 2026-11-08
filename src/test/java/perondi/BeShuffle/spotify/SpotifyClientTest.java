package perondi.BeShuffle.spotify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Testes do cliente da Web API do Spotify")
class SpotifyClientTest {

    private MockRestServiceServer server;
    private SpotifyClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.spotify.com");
        server = MockRestServiceServer.bindTo(builder).build();
        SpotifyTokenProvider tokenProvider = mock(SpotifyTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("token");
        client = new SpotifyClient(builder.build(), tokenProvider);
    }

    @Test
    @DisplayName("Busca o álbum com o token e ignora campos que não usamos")
    void shouldGetAlbum() {
        server.expect(requestTo("https://api.spotify.com/v1/albums/abc"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {"id":"abc","name":"Disco","release_date":"2020-05-01","uri":"spotify:album:abc",
                         "label":"Selo","popularity":50,"total_tracks":10,
                         "images":[{"url":"https://img/1.jpg","height":640,"width":640}],
                         "artists":[{"id":"ar1","name":"Artista","uri":"spotify:artist:ar1","type":"artist"}]}""",
                        MediaType.APPLICATION_JSON));

        Album album = client.getAlbum("abc");

        assertEquals("abc", album.getId());
        assertEquals("2020-05-01", album.getReleaseDate());
        assertEquals("Artista", album.getArtists().getFirst().getName());
        assertEquals("https://img/1.jpg", album.getImages().getFirst().getUrl());
        server.verify();
    }

    @Test
    @DisplayName("Monta a busca de álbuns com query, limite e offset")
    void shouldSearchAlbums() {
        server.expect(requestTo("https://api.spotify.com/v1/search?q=k&type=album&limit=5&offset=42"))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {"albums":{"total":1,"items":[{"id":"x","name":"Nome","album_type":"album","total_tracks":9}]}}""",
                        MediaType.APPLICATION_JSON));

        List<AlbumSearchItem> items = client.searchAlbums("k", 5, 42);

        assertEquals(List.of(new AlbumSearchItem("x", "Nome", "album")), items);
        server.verify();
    }

    @Test
    @DisplayName("Devolve lista vazia quando a resposta não tem álbuns")
    void shouldReturnEmptyListWhenNoAlbums() {
        server.expect(requestTo("https://api.spotify.com/v1/search?q=k&type=album&limit=5&offset=0"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertTrue(client.searchAlbums("k", 5, 0).isEmpty());
    }
}
