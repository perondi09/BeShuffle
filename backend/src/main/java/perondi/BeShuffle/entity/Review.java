package perondi.BeShuffle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Nota (1 a 5) e comentário de uma conta para um Álbum do Dia. Uma por conta e álbum. */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_album_id", nullable = false)
    private Long dailyAlbumId;

    /** Nota em meias estrelas, de 1 a 10 (2,5 estrelas = 5). A API fala em estrelas; o banco guarda o dobro. */
    @Column(name = "rating_x2", nullable = false)
    private Integer ratingX2;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
