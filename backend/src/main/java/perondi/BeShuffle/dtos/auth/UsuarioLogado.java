package perondi.BeShuffle.dtos.auth;

import java.io.Serializable;

/**
 * Quem está logado. Fica na sessão (por isso Serializable) e é a resposta de GET /api/me.
 * Nunca carrega a senha nem o hash. {@code admin} diz se a conta vê o painel /admin; sessões gravadas antes
 * deste campo existir são lidas com admin = false (records toleram campo novo na desserialização).
 */
public record UsuarioLogado(Long id, String email, String displayName, boolean admin) implements Serializable {

    public UsuarioLogado(Long id, String email, String displayName) {
        this(id, email, displayName, false);
    }
}
