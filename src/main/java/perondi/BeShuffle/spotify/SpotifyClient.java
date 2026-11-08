package perondi.BeShuffle.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import perondi.BeShuffle.entity.Album;

import java.util.List;
import java.util.function.Function;

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
        return withToken(token -> apiClient.get()
                .uri("/v1/albums/{id}", albumId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(Album.class));
    }

    public List<AlbumSearchItem> searchAlbums(String query, int limit, int offset) {
        SearchResponse response = withToken(token -> apiClient.get()
                .uri(uri -> uri.path("/v1/search")
                        .queryParam("q", query)
                        .queryParam("type", "album")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(SearchResponse.class));

        if (response == null || response.albums() == null || response.albums().items() == null) {
            return List.of();
        }
        return response.albums().items();
    }

    /**
     * Executa a chamada com o token em cache. Se o Spotify recusar o token (401, ex.: revogado),
     * descarta o cache e tenta uma única vez com um token novo.
     */
    private <T> T withToken(Function<String, T> call) {
        try {
            return call.apply(tokenProvider.getAccessToken());
        } catch (HttpClientErrorException.Unauthorized e) {
            tokenProvider.invalidate();
            return call.apply(tokenProvider.getAccessToken());
        }
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
