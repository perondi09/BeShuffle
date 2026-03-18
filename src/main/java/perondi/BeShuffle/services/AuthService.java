package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.client.AuthSpotifyClient;
import perondi.BeShuffle.dtos.login.LoginRequest;
import perondi.BeShuffle.dtos.login.LoginResponse;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

@Slf4j
@Service
public class AuthService {

    private final AuthSpotifyClient authSpotifyClient;

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    public AuthService(AuthSpotifyClient authSpotifyClient) {
        this.authSpotifyClient = authSpotifyClient;
    }

    public String getAccessToken() {
        if (clientId == null || clientId.isEmpty()) {
            throw new ValidationException("Credenciais Spotify não configuradas", "clientId");
        }

        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new ValidationException("Credenciais Spotify não configuradas", "clientSecret");
        }

        try {
            LoginResponse response = authSpotifyClient.login(new LoginRequest("client_credentials", clientId, clientSecret));

            if (response == null) {
                throw new SpotifyAuthenticationException("Resposta vazia do Spotify", 401);
            }

            String token = response.getAccessToken();
            if (token == null || token.isEmpty()) {
                throw new SpotifyAuthenticationException("Token de acesso vazio", 401);
            }

            return token;
        } catch (SpotifyAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao autenticar com Spotify", e);
            throw new SpotifyAuthenticationException("Falha na autenticação Spotify: " + e.getMessage(), e);
        }
    }
}
