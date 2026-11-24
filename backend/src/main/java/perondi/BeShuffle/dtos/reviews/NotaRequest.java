package perondi.BeShuffle.dtos.reviews;

/**
 * Corpo de PUT /api/reviews/today. A nota é em estrelas, de 0,5 a 5 de meia em meia (2,5 e 3,5 valem; quem manda
 * número inteiro continua funcionando). O comentário é opcional.
 */
public record NotaRequest(Double rating, String comment) {
}
