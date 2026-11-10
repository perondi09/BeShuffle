package perondi.BeShuffle.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.auth.LoginRequest;
import perondi.BeShuffle.dtos.auth.RegisterRequest;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.services.AuthService;

import java.util.List;

/** Cadastro, login e "quem sou eu". O logout é do Spring Security (POST /api/auth/logout). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    @PostMapping("/auth/register")
    public ResponseEntity<UsuarioLogado> register(@RequestBody RegisterRequest body,
                                                  HttpServletRequest request, HttpServletResponse response) {
        UsuarioLogado usuario = authService.register(body);
        abrirSessao(usuario, request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest body,
                                      HttpServletRequest request, HttpServletResponse response) {
        abrirSessao(authService.login(body), request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UsuarioLogado me(@AuthenticationPrincipal UsuarioLogado usuario) {
        return usuario;
    }

    private void abrirSessao(UsuarioLogado usuario, HttpServletRequest request, HttpServletResponse response) {
        // Troca o id de uma sessão anônima que já existia, para um id roubado antes do login não valer depois.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext contexto = contextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        contextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
    }
}
