package perondi.BeShuffle.spotify;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;

/**
 * Clientes HTTP usados para conversar com o Spotify.
 * Um para a API de dados (api.spotify.com) e outro para o servidor de token (accounts.spotify.com).
 */
@Configuration
public class SpotifyConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RestClient spotifyApiRestClient(@Value("${spotify.api.url:https://api.spotify.com}") String baseUrl) {
        return buildClient(baseUrl);
    }

    @Bean
    public RestClient spotifyAccountsRestClient(@Value("${spotify.auth.url:https://accounts.spotify.com}") String baseUrl) {
        return buildClient(baseUrl);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    private RestClient buildClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
