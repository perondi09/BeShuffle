package perondi.BeShuffle.dtos.reviews;

import java.time.LocalDate;
import java.util.List;

/**
 * Resposta de GET e PUT /api/reviews/today: o Álbum do Dia (nome, artista, capa e o dia), a média, o total, a nota
 * de quem pediu e as mais recentes.
 */
public record NotasDoDia(String albumId, String albumName, String albumArtist, String albumImageUrl, LocalDate date,
                         Double average, long count, MinhaNota mine, List<NotaPublica> reviews) {
}
