package perondi.BeShuffle.dtos.auth;

import java.io.Serializable;

/**
 * Quem está logado. Fica na sessão (por isso Serializable) e é a resposta de GET e PUT /api/me.
 * Nunca carrega a senha nem o hash. {@code admin} diz se a conta vê o painel /admin. Sessões gravadas antes de um
 * campo existir são lidas com o valor vazio (records toleram campo novo); por isso GET /api/me relê a conta no banco
 * em vez de devolver o que a sessão guardou.
 */
public record UsuarioLogado(Long id, String email, String displayName, boolean admin, String handle, String avatar)
        implements Serializable {
}
