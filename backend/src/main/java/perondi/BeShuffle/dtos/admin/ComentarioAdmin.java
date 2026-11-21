package perondi.BeShuffle.dtos.admin;

import java.time.Instant;
import java.time.LocalDate;

/** Um comentário de nota, para moderação. Remover apaga só o texto; a nota continua valendo. */
public record ComentarioAdmin(
        Long reviewId,
        Long userId,
        String displayName,
        String email,
        int rating,
        String comment,
        Instant updatedAt,
        String albumName,
        LocalDate albumDate,
        String area) {
}
