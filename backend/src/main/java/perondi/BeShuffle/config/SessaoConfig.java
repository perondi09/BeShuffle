package perondi.BeShuffle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

/**
 * Cookie da sessão guardada no Postgres (Spring Session JDBC). Sem este bean o Spring Session usaria o
 * cookie "SESSION" sem HttpOnly nem SameSite; aqui ele segue as mesmas regras de server.servlet.session.cookie.*
 * que valiam para a sessão em memória: nome JSESSIONID, HttpOnly, SameSite=Lax, Secure em produção e a mesma
 * validade da sessão.
 */
@Configuration
public class SessaoConfig {

    @Bean
    public CookieSerializer cookieSerializer(
            @Value("${server.servlet.session.cookie.name:JSESSIONID}") String nome,
            @Value("${server.servlet.session.cookie.secure:false}") boolean secure,
            @Value("${server.servlet.session.cookie.same-site:lax}") String sameSite,
            @Value("${server.servlet.session.timeout:7d}") Duration validade) {
        DefaultCookieSerializer cookie = new DefaultCookieSerializer();
        cookie.setCookieName(nome);
        cookie.setUseHttpOnlyCookie(true);
        cookie.setUseSecureCookie(secure);
        cookie.setSameSite(Character.toUpperCase(sameSite.charAt(0)) + sameSite.substring(1).toLowerCase());
        cookie.setCookiePath("/");
        cookie.setCookieMaxAge((int) validade.toSeconds());
        // O Spring Session codifica o id em Base64 por padrão; sem isso o valor fica igual ao id do banco.
        cookie.setUseBase64Encoding(false);
        return cookie;
    }
}
