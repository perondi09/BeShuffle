package perondi.BeShuffle.dtos.reviews;

import java.time.Instant;

/**
 * Nota de alguém da comunidade. Mostra o nome de exibição, o nick e o avatar (o site linka o autor para o perfil),
 * nunca o e-mail. A nota é em estrelas (pode ser 3,5).
 */
public record NotaPublica(String author, String handle, String avatar, Double rating, String comment,
                          Instant updatedAt) {
}
