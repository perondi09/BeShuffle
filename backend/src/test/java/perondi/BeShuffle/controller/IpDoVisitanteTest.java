package perondi.BeShuffle.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals(32, a.length());
        assertFalse(a.contains("200"));
    }

    @Test
    @DisplayName("Cada API sobe com um sal novo: o mesmo IP vira outro hash")
    void salPorInstancia() {
        assertNotEquals(new IpDoVisitante().hash("200.1.2.3"), new IpDoVisitante().hash("200.1.2.3"));
    }
}
