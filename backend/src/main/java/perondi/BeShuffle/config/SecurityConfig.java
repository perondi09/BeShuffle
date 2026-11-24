package perondi.BeShuffle.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.repository.AppUserRepository;
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
 * Exigem login: /api/me, /api/draws/today, /api/draws/mine e /api/reviews/**; /api/admin/** exige conta admin; o resto
 * é público. Conta banida perde a sessão na próxima requisição (ContaBanidaFilter). CSRF no modo SPA: o cookie XSRF-TOKEN é legível
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
                                                   ObjectMapper objectMapper, AppUserRepository users) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/me", "/api/draws/today", "/api/draws/mine", "/api/reviews/**",
                                "/api/users/**").authenticated()
                        .anyRequest().permitAll())
                .securityContext(ctx -> ctx.securityContextRepository(contextRepository))
                .csrf(csrf -> csrf.spa())
                // O token é "preguiçoso": sem isto o cookie só sairia quando alguém lesse o token.
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new ContaBanidaFilter(users, objectMapper), CsrfCookieFilter.class)
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

    /**
     * Conta banida perde o acesso na próxima requisição: a sessão é apagada e a resposta é 401. Assim o
     * banimento vale mesmo para quem já estava logado, em qualquer aparelho, sem esperar a sessão expirar.
     */
    static final class ContaBanidaFilter extends OncePerRequestFilter {

        private final AppUserRepository users;
        private final ObjectMapper objectMapper;

        ContaBanidaFilter(AppUserRepository users, ObjectMapper objectMapper) {
            this.users = users;
            this.objectMapper = objectMapper;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioLogado usuario
                    && users.existsByIdAndBannedAtIsNotNull(usuario.id())) {
                HttpSession sessao = request.getSession(false);
                if (sessao != null) {
                    sessao.invalidate();
                }
                SecurityContextHolder.clearContext();
                escreverErro(response, objectMapper, 401, "ACCOUNT_SUSPENDED", "Sua conta foi suspensa");
                return;
            }
            chain.doFilter(request, response);
        }
    }
}
