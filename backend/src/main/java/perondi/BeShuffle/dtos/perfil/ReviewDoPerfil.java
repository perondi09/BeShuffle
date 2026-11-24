package perondi.BeShuffle.dtos.perfil;

import java.time.Instant;
import java.time.LocalDate;

/** Uma entrada do diário de notas de uma conta: o Álbum do Dia avaliado, a nota em estrelas e o comentário. */
public record ReviewDoPerfil(String albumId, String albumName, String albumArtist, String albumImageUrl,
                             LocalDate date, Double rating, String comment, Instant updatedAt) {
}
