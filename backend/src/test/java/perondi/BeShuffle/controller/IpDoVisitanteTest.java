package perondi.BeShuffle.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IP do visitante (só como hash)")
class IpDoVisitanteTest {

    private static MockHttpServletRequest pedido(String encaminhado) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.9");
        if (encaminhado != null) {
            request.addHeader("X-Forwarded-For", encaminhado);
        }
        return request;
    }

    @Test
    @DisplayName("Usa o último item do X-Forwarded-For, que o proxy acrescenta; os anteriores o cliente pode inventar")
    void ultimoItemDoProxy() {
        assertEquals("200.1.2.3", IpDoVisitante.ip(pedido("1.1.1.1, 200.1.2.3")));
        assertEquals("200.1.2.3", IpDoVisitante.ip(pedido("200.1.2.3")));
    }

    @Test
    @DisplayName("Sem o header, usa o endereço da conexão")
    void semProxy() {
        assertEquals("10.0.0.9", IpDoVisitante.ip(pedido(null)));
        assertEquals("10.0.0.9", IpDoVisitante.ip(pedido("  ")));
    }

    @Test
    @DisplayName("O hash é estável para o mesmo IP, muda entre IPs e não contém o IP")
    void hash() {
        IpDoVisitante ip = new IpDoVisitante();
        String a = ip.hash(pedido("200.1.2.3"));
        assertEquals(a, ip.hash(pedido("9.9.9.9, 200.1.2.3")));
        assertNotEquals(a, ip.hash(pedido("200.1.2.4")));
        // Só hexadecimal: o IP (com pontos) não aparece. Procurar "200" no hash falhava ~0,7% das vezes por acaso.
        assertTrue(a.matches("[0-9a-f]{32}"));
    }

    @Test
    @DisplayName("Cada API sobe com um sal novo: o mesmo IP vira outro hash")
    void salPorInstancia() {
        assertNotEquals(new IpDoVisitante().hash("200.1.2.3"), new IpDoVisitante().hash("200.1.2.3"));
    }
}
