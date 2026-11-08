package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyTokenProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

@DisplayName("Caracterização do sorteio de álbum aleatório")
class RandomQueryAlbumPickerTest {

    private static final String SEARCH_URL = "https://api.spotify.com/v1/search";

    private MockRestServiceServer server;
    private RandomQueryAlbumPicker picker;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.spotify.com");
        server = MockRestServiceServer.bindTo(builder).build();
        SpotifyTokenProvider tokenProvider = mock(SpotifyTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("token");
        picker = new RandomQueryAlbumPicker(new SpotifyClient(builder.build(), tokenProvider));
    }

    @Test
    @DisplayName("Ignora singles e retorna o primeiro item do tipo album")
    void returnsFirstFullAlbum() {
        server.expect(requestTo(startsWith(SEARCH_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {"albums":{"items":[
                          {"id":"single1","name":"Single","album_type":"single"},
                          {"id":"album1","name":"Album","album_type":"album"},
                          {"id":"album2","name":"Outro","album_type":"album"}
                        ]}}""", MediaType.APPLICATION_JSON));

        assertEquals("album1", picker.pickAlbumId());
        server.verify();
    }

    @Test
    @DisplayName("Tenta de novo quando a busca não traz álbum e para no primeiro acerto")
    void retriesUntilAlbumFound() {
        server.expect(requestTo(startsWith(SEARCH_URL)))
                .andRespond(withSuccess("{\"albums\":{\"items\":[]}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith(SEARCH_URL)))
                .andRespond(withSuccess("{\"albums\":{\"items\":[{\"id\":\"s\",\"album_type\":\"single\"}]}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith(SEARCH_URL)))
                .andRespond(withSuccess("{\"albums\":{\"items\":[{\"id\":\"found\",\"album_type\":\"album\"}]}}", MediaType.APPLICATION_JSON));

        assertEquals("found", picker.pickAlbumId());
        server.verify();
    }

    @Test
    @DisplayName("Retorna null depois de 5 tentativas sem sucesso, inclusive com erro da API")
    void returnsNullAfterFiveFailedAttempts() {
        server.expect(times(5), requestTo(startsWith(SEARCH_URL))).andRespond(withServerError());

        assertNull(picker.pickAlbumId());
        server.verify();
    }
}
