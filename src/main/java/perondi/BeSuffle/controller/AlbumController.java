
package perondi.BeSuffle.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeSuffle.client.Album;
import perondi.BeSuffle.client.AlbumSpotifyClient;
import perondi.BeSuffle.client.AuthSpotifyClient;
import perondi.BeSuffle.client.LoginRequest;

@RestController
@RequestMapping("/spotify/api")
public class AlbumController {

    private final AuthSpotifyClient authSpotifyClient;
    private final AlbumSpotifyClient albumSpotifyClient;

    public AlbumController(AuthSpotifyClient authSpotifyClient,
                           AlbumSpotifyClient albumSpotifyClient) {
        this.authSpotifyClient = authSpotifyClient;
        this.albumSpotifyClient = albumSpotifyClient;
    }

    @GetMapping("/albums")
    public ResponseEntity<Album> getAlbum(@RequestParam("id") String id) {

        var request = new LoginRequest(
                "client_credentials",
                "13ad5a1231574ce49226682d2303b947",
                "b43ce3f49b1841d89b1ddaa75670bbc5"
        );
        var token = authSpotifyClient.login(request).getAccessToken();

        var response = albumSpotifyClient.getAlbum("Bearer " + token, id);

        return ResponseEntity.ok(response);
    }
}
