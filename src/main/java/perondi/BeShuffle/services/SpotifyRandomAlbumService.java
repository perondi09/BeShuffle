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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
@Service
public class SpotifyRandomAlbumService {

    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random;

    // Palavras-chave para busca aleatória
    private static final String[] SEARCH_KEYWORDS = {
            "rock", "pop", "jazz", "hip", "country", "reggae",
            "electronic", "indie", "metal", "blues", "classical", "soul",
            "rb", "folk", "punk", "disco", "funk", "gospel", "latin",
            "techno", "house", "ambient", "experimental", "grunge", "alternative"
    };

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

    /**
     * Busca um álbum COMPLETAMENTE ALEATÓRIO da API do Spotify
     * Sem usar nenhuma lista pré-definida
     * Cada execução pode retornar um álbum diferente
     */
    public String getRandomAlbumIdFromSpotify() {
        try {
            log.info("🎲 Buscando álbum aleatório da API do Spotify...");

            // 1. Selecionar palavra-chave aleatória
            String randomKeyword = getRandomKeyword();
            log.info("🔍 Termo de busca: {}", randomKeyword);

            // 2. Buscar álbuns com essa palavra-chave
            String albumId = searchRandomAlbum(randomKeyword);

            if (albumId != null) {
                log.info("✅ Álbum aleatório encontrado: {}", albumId);
                return albumId;
            } else {
                // Se falhar, tenta outra palavra-chave
                log.warn("⚠️ Nenhum álbum encontrado, tentando outro termo...");
                return retryWithDifferentKeyword();
            }

        } catch (Exception e) {
            log.error("❌ Erro ao buscar álbum aleatório: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Busca álbuns usando um termo e retorna um aleatório
     */
    private String searchRandomAlbum(String keyword) {
        try {
            String token = authService.getAccessToken();

            // Buscar álbuns com o termo (aleatoriamente pega offset diferente)
            int offset = random.nextInt(3000);

            // Codificar a keyword corretamente
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // Construir URL simples (sem UriComponentsBuilder)
            String url = "https://api.spotify.com/v1/search?" +
                    "q=" + encodedKeyword +
                    "&type=album" +
                    "&limit=10" +
                    "&offset=" + offset;

            log.debug("URL da busca: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.debug("Status da resposta: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode albums = root.get("albums");

                if (albums != null) {
                    JsonNode items = albums.get("items");

                    if (items != null && items.isArray() && items.size() > 0) {
                        // Selecionar um álbum aleatório dos resultados
                        int randomIndex = random.nextInt(items.size());
                        JsonNode album = items.get(randomIndex);

                        String albumId = album.get("id").asText();
                        String albumName = album.get("name").asText();

                        log.info("📀 Álbum selecionado: {} (ID: {})", albumName, albumId);
                        return albumId;
                    } else {
                        log.warn("⚠️ Items vazio ou não encontrado");
                    }
                } else {
                    log.warn("⚠️ Albums não encontrado na resposta");
                }
            } else {
                log.warn("⚠️ Status não 2xx: {}", response.getStatusCode());
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar álbum: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tenta com outra palavra-chave aleatória
     */
    private String retryWithDifferentKeyword() {
        try {
            String differentKeyword = getRandomKeyword();
            log.info("🔄 Segunda tentativa com termo: {}", differentKeyword);

            String albumId = searchRandomAlbum(differentKeyword);

            if (albumId != null) {
                return albumId;
            }

            // Tenta uma terceira vez
            log.warn("⚠️ Segunda tentativa falhou, tentando novamente...");
            String thirdKeyword = getRandomKeyword();
            return searchRandomAlbum(thirdKeyword);

        } catch (Exception e) {
            log.error("❌ Erro na tentativa de busca: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Retorna uma palavra-chave aleatória
     */
    private String getRandomKeyword() {
        int randomIndex = random.nextInt(SEARCH_KEYWORDS.length);
        return SEARCH_KEYWORDS[randomIndex];
    }
}