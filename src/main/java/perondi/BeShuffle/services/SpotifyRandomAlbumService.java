package perondi.BeShuffle.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.exceptions.SpotifyAuthenticationException;

import java.util.Random;

@Slf4j
@Service
public class SpotifyRandomAlbumService {

    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random;

    public SpotifyRandomAlbumService(
            AuthService authService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.authService = authService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.random = new Random();
    }

    public String getRandomAlbumIdFromSpotify() {
        try {
            log.info("🎲 Buscando álbum totalmente aleatório...");

            String albumId = searchRandomAlbum();

            if (albumId != null) {
                log.info("✅ Álbum aleatório encontrado: {}", albumId);
                return albumId;
            } else {
                log.warn("⚠️ Nenhum álbum encontrado, tentando novamente...");
                return retrySearch();
            }

        } catch (Exception e) {
            log.error("❌ Erro ao buscar álbum aleatório: {}", e.getMessage());
            return null;
        }
    }

    private String searchRandomAlbum() {
        try {
            String token = authService.getAccessToken();

            String query = getRandomQuery();
            int offset = random.nextInt(950);  // 0 a 949 (SEGURO)

            log.debug("🔍 Query: {}, Offset: {}", query, offset);

            String url = "https://api.spotify.com/v1/search?" +
                    "q=" + query +
                    "&type=album" +
                    "&limit=5" +
                    "&offset=" + offset;

            log.debug("URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode albums = root.get("albums");

                if (albums != null) {
                    JsonNode items = albums.get("items");

                    if (items != null && items.isArray() && items.size() > 0) {
                        int randomIndex = random.nextInt(items.size());
                        JsonNode album = items.get(randomIndex);

                        String albumId = album.get("id").asText();
                        String albumName = album.get("name").asText();
                        String albumType = album.get("album_type").asText();

                        log.info("📀 Álbum encontrado: {} (Tipo: {})", albumName, albumType);

                        if ("album".equalsIgnoreCase(albumType)) {
                            return albumId;
                        } else {
                            log.warn("⚠️ Resultado não é álbum: {}", albumType);
                            return null;
                        }
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar: {}", e.getMessage());
            return null;
        }
    }
    private String retrySearch() {
        try {
            log.info("🔄 Tentando novamente...");

            // Tenta 5 vezes com queries diferentes
            for (int i = 0; i < 5; i++) {
                String albumId = searchRandomAlbum();
                if (albumId != null) {
                    return albumId;
                }
                log.warn("⚠️ Tentativa {} falhou", i + 1);
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Erro na tentativa: {}", e.getMessage());
            return null;
        }
    }

    private String getRandomQuery() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        char randomChar = chars[random.nextInt(chars.length)];

        return String.valueOf(randomChar);
    }
}