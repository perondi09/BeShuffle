package perondi.BeShuffle.services;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import perondi.BeShuffle.controller.IpDoVisitante;
import perondi.BeShuffle.controller.VisitanteCookie;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.repository.AcessoRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Registro de acessos do painel de admin")
class AcessoServiceTest {

    private static final String UUID_COOKIE = "123e4567-e89b-42d3-a456-426614174000";
    // 01:00 UTC do dia 21 ainda é 22:00 do dia 20 em São Paulo: o acesso conta no dia 20.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-21T01:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private AcessoRepository repository;
    private AcessoService service;

    @BeforeEach
    void setUp() {
        repository = mock(AcessoRepository.class);
        service = new AcessoService(repository, new VisitanteCookie(false), new IpDoVisitante(), CLOCK);
    }

    @Test
    @DisplayName("Conta logada vira u:<id>, com o dia no fuso do site")
    void contaLogada() {
        service.registrar(new MockHttpServletRequest(), new UsuarioLogado(42L, "a@b.co", "Ana", false, "ana", "rosa"), AcessoService.MUSICA);
        verify(repository).registrar(LocalDate.of(2026, 11, 20), "u:42", "musica", true, false);
    }

    @Test
    @DisplayName("Visitante com cookie vira a:<hash>, sem guardar o valor do cookie")
    void visitanteComCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("bs_anon", UUID_COOKIE));
        service.registrar(request, null, AcessoService.MUSICA);
        String chave = "a:" + AcessoService.resumo(UUID_COOKIE);
        verify(repository).registrar(any(), eq(chave), eq("musica"), eq(false), eq(false));
        assertFalse(chave.contains(UUID_COOKIE));
    }

    @Test
    @DisplayName("Visitante sem cookie vira i:<hash do IP>")
    void visitanteSemCookie() {
        service.registrar(new MockHttpServletRequest(), null, AcessoService.MUSICA);
        verify(repository).registrar(any(), startsWith("i:"), eq("musica"), eq(false), eq(false));
    }

    @Test
    @DisplayName("Reconhece celular pelo User-Agent")
    void celular() {
        assertTrue(AcessoService.ehCelular("Mozilla/5.0 (iPhone; CPU iPhone OS 18_0) Mobile/15E148"));
        assertTrue(AcessoService.ehCelular("Mozilla/5.0 (Linux; Android 15; Pixel 9)"));
        assertFalse(AcessoService.ehCelular("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/140"));
        assertFalse(AcessoService.ehCelular(null));
    }

    @Test
    @DisplayName("Erro ao gravar o acesso não derruba a página")
    void erroNaoPropaga() {
        doThrow(new IllegalStateException("banco fora")).when(repository)
                .registrar(any(), anyString(), anyString(), anyBoolean(), anyBoolean());
        assertDoesNotThrow(() -> service.registrar(new MockHttpServletRequest(), null, AcessoService.MUSICA));
    }
}
