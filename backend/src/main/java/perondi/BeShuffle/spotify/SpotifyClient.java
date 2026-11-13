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

    /**
     * Busca álbuns. O Spotify ordena por relevância, então os primeiros resultados são os mais ouvidos.
     *
     * @param market código do país (ex.: "BR"); vazio ou nulo para não filtrar por mercado
     * @param limit  no Development Mode o Spotify aceita no máximo 10
     */
    public List<AlbumSearchItem> searchAlbums(String query, String market, int limit, int offset) {
        SearchResponse response = withToken(token -> apiClient.get()
                .uri(uri -> {
                    uri.path("/v1/search")
                            .queryParam("q", query)
                            .queryParam("type", "album");
                    if (market != null && !market.isBlank()) {
                        uri.queryParam("market", market);
                    }
                    return uri.queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .build();
                })
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(SearchResponse.class));

        if (response == null || response.albums() == null || response.albums().items() == null) {
            return List.of();
        }
        return response.albums().items();
    }

    /**
     * Busca faixas e devolve o álbum de cada uma. Serve para filtrar por gênero ({@code genre:"rock"}),
     * que o Spotify só aceita na busca de faixas e artistas, não na de álbuns.
     */
    public List<AlbumSearchItem> searchTrackAlbums(String query, String market, int limit, int offset) {
        TrackSearchResponse response = withToken(token -> apiClient.get()
                .uri(uri -> {
                    uri.path("/v1/search")
                            .queryParam("q", query)
                            .queryParam("type", "track");
                    if (market != null && !market.isBlank()) {
                        uri.queryParam("market", market);
                    }
                    return uri.queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .build();
                })
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(TrackSearchResponse.class));

        if (response == null || response.tracks() == null || response.tracks().items() == null) {
            return List.of();
        }
        return response.tracks().items().stream()
                .filter(track -> track != null && track.album() != null)
                .map(TrackItem::album)
                .toList();
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
            @JsonProperty("album_type") String albumType,
            @JsonProperty("total_tracks") Integer totalTracks,
            List<ArtistRef> artists
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ArtistRef(String id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResponse(AlbumPage albums) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AlbumPage(List<AlbumSearchItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrackSearchResponse(TrackPage tracks) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrackPage(List<TrackItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrackItem(AlbumSearchItem album) {
    }
}
