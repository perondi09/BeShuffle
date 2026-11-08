package perondi.BeShuffle.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Obtém o token de aplicação do Spotify (fluxo client_credentials) e o reaproveita
 * até um minuto antes de expirar, evitando um pedido de token a cada chamada da API.
 */
@Slf4j
@Component
public class SpotifyTokenProvider {

    private static final Duration EXPIRY_MARGIN = Duration.ofSeconds(60);

    private final RestClient accountsClient;
    private final Clock clock;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant cachedUntil = Instant.EPOCH;

    public SpotifyTokenProvider(
            @Qualifier("spotifyAccountsRestClient") RestClient accountsClient,
            Clock clock,
            @Value("${spotify.client.id}") String clientId,
            @Value("${spotify.client.secret}") String clientSecret
    ) {
        this.accountsClient = accountsClient;
        this.clock = clock;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && clock.instant().isBefore(cachedUntil)) {
            return cachedToken;
        }

        validateCredentials();
        TokenResponse response = requestToken();
        cachedToken = response.accessToken();
        cachedUntil = response.expiresIn() == null
                ? Instant.EPOCH
                : clock.instant().plusSeconds(response.expiresIn()).minus(EXPIRY_MARGIN);

        log.debug("Token de acesso do Spotify obtido com sucesso");
        return cachedToken;
    }

    /** Descarta o token em cache; a próxima chamada pede um novo ao Spotify. */
    public synchronized void invalidate() {
        cachedToken = null;
        cachedUntil = Instant.EPOCH;
    }

    private void validateCredentials() {
        if (clientId == null || clientId.isBlank()) {
            log.error("Credencial do Spotify ausente: clientId");
            throw new ValidationException("Credenciais Spotify não configuradas", "clientId");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            log.error("Credencial do Spotify ausente: clientSecret");
            throw new ValidationException("Credenciais Spotify não configuradas", "clientSecret");
        }
    }

    private TokenResponse requestToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        TokenResponse response;
        try {
            response = accountsClient.post()
                    .uri("/api/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
        } catch (Exception e) {
            // O detalhe (status, corpo da resposta, URL) vai só para o log, nunca para o cliente da API.
            log.error("Erro ao autenticar com Spotify", e);
            throw new SpotifyAuthenticationException("não foi possível obter o token de acesso", e);
        }

        if (response == null) {
            throw new SpotifyAuthenticationException("Resposta vazia do Spotify", 401);
        }
        if (response.accessToken() == null || response.accessToken().isBlank()) {
            throw new SpotifyAuthenticationException("Token de acesso vazio", 401);
        }
        return response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn
    ) {
    }
}
