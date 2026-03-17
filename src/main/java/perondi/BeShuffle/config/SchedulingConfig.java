package perondi.BeShuffle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Cria um bean de ObjectMapper para serialização/deserialização JSON
     * Necessário para SpotifyRandomAlbumService
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Cria um bean de RestTemplate para fazer requisições HTTP
     * Necessário para chamar a API do Spotify
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}