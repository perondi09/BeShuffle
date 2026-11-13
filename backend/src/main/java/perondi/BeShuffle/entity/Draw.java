package perondi.BeShuffle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** Um sorteio feito no site. Tem dono: um usuário (userId) ou um visitante (anonId do cookie bs_anon). */
@Entity
@Table(name = "draws")
@Getter
@Setter
@NoArgsConstructor
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "anon_id", length = 36)
    private String anonId;

    @Column(name = "spotify_album_id", nullable = false, length = 64)
    private String spotifyAlbumId;

    @Column(name = "album_name", nullable = false)
    private String albumName;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
