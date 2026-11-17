package perondi.BeShuffle.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Limitador de requisições por IP")
class LimitadorDeRequisicoesTest {

    private final AtomicLong agora = new AtomicLong(1_000_000);
    private final LimitadorDeRequisicoes limitador = new LimitadorDeRequisicoes(3, 5, agora::get);

    private MockHttpServletResponse chamar(String metodo, String caminho, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, caminho);
        request.addHeader("X-Forwarded-For", ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        limitador.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    @DisplayName("Login passa até o limite e depois recebe 429 com Retry-After")
    void bloqueiaDepoisDoLimite() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertEquals(200, chamar("POST", "/api/auth/login", "1.1.1.1").getStatus());
        }
        MockHttpServletResponse bloqueada = chamar("POST", "/api/auth/login", "1.1.1.1");
        assertEquals(429, bloqueada.getStatus());
        assertNotNull(bloqueada.getHeader("Retry-After"));
    }

    @Test
    @DisplayName("Cada IP tem a sua contagem, e login e cadastro dividem o mesmo teto")
    void contagemPorIp() throws Exception {
        chamar("POST", "/api/auth/login", "1.1.1.1");
        chamar("POST", "/api/auth/register", "1.1.1.1");
        chamar("POST", "/api/auth/login", "1.1.1.1");
        assertEquals(429, chamar("POST", "/api/auth/register", "1.1.1.1").getStatus());
        assertEquals(200, chamar("POST", "/api/auth/login", "2.2.2.2").getStatus());
    }

    @Test
    @DisplayName("A janela reabre depois de um minuto")
    void janelaReabre() throws Exception {
        for (int i = 0; i < 3; i++) {
            chamar("POST", "/api/auth/login", "1.1.1.1");
        }
        assertEquals(429, chamar("POST", "/api/auth/login", "1.1.1.1").getStatus());
        agora.addAndGet(60_000);
        assertEquals(200, chamar("POST", "/api/auth/login", "1.1.1.1").getStatus());
    }

    @Test
    @DisplayName("Sorteio tem teto próprio; leituras (GET) nunca são limitadas")
    void sorteioEGet() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertEquals(200, chamar("POST", "/api/draws", "1.1.1.1").getStatus());
        }
        assertEquals(429, chamar("POST", "/api/draws", "1.1.1.1").getStatus());
        for (int i = 0; i < 20; i++) {
            assertEquals(200, chamar("GET", "/api/draws/allowance", "1.1.1.1").getStatus());
        }
    }
}
