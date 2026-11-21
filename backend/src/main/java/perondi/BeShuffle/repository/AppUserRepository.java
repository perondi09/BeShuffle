package perondi.BeShuffle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import perondi.BeShuffle.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Usado a cada requisição logada pelo filtro que derruba a sessão de conta banida. */
    boolean existsByIdAndBannedAtIsNotNull(Long id);
}
