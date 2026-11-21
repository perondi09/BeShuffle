package perondi.BeShuffle.dtos.admin;

import java.time.Instant;

/** Uma conta na lista do admin. Nunca carrega a senha nem o hash. */
public record UsuarioAdmin(
        Long id,
        String email,
        String displayName,
        String role,
        Instant createdAt,
        Instant bannedAt,
        String banReason,
        long draws,
        long reviews) {
}
