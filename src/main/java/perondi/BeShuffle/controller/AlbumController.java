package perondi.BeShuffle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.album.Album;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.services.AlbumService;
import perondi.BeShuffle.services.SpotifyRandomAlbumService;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final SpotifyRandomAlbumService spotifyRandomAlbumService;

    public AlbumController(AlbumService albumService, SpotifyRandomAlbumService spotifyRandomAlbumService) {
        this.albumService = albumService;
        this.spotifyRandomAlbumService = spotifyRandomAlbumService;
    }

    @PostMapping("/random")
    public ResponseEntity<Album> getRandomAlbum() {
        String randomAlbumId = spotifyRandomAlbumService.getRandomAlbumIdFromSpotify();
        if (randomAlbumId == null) {
            throw new SpotifyApiException("Não foi possível obter um álbum aleatório do Spotify", 503);
        }

        Album album = albumService.getAlbumById(randomAlbumId);
        return ResponseEntity.ok(album);
    }
}