package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.auth.LoginRequest;
import perondi.BeShuffle.dtos.auth.RegisterRequest;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.entity.AppUser;
import perondi.BeShuffle.exceptions.CredenciaisInvalidasException;
import perondi.BeShuffle.exceptions.EmailJaCadastradoException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.repository.AppUserRepository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Locale;
import java.util.regex.Pattern;

/** Cadastro e conferência de senha. A sessão em si é aberta pelo AuthController. */
@Slf4j
@Service
public class AuthService {

    // Mesmas regras do frontend (frontend/src/features/auth/validation.ts)
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_SENHA = 8;
    private static final int MAX_SENHA_BYTES = 72; // o BCrypt ignora o que passa disso
    private static final int MIN_NOME = 2;
    private static final int MAX_NOME = 60;
    private static final int MAX_EMAIL = 255;

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    /** Comparado quando o e-mail não existe, para o tempo de resposta não revelar quais e-mails têm conta. */
    private final String hashFalso;

    public AuthService(AppUserRepository repository, PasswordEncoder passwordEncoder, Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.hashFalso = passwordEncoder.encode("senha-que-nenhuma-conta-tem");
    }

    public UsuarioLogado register(RegisterRequest request) {
        String nome = request.displayName() == null ? "" : request.displayName().trim();
        String email = normalizarEmail(request.email());
        String senha = request.password();

        if (nome.length() < MIN_NOME || nome.length() > MAX_NOME) {
            throw new ValidationException("o nome precisa ter entre " + MIN_NOME + " e " + MAX_NOME + " caracteres", "displayName");
        }
        if (email.isEmpty() || email.length() > MAX_EMAIL || !EMAIL.matcher(email).matches()) {
            throw new ValidationException("e-mail inválido", "email");
        }
        if (senha == null || senha.length() < MIN_SENHA) {
            throw new ValidationException("a senha precisa de " + MIN_SENHA + " caracteres ou mais", "password");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > MAX_SENHA_BYTES) {
            throw new ValidationException("a senha é longa demais", "password");
        }
        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(nome);
        user.setPasswordHash(passwordEncoder.encode(senha));
        user.setCreatedAt(clock.instant());
        AppUser salvo = repository.save(user);
        log.info("Conta criada: id {}", salvo.getId());
        return paraLogado(salvo);
    }

    public UsuarioLogado login(LoginRequest request) {
        String email = normalizarEmail(request.email());
        String senha = request.password();
        if (email.isEmpty() || senha == null) {
            throw new CredenciaisInvalidasException();
        }
        AppUser user = repository.findByEmail(email).orElse(null);
        boolean senhaConfere = passwordEncoder.matches(senha, user == null ? hashFalso : user.getPasswordHash());
        if (user == null || !senhaConfere) {
            log.info("Login recusado");
            throw new CredenciaisInvalidasException();
        }
        return paraLogado(user);
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static UsuarioLogado paraLogado(AppUser user) {
        return new UsuarioLogado(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
