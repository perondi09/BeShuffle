package perondi.BeShuffle.dtos.draws;

import java.time.Instant;

/** Um sorteio da própria conta hoje (GET /api/draws/mine), do mais recente para o mais antigo. */
public record MeuSorteio(String id, String name, String artist, String imageUrl, Instant drawnAt) {
}
