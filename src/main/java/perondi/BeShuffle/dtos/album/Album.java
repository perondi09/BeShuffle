package perondi.BeShuffle.dtos.album;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private String id;
    private String name;
    @JsonProperty("release_date")
    private String releaseDate;
    private String href;
    private String uri;
    private List<AlbumImage> images;
    private List<Artist> artists;
}
