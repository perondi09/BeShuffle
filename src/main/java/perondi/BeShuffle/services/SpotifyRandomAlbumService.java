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

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class SpotifyRandomAlbumService {

    private static final String SEARCH_CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_SEARCH_ATTEMPTS = 5;
    private static final int MAX_OFFSET = 950;

    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SpotifyRandomAlbumService(
            AuthService authService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.authService = authService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getRandomAlbumIdFromSpotify() {
        log.info("Buscando álbum aleatório no Spotify...");

        for (int attempt = 1; attempt <= MAX_SEARCH_ATTEMPTS; attempt++) {
            String albumId = buscarAlbumAleatorio();
            if (albumId != null && !albumId.isBlank()) {
                log.info("Álbum aleatório encontrado na tentativa {}: {}", attempt, albumId);
                return albumId;
            }
            log.warn("Nenhum álbum válido encontrado na tentativa {} de {}", attempt, MAX_SEARCH_ATTEMPTS);
        }

        log.error("Não foi possível encontrar um álbum aleatório após {} tentativas", MAX_SEARCH_ATTEMPTS);
        return null;
    }

    private String buscarAlbumAleatorio() {
        try {
            String token = authService.getAccessToken();
            if (token == null || token.isBlank()) {
                log.warn("Token de autenticação inválido ao buscar álbum aleatório");
                return null;
            }

            String query = gerarQueryAleatoria();
            int offset = ThreadLocalRandom.current().nextInt(MAX_OFFSET);
            String url = String.format(
                    "https://api.spotify.com/v1/search?q=%s&type=album&limit=5&offset=%d",
                    query,
                    offset
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Resposta inválida da API do Spotify ao buscar álbum aleatório: {}", response.getStatusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("albums").path("items");
            if (!items.isArray() || items.isEmpty()) {
                return null;
            }

            for (JsonNode item : items) {
                if (!"album".equalsIgnoreCase(item.path("album_type").asText())) {
                    continue;
                }

                String albumId = item.path("id").asText(null);
                String albumName = item.path("name").asText("desconhecido");
                if (albumId != null && !albumId.isBlank()) {
                    log.debug("Álbum aleatório selecionado: {} ({})", albumName, albumId);
                    return albumId;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar álbum aleatório no Spotify", e);
            return null;
        }
    }

    private String gerarQueryAleatoria() {
        return String.valueOf(SEARCH_CHARSET.charAt(ThreadLocalRandom.current().nextInt(SEARCH_CHARSET.length())));
    }
}