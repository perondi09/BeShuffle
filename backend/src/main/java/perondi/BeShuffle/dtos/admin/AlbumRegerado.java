package perondi.BeShuffle.dtos.admin;

import java.time.LocalDate;

/** Resposta de POST /api/admin/daily-album/regenerate: o novo Álbum do Dia. */
public record AlbumRegerado(String albumName, String artistName, LocalDate date) {
}
