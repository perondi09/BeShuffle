package perondi.BeShuffle.dtos.reviews;

/** Corpo de PUT /api/reviews/today. O comentário é opcional. */
public record NotaRequest(Integer rating, String comment) {
}
