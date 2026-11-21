package perondi.BeShuffle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * E-mails que viram admin ao entrar ou se cadastrar (ADMIN_EMAILS no infra/.env, separados por vírgula).
 * Não existe tela nem rota para promover alguém: tirar um e-mail da lista não rebaixa quem já é admin.
 */
@Component
public class AdminEmails {

    private final Set<String> emails;

    public AdminEmails(@Value("${beshuffle.admin.emails:}") String lista) {
        this.emails = Arrays.stream(lista == null ? new String[0] : lista.split(","))
                .map(e -> e.trim().toLowerCase(Locale.ROOT))
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /** O e-mail já deve vir normalizado (minúsculo, sem espaços), como o AuthService guarda. */
    public boolean contem(String email) {
        return email != null && emails.contains(email);
    }
}
