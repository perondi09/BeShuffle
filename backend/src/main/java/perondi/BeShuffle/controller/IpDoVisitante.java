package perondi.BeShuffle.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * IP de quem chamou, só como hash: serve para o teto de sorteios por rede e nunca é gravado em texto (LGPD).
 *
 * <p>A API fica sempre atrás de um proxy (nginx no Docker, CloudFront na AWS), e os dois acrescentam o IP real
 * no fim do header X-Forwarded-For. Por isso vale o último item: os anteriores podem ter sido inventados pelo
 * cliente. Sem o header (chamada direta), usa o endereço da conexão.
 *
 * <p>O hash leva um sal aleatório criado quando a API sobe, então não dá para descobrir o IP a partir dele.
 * Reiniciar a API troca o sal e zera o teto do dia, o que é aceitável.
 */
@Component
public class IpDoVisitante {

    private final byte[] sal = new byte[16];

    public IpDoVisitante() {
        new SecureRandom().nextBytes(sal);
    }

    public String hash(HttpServletRequest request) {
        return hash(ip(request));
    }

    static String ip(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            String[] partes = encaminhado.split(",");
            String ultimo = partes[partes.length - 1].trim();
            if (!ultimo.isEmpty()) {
                return ultimo;
            }
        }
        return request.getRemoteAddr();
    }

    String hash(String ip) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(sal);
            byte[] digest = sha.digest(String.valueOf(ip).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
