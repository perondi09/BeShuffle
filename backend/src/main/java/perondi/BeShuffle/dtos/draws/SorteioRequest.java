package perondi.BeShuffle.dtos.draws;

import java.util.List;

/** Corpo opcional de POST /api/draws. {@code genres} vazio ou ausente = sorteio geral. */
public record SorteioRequest(List<String> genres) {
}
