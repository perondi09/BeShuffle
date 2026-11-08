package perondi.BeShuffle.spotify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Testes do provedor de token do Spotify")
class SpotifyTokenProviderTest {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private MockRestServiceServer server;
    private RestClient restClient;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://accounts.spotify.com");
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        clock = new MutableClock(Instant.parse("2026-01-01T12:00:00Z"));
    }

    private SpotifyTokenProvider provider(String clientId, String clientSecret) {
        return new SpotifyTokenProvider(restClient, clock, clientId, clientSecret);
    }

    private static String tokenJson(String token, int expiresIn) {
        return "{\"access_token\":\"" + token + "\",\"token_type\":\"Bearer\",\"expires_in\":" + expiresIn + "}";
    }

    @Test
    @DisplayName("Pede o token com client_credentials e devolve o access_token")
    void shouldReturnAccessToken() {
        server.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", "client-id",
                        "client_secret", "client-secret")))
                .andRespond(withSuccess(tokenJson("abcdef", 3600), MediaType.APPLICATION_JSON));

        assertEquals("abcdef", provider("client-id", "client-secret").getAccessToken());
        server.verify();
    }

    @Test
    @DisplayName("Reaproveita o token enquanto ele não está perto de expirar")
    void shouldReuseCachedToken() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess(tokenJson("first", 3600), MediaType.APPLICATION_JSON));
        SpotifyTokenProvider provider = provider("client-id", "client-secret");

        assertEquals("first", provider.getAccessToken());
        clock.advance(Duration.ofMinutes(58));
        assertEquals("first", provider.getAccessToken());
        server.verify();
    }

    @Test
    @DisplayName("Pede um token novo quando falta menos de um minuto para expirar")
    void shouldRefreshTokenNearExpiry() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess(tokenJson("first", 3600), MediaType.APPLICATION_JSON));
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess(tokenJson("second", 3600), MediaType.APPLICATION_JSON));
        SpotifyTokenProvider provider = provider("client-id", "client-secret");

        assertEquals("first", provider.getAccessToken());
        clock.advance(Duration.ofMinutes(59).plusSeconds(1));
        assertEquals("second", provider.getAccessToken());
        server.verify();
    }

    @Test
    @DisplayName("Rejeita credenciais ausentes sem chamar o Spotify")
    void shouldRejectMissingCredentials() {
        server.expect(never(), requestTo(TOKEN_URL));

        assertThrows(ValidationException.class, () -> provider("", "client-secret").getAccessToken());
        assertThrows(ValidationException.class, () -> provider("client-id", " ").getAccessToken());
        server.verify();
    }

    @Test
    @DisplayName("Falha quando o Spotify devolve token vazio")
    void shouldFailWhenTokenIsBlank() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess(tokenJson("", 3600), MediaType.APPLICATION_JSON));

        assertThrows(SpotifyAuthenticationException.class, () -> provider("client-id", "client-secret").getAccessToken());
    }

    @Test
    @DisplayName("Converte erro HTTP do Spotify em falha de autenticação")
    void shouldWrapHttpErrors() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertThrows(SpotifyAuthenticationException.class, () -> provider("client-id", "client-secret").getAccessToken());
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
