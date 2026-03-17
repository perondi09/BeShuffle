package perondi.BeShuffle.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String spotifyAlbumId;

    @Column(nullable = false)
    private String albumName;

    @Column(nullable = false)
    private String artistName;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private String albumUrl;
    private String releaseDate;

    @Column(nullable = false, unique = true)
    private LocalDate displayDate;

    @Column(columnDefinition = "TEXT")
    private String fullAlbumJson;
}
