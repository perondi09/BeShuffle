package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import perondi.BeShuffle.config.AdminEmails;
import perondi.BeShuffle.dtos.auth.AtualizarPerfilRequest;
import perondi.BeShuffle.dtos.auth.LoginRequest;
import perondi.BeShuffle.dtos.auth.RegisterRequest;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.entity.AppUser;
import perondi.BeShuffle.exceptions.ContaSuspensaException;
import perondi.BeShuffle.exceptions.CredenciaisInvalidasException;
import perondi.BeShuffle.exceptions.EmailJaCadastradoException;
import perondi.BeShuffle.exceptions.LoginNecessarioException;
import perondi.BeShuffle.exceptions.NickEmUsoException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.repository.AppUserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Serviço de contas: cadastro e login")
class AuthServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T15:00:00Z");

    private AppUserRepository repository;
    private PasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repository = mock(AppUserRepository.class);
        encoder = new BCryptPasswordEncoder(4); // custo baixo só para o teste ficar rápido
        service = new AuthService(repository, encoder, Clock.fixed(AGORA, ZoneOffset.UTC),
                new AdminEmails(" Chefe@Exemplo.com , outro@exemplo.com"));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    private AppUser salvo(String email, String senha) {
        AppUser u = new AppUser();
        u.setId(7L);
        u.setEmail(email);
        u.setDisplayName("Ana");
        u.setHandle("ana");
        u.setPasswordHash(encoder.encode(senha));
        u.setCreatedAt(AGORA);
        return u;
    }

    @Test
    @DisplayName("Cadastra com e-mail em minúsculas, nome sem espaços nas pontas e senha com hash BCrypt")
    void cadastra() {
        when(repository.existsByEmail("ana@exemplo.com")).thenReturn(false);

        UsuarioLogado logado = service.register(new RegisterRequest("  Ana  ", " Ana@Exemplo.COM ", "senha-forte-1"));

        ArgumentCaptor<AppUser> salvo = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(salvo.capture());
        AppUser u = salvo.getValue();
        assertEquals("ana@exemplo.com", u.getEmail());
        assertEquals("Ana", u.getDisplayName());
        assertEquals(AGORA, u.getCreatedAt());
        assertNotEquals("senha-forte-1", u.getPasswordHash());
        assertTrue(encoder.matches("senha-forte-1", u.getPasswordHash()));
        assertEquals(new UsuarioLogado(1L, "ana@exemplo.com", "Ana", false, "ana", "rosa"), logado);
        assertEquals("ana", u.getHandle());
    }

    @Test
    @DisplayName("Recusa e-mail já cadastrado com 409")
    void recusaEmailRepetido() {
        when(repository.existsByEmail("ana@exemplo.com")).thenReturn(true);

        EmailJaCadastradoException e = assertThrows(EmailJaCadastradoException.class,
                () -> service.register(new RegisterRequest("Ana", "ana@exemplo.com", "senha-forte-1")));
        assertEquals(409, e.getHttpStatus());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Valida nome, e-mail e tamanho da senha (8 caracteres até 72 bytes do BCrypt)")
    void validaCampos() {
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("A", "ana@exemplo.com", "senha-forte-1")));
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest(null, "ana@exemplo.com", "senha-forte-1")));
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("Ana", "nao-e-email", "senha-forte-1")));
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("Ana", null, "senha-forte-1")));
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("Ana", "ana@exemplo.com", "curta")));
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("Ana", "ana@exemplo.com", null)));
        // 73 bytes: o BCrypt ignoraria o resto, então a senha seria aceita truncada
        assertThrows(ValidationException.class, () -> service.register(new RegisterRequest("Ana", "ana@exemplo.com", "a".repeat(73))));
        // 24 "ç" = 48 bytes em UTF-8: cabe
        when(repository.existsByEmail("ana@exemplo.com")).thenReturn(false);
        service.register(new RegisterRequest("Ana", "ana@exemplo.com", "ç".repeat(24)));
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Login com e-mail e senha certos devolve o usuário logado (e-mail sem diferenciar maiúscula)")
    void loginCerto() {
        when(repository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(salvo("ana@exemplo.com", "senha-forte-1")));

        UsuarioLogado logado = service.login(new LoginRequest("ANA@exemplo.com ", "senha-forte-1"));

        assertEquals(new UsuarioLogado(7L, "ana@exemplo.com", "Ana", false, "ana", "rosa"), logado);
    }

    @Test
    @DisplayName("Senha errada e e-mail inexistente dão o mesmo 401, sem dizer qual dos dois errou")
    void loginErrado() {
        when(repository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(salvo("ana@exemplo.com", "senha-forte-1")));
        when(repository.findByEmail("ninguem@exemplo.com")).thenReturn(Optional.empty());

        CredenciaisInvalidasException senhaErrada = assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequest("ana@exemplo.com", "outra-senha")));
        CredenciaisInvalidasException semConta = assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequest("ninguem@exemplo.com", "senha-forte-1")));

        assertEquals(401, senhaErrada.getHttpStatus());
        assertEquals(senhaErrada.getMessage(), semConta.getMessage());
    }

    @Test
    @DisplayName("Login sem e-mail ou senha também é 401")
    void loginVazio() {
        assertThrows(CredenciaisInvalidasException.class, () -> service.login(new LoginRequest(null, "x")));
        assertThrows(CredenciaisInvalidasException.class, () -> service.login(new LoginRequest("ana@exemplo.com", null)));
    }

    @Test
    @DisplayName("Conta banida com a senha certa recebe 403 de conta suspensa; com a senha errada, o 401 de sempre")
    void contaBanida() {
        AppUser banida = salvo("ana@exemplo.com", "senha-forte-1");
        banida.setBannedAt(AGORA);
        when(repository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(banida));

        ContaSuspensaException suspensa = assertThrows(ContaSuspensaException.class,
                () -> service.login(new LoginRequest("ana@exemplo.com", "senha-forte-1")));
        assertEquals(403, suspensa.getHttpStatus());
        // Sem a senha certa ninguém descobre que a conta existe e foi banida.
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequest("ana@exemplo.com", "outra-senha")));
    }

    @Test
    @DisplayName("E-mail da lista ADMIN_EMAILS vira admin no cadastro e no login; os outros não")
    void promoveAdmin() {
        when(repository.existsByEmail("chefe@exemplo.com")).thenReturn(false);
        UsuarioLogado novo = service.register(new RegisterRequest("Chefe", "CHEFE@exemplo.com", "senha-forte-1"));
        assertTrue(novo.admin());

        AppUser antigo = salvo("outro@exemplo.com", "senha-forte-1");
        when(repository.findByEmail("outro@exemplo.com")).thenReturn(Optional.of(antigo));
        UsuarioLogado logado = service.login(new LoginRequest("outro@exemplo.com", "senha-forte-1"));
        assertTrue(logado.admin());
        assertEquals(AppUser.ADMIN, antigo.getRole());

        when(repository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(salvo("ana@exemplo.com", "senha-forte-1")));
        assertFalse(service.login(new LoginRequest("ana@exemplo.com", "senha-forte-1")).admin());
    }

    @Test
    @DisplayName("Cadastro gera o nick a partir do nome (sem acento, minúsculo) e põe número quando já existe")
    void geraNick() {
        when(repository.existsByHandle("marina_wav")).thenReturn(true);

        service.register(new RegisterRequest("Marína Wav", "m@exemplo.com", "senha-forte-1"));

        ArgumentCaptor<AppUser> salvo = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(salvo.capture());
        assertEquals("marina_wav2", salvo.getValue().getHandle());
        assertEquals("rosa", salvo.getValue().getAvatarKey());
    }

    @Test
    @DisplayName("Atualiza nome, nick e avatar da própria conta, tirando espaços e maiúsculas")
    void atualizaPerfil() {
        AppUser conta = salvo("ana@exemplo.com", "senha-forte-1");
        when(repository.findById(7L)).thenReturn(Optional.of(conta));

        UsuarioLogado novo = service.atualizarPerfil(7L, new AtualizarPerfilRequest("  Ana Lima ", " Ana_Lima ", "Menta"));

        assertEquals(new UsuarioLogado(7L, "ana@exemplo.com", "Ana Lima", false, "ana_lima", "menta"), novo);
        verify(repository).saveAndFlush(conta);
    }

    @Test
    @DisplayName("Campo ausente fica como está e repetir o próprio nick não conta como nick em uso")
    void atualizaSoUmaParte() {
        AppUser conta = salvo("ana@exemplo.com", "senha-forte-1");
        when(repository.findById(7L)).thenReturn(Optional.of(conta));

        UsuarioLogado novo = service.atualizarPerfil(7L, new AtualizarPerfilRequest(null, "ANA", "gema"));

        assertEquals(new UsuarioLogado(7L, "ana@exemplo.com", "Ana", false, "ana", "gema"), novo);
        verify(repository, never()).existsByHandle(any());
        assertEquals("Ana", service.atualizarPerfil(7L, null).displayName());
    }

    @Test
    @DisplayName("Recusa nick inválido ou reservado, nick de outra conta, avatar desconhecido e nome curto")
    void recusaPerfilInvalido() {
        AppUser conta = salvo("ana@exemplo.com", "senha-forte-1");
        when(repository.findById(7L)).thenReturn(Optional.of(conta));
        when(repository.existsByHandle("bruno")).thenReturn(true);

        for (String nick : new String[]{"ab", "ana lima", "ana-lima", "ação", "x".repeat(21), "", "admin", "Beshuffle"}) {
            assertThrows(ValidationException.class,
                    () -> service.atualizarPerfil(7L, new AtualizarPerfilRequest(null, nick, null)), "nick: " + nick);
        }
        NickEmUsoException emUso = assertThrows(NickEmUsoException.class,
                () -> service.atualizarPerfil(7L, new AtualizarPerfilRequest(null, "Bruno", null)));
        assertEquals(409, emUso.getHttpStatus());
        assertThrows(ValidationException.class, () -> service.atualizarPerfil(7L, new AtualizarPerfilRequest(null, null, "roxo")));
        assertThrows(ValidationException.class, () -> service.atualizarPerfil(7L, new AtualizarPerfilRequest("A", null, null)));
        verify(repository, never()).saveAndFlush(any());
        assertEquals("ana", conta.getHandle());
    }

    @Test
    @DisplayName("Dois pedidos pelo mesmo nick ao mesmo tempo: o que perde a corrida recebe 409")
    void nickPegoNaCorrida() {
        AppUser conta = salvo("ana@exemplo.com", "senha-forte-1");
        when(repository.findById(7L)).thenReturn(Optional.of(conta));
        when(repository.saveAndFlush(any(AppUser.class))).thenThrow(new DataIntegrityViolationException("uk_users_handle"));

        assertThrows(NickEmUsoException.class,
                () -> service.atualizarPerfil(7L, new AtualizarPerfilRequest(null, "ana2", null)));
    }

    @Test
    @DisplayName("GET /api/me lê a conta no banco; conta banida ou apagada vira 401")
    void perfilDoBanco() {
        AppUser conta = salvo("ana@exemplo.com", "senha-forte-1");
        when(repository.findById(7L)).thenReturn(Optional.of(conta));
        assertEquals("ana", service.perfil(7L).handle());

        conta.setBannedAt(AGORA);
        assertEquals(401, assertThrows(LoginNecessarioException.class, () -> service.perfil(7L)).getHttpStatus());
        assertThrows(LoginNecessarioException.class, () -> service.perfil(99L));
    }
}
