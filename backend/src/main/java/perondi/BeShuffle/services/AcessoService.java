package perondi.BeShuffle.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.controller.IpDoVisitante;
import perondi.BeShuffle.controller.VisitanteCookie;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.repository.AcessoRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HexFormat;

/**
 * Conta quem abriu o site em cada dia e área, para o painel de admin. Chave do visitante: "u:&lt;id&gt;" para conta,
 * "a:&lt;hash do cookie bs_anon&gt;" para visitante com cookie e "i:&lt;hash do IP&gt;" para quem ainda não tem cookie.
 * Nunca guarda o IP puro nem o valor do cookie. Falhar aqui nunca derruba a página: só registra um aviso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcessoService {

    public static final String MUSICA = "musica";
    public static final String FILMES = "filmes";

    private final AcessoRepository repository;
    private final VisitanteCookie visitanteCookie;
    private final IpDoVisitante ipDoVisitante;
    private final Clock clock;

    public void registrar(HttpServletRequest request, UsuarioLogado usuario, String area) {
        try {
            boolean logado = usuario != null;
            String chave = logado
                    ? "u:" + usuario.id()
                    : visitanteCookie.ler(request)
                            .map(cookie -> "a:" + resumo(cookie))
                            .orElseGet(() -> "i:" + ipDoVisitante.hash(request));
            repository.registrar(LocalDate.now(clock), chave, area, logado, ehCelular(request.getHeader("User-Agent")));
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar o acesso: {}", e.getMessage());
        }
    }

    /** Navegadores de celular e tablet trazem "Mobi" ou "Android" no User-Agent. */
    static boolean ehCelular(String userAgent) {
        return userAgent != null && (userAgent.contains("Mobi") || userAgent.contains("Android"));
    }

    static String resumo(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
