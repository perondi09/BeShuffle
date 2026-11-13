package perondi.BeShuffle.dtos.draws;

import perondi.BeShuffle.entity.Album;

/** Resposta de POST /api/draws: o álbum sorteado e o saldo já descontado. */
public record SorteioFeito(Album album, SaldoDeFichas allowance) {
}
