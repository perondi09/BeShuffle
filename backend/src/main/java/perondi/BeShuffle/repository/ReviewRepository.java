package perondi.BeShuffle.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import perondi.BeShuffle.dtos.perfil.ReviewDoPerfil;
import org.springframework.data.jpa.repository.Query;
import perondi.BeShuffle.dtos.reviews.NotaPublica;
import perondi.BeShuffle.dtos.reviews.ResumoNotas;
import perondi.BeShuffle.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndDailyAlbumId(Long userId, Long dailyAlbumId);

    @Query("""
            select new perondi.BeShuffle.dtos.reviews.ResumoNotas(avg(r.ratingX2 * 1.0) / 2.0, count(r))
            from Review r where r.dailyAlbumId = :dailyAlbumId""")
    ResumoNotas resumo(Long dailyAlbumId);

    /** Média (em estrelas) e total das notas de uma conta, para o perfil. */
    @Query("""
            select new perondi.BeShuffle.dtos.reviews.ResumoNotas(avg(r.ratingX2 * 1.0) / 2.0, count(r))
            from Review r where r.userId = :userId""")
    ResumoNotas resumoDaConta(Long userId);

    /** As notas de uma conta com o álbum de cada uma, do álbum mais recente para o mais antigo (diário do perfil). */
    @Query("""
            select new perondi.BeShuffle.dtos.perfil.ReviewDoPerfil(
                a.spotifyAlbumId, a.albumName, a.artistName, a.imageUrl, a.displayDate,
                r.ratingX2 / 2.0, r.comment, r.updatedAt)
            from Review r join DailyAlbum a on a.id = r.dailyAlbumId
            where r.userId = :userId
            order by a.displayDate desc""")
    List<ReviewDoPerfil> doPerfil(Long userId, Pageable pagina);

    /** Notas mais recentes de um álbum, com o nome, o nick e o avatar de quem avaliou. */
    @Query("""
            select new perondi.BeShuffle.dtos.reviews.NotaPublica(
                u.displayName, u.handle, u.avatarKey, r.ratingX2 / 2.0, r.comment, r.updatedAt)
            from Review r join AppUser u on u.id = r.userId
            where r.dailyAlbumId = :dailyAlbumId
            order by r.updatedAt desc""")
    List<NotaPublica> recentes(Long dailyAlbumId, Pageable pagina);
}
