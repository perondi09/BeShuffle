package perondi.BeShuffle.dtos.reviews;

/** Média e quantidade de notas de um álbum (média nula quando ninguém avaliou). */
public record ResumoNotas(Double average, Long count) {
}
