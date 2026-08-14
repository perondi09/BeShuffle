package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import perondi.BeShuffle.client.AuthSpotifyClient;
import perondi.BeShuffle.dtos.login.LoginRequest;
import perondi.BeShuffle.dtos.login.LoginResponse;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;
import perondi.BeShuffle.exceptions.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Testes do serviço de autenticação")
class AuthServiceTest {

    @Mock
    private AuthSpotifyClient authSpotifyClient;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(authSpotifyClient);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "clientId", "client-id");
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "clientSecret", "client-secret");
    }

    @Test
    @DisplayName("Deve retornar token quando as credenciais forem válidas")
    void shouldReturnAccessToken() {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("abcdef");
        when(authSpotifyClient.login(any(LoginRequest.class))).thenReturn(response);

        String token = authService.getAccessToken();

        assertEquals("abcdef", token);
    }

    @Test
    @DisplayName("Deve rejeitar credenciais ausentes")
    void shouldRejectMissingCredentials() {
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "clientId", "");
        assertThrows(ValidationException.class, authService::getAccessToken);

        org.springframework.test.util.ReflectionTestUtils.setField(authService, "clientId", "client-id");
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "clientSecret", "");
        assertThrows(ValidationException.class, authService::getAccessToken);
    }

    @Test
    @DisplayName("Deve falhar quando a API retornar resposta vazia")
    void shouldFailWhenSpotifyReturnsEmptyResponse() {
        when(authSpotifyClient.login(any(LoginRequest.class))).thenReturn(null);

        assertThrows(SpotifyAuthenticationException.class, authService::getAccessToken);
    }
}
