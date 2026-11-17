package perondi.BeShuffle.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import perondi.BeShuffle.controller.IpDoVisitante;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Teto de requisições por IP e por minuto nas rotas de maior risco: login e cadastro (força bruta, custo do
 * BCrypt) e sorteio (cada um chama o Spotify). Janela fixa em memória: reiniciar a API zera as contagens, o que
 * basta para uma instância só. O IP é o mesmo de {@link IpDoVisitante} (último item do X-Forwarded-For).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LimitadorDeRequisicoes extends OncePerRequestFilter {

    private static final long JANELA_MS = 60_000;
    private static final int MAX_CHAVES = 50_000;

    private record Janela(long inicio, int contagem) { }

    private final int limiteAuth;
    private final int limiteSorteio;
    private final LongSupplier relogioMs;
    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();

    @Autowired
    public LimitadorDeRequisicoes(@Value("${beshuffle.limite.auth-por-minuto:10}") int limiteAuth,
                                  @Value("${beshuffle.limite.sorteio-por-minuto:30}") int limiteSorteio) {
        this(limiteAuth, limiteSorteio, System::currentTimeMillis);
    }

    LimitadorDeRequisicoes(int limiteAuth, int limiteSorteio, LongSupplier relogioMs) {
        this.limiteAuth = limiteAuth;
        this.limiteSorteio = limiteSorteio;
        this.relogioMs = relogioMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String grupo = grupo(request);
        if (grupo == null) {
            chain.doFilter(request, response);
            return;
        }
        int limite = grupo.equals("auth") ? limiteAuth : limiteSorteio;
        long espera = tentar(grupo + ":" + IpDoVisitante.ip(request), limite);
        if (espera > 0) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(espera));
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\","
                    + "\"message\":\"Muitas tentativas. Aguarde um pouco e tente de novo\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    /** Segundos até a janela abrir de novo se o limite estourou; 0 se o pedido pode passar. */
    long tentar(String chave, int limite) {
        long agora = relogioMs.getAsLong();
        if (janelas.size() > MAX_CHAVES) {
            janelas.values().removeIf(j -> agora - j.inicio() >= JANELA_MS);
        }
        long[] espera = {0};
        janelas.compute(chave, (k, atual) -> {
            if (atual == null || agora - atual.inicio() >= JANELA_MS) {
                return new Janela(agora, 1);
            }
            if (atual.contagem() >= limite) {
                espera[0] = Math.max(1, (JANELA_MS - (agora - atual.inicio()) + 999) / 1000);
                return atual;
            }
            return new Janela(atual.inicio(), atual.contagem() + 1);
        });
        return espera[0];
    }

    private static String grupo(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return null;
        }
        String caminho = request.getRequestURI();
        if ("/api/auth/login".equals(caminho) || "/api/auth/register".equals(caminho)) {
            return "auth";
        }
        return "/api/draws".equals(caminho) ? "sorteio" : null;
    }
}
