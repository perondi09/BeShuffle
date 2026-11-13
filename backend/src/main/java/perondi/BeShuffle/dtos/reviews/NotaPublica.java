package perondi.BeShuffle.dtos.reviews;

import java.time.Instant;

/** Nota de alguém da comunidade. Mostra só o nome de exibição, nunca o e-mail. */
public record NotaPublica(String author, Integer rating, String comment, Instant updatedAt) {
}
