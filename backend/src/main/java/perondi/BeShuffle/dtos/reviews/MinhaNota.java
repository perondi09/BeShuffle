package perondi.BeShuffle.dtos.reviews;

import java.time.Instant;

/** A nota de quem está logado, para preencher o formulário. A nota é em estrelas (pode ser 3,5). */
public record MinhaNota(Double rating, String comment, Instant updatedAt) {
}
