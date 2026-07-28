package perondi.BeShuffle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import perondi.BeShuffle.entity.DailyAlbum;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyAlbumRepository extends JpaRepository<DailyAlbum, Long> {
    Optional<DailyAlbum> findByDisplayDate(LocalDate date);
}
