package perondi.BeShuffle.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.entity.Album;

import java.util.List;

/**
 * Único ponto de acesso à Web API do Spotify. Cada chamada envia o token de aplicação
 * fornecido pelo {@link SpotifyTokenProvider}.
 */
@Component
public class SpotifyClient {

    private final RestClient apiClient;
    private final SpotifyTokenProvider tokenProvider;

    public SpotifyClient(
            @Qualifier("spotifyApiRestClient") RestClient apiClient,
            SpotifyTokenProvider tokenProvider
    ) {
        this.apiClient = apiClient;
        this.tokenProvider = tokenProvider;
    }

    public Album getAlbum(String albumId) {
        return apiClient.get()
                .uri("/v1/albums/{id}", albumId)
                .headers(headers -> headers.setBearerAuth(tokenProvider.getAccessToken()))
                .retrieve()
                .body(Album.class);
    }

    public List<AlbumSearchItem> searchAlbums(String query, int limit, int offset) {
        SearchResponse response = apiClient.get()
                .uri(uri -> uri.path("/v1/search")
                        .queryParam("q", query)
                        .queryParam("type", "album")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .headers(headers -> headers.setBearerAuth(tokenProvider.getAccessToken()))
                .retrieve()
                .body(SearchResponse.class);

        if (response == null || response.albums() == null || response.albums().items() == null) {
            return List.of();
        }
        return response.albums().items();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AlbumSearchItem(
            String id,
            String name,
            @JsonProperty("album_type") String albumType
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResponse(AlbumPage albums) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AlbumPage(List<AlbumSearchItem> items) {
    }
}
