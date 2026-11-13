package perondi.BeShuffle.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Cookie bs_anon: um UUID aleatório que identifica o visitante sem login, para contar o sorteio do dia
 * no servidor. HttpOnly (o site não precisa ler) e sem nenhum dado pessoal.
 */
@Component
public class VisitanteCookie {

    static final String NOME = "bs_anon";
    private static final Duration VALIDADE = Duration.ofDays(365);
    private static final Pattern UUID_VALIDO =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private final boolean secure;

    public VisitanteCookie(@Value("${server.servlet.session.cookie.secure:false}") boolean secure) {
        this.secure = secure;
    }

    /** O id do visitante, se o navegador mandou um cookie válido. */
    public Optional<String> ler(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (NOME.equals(cookie.getName()) && cookie.getValue() != null
                    && UUID_VALIDO.matcher(cookie.getValue()).matches()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    /** O id do visitante; cria um novo e manda o cookie quando ainda não existe. */
    public String lerOuCriar(HttpServletRequest request, HttpServletResponse response) {
        return ler(request).orElseGet(() -> {
            String novo = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from(NOME, novo)
                    .httpOnly(true)
                    .secure(secure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(VALIDADE)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return novo;
        });
    }
}
