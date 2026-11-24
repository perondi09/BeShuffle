package perondi.BeShuffle.dtos.perfil;

import java.time.Instant;

/**
 * O perfil de uma conta, como as outras contas logadas veem (GET /api/users/{handle}). Sem e-mail. A média é em
 * estrelas com uma casa e vem nula enquanto a conta não deu nenhuma nota.
 */
public record PerfilPublico(String handle, String displayName, String avatar, Instant joinedAt, long reviewCount,
                            Double average) {
}
