package perondi.BeShuffle.dtos.draws;

import java.time.Instant;

/** Um álbum dos "Sorteados de hoje": quantas vezes saiu no dia e quando saiu pela última vez. */
public record AlbumSorteado(String id, String name, String artist, String imageUrl, Long count,
                            Instant lastDrawnAt) {
}
