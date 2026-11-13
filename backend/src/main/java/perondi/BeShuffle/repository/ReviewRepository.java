package perondi.BeShuffle.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import perondi.BeShuffle.dtos.reviews.NotaPublica;
import perondi.BeShuffle.dtos.reviews.ResumoNotas;
import perondi.BeShuffle.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndDailyAlbumId(Long userId, Long dailyAlbumId);

    @Query("""
            select new perondi.BeShuffle.dtos.reviews.ResumoNotas(avg(r.rating * 1.0), count(r))
            from Review r where r.dailyAlbumId = :dailyAlbumId""")
    ResumoNotas resumo(Long dailyAlbumId);

    /** Notas mais recentes de um álbum, com o nome de exibição de quem avaliou. */
    @Query("""
            select new perondi.BeShuffle.dtos.reviews.NotaPublica(u.displayName, r.rating, r.comment, r.updatedAt)
            from Review r join AppUser u on u.id = r.userId
            where r.dailyAlbumId = :dailyAlbumId
            order by r.updatedAt desc""")
    List<NotaPublica> recentes(Long dailyAlbumId, Pageable pagina);
}
