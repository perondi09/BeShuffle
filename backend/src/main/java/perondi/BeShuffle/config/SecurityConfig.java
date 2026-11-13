package perondi.BeShuffle.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sessão por cookie (JSESSIONID, HttpOnly) no mesmo domínio do site, que chega à API pelo proxy /api.
 * Exigem login: /api/me, /api/draws/today e /api/reviews/**; o resto é público. CSRF no modo SPA: o cookie XSRF-TOKEN é legível
 * pelo site, que devolve o valor no header X-XSRF-TOKEN em todo POST.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** O AuthController grava a sessão por aqui depois do login, e o filtro lê pelo mesmo lugar. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/me", "/api/draws/today", "/api/reviews/**").authenticated()
                        .anyRequest().permitAll())
                .securityContext(ctx -> ctx.securityContextRepository(contextRepository))
                .csrf(csrf -> csrf.spa())
                // O token é "preguiçoso": sem isto o cookie só sairia quando alguém lesse o token.
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .sessionManagement(s -> s.sessionFixation(f -> f.changeSessionId()))
                // Sem tela de login para "voltar depois": guardar o pedido criaria uma sessão a cada visitante.
                .requestCache(cache -> cache.disable())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                escreverErro(res, objectMapper, 401, "UNAUTHORIZED", "Faça login para continuar"))
                        .accessDeniedHandler((req, res, e) ->
                                escreverErro(res, objectMapper, 403, "FORBIDDEN", "Requisição recusada")));
        return http.build();
    }

    /** Mesmo formato de erro do GlobalExceptionHandler. */
    private static void escreverErro(HttpServletResponse res, ObjectMapper objectMapper, int status,
                                     String erro, String mensagem) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", erro);
        body.put("message", mensagem);
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), body);
    }

    /** Lê o token CSRF em toda requisição para o cookie XSRF-TOKEN sempre chegar ao site. */
    static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
