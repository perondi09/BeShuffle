package perondi.BeShuffle.dtos.reviews;

import java.time.Instant;

/** A nota de quem está logado, para preencher o formulário. */
public record MinhaNota(Integer rating, String comment, Instant updatedAt) {
}
