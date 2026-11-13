package perondi.BeShuffle.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import perondi.BeShuffle.dtos.draws.AlbumSorteado;
import perondi.BeShuffle.entity.Draw;

import java.time.LocalDate;
import java.util.List;

public interface DrawRepository extends JpaRepository<Draw, Long> {

    /** Álbuns sorteados no dia, um por álbum, do mais recente para o mais antigo. */
    @Query("""
            select new perondi.BeShuffle.dtos.draws.AlbumSorteado(
                d.spotifyAlbumId, d.albumName, d.artistName, d.imageUrl, count(d), max(d.createdAt))
            from Draw d
            where d.drawDate = :dia
            group by d.spotifyAlbumId, d.albumName, d.artistName, d.imageUrl
            order by max(d.createdAt) desc""")
    List<AlbumSorteado> sorteadosDoDia(LocalDate dia, Pageable pagina);
}
