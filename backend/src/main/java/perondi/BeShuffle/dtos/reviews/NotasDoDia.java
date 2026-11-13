package perondi.BeShuffle.dtos.reviews;

import java.util.List;

/** Resposta de GET e PUT /api/reviews/today: média, total, a nota de quem pediu e as mais recentes. */
public record NotasDoDia(String albumId, String albumName, Double average, long count, MinhaNota mine,
                         List<NotaPublica> reviews) {
}
