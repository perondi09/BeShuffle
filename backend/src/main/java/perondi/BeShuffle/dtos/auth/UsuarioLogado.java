package perondi.BeShuffle.dtos.auth;

import java.io.Serializable;

/**
 * Quem está logado. Fica na sessão (por isso Serializable) e é a resposta de GET /api/me.
 * Nunca carrega a senha nem o hash.
 */
public record UsuarioLogado(Long id, String email, String displayName) implements Serializable {
}
