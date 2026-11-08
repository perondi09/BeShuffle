package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sorteio atual: busca por um caractere aleatório em uma posição aleatória do catálogo
 * e fica com o primeiro resultado que seja um álbum completo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RandomQueryAlbumPicker implements AlbumPicker {

    private static final String SEARCH_CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_SEARCH_ATTEMPTS = 5;
    private static final int MAX_OFFSET = 950;
    private static final int PAGE_SIZE = 5;

    private final SpotifyClient spotifyClient;

    @Override
    public String pickAlbumId() {
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
            String query = gerarQueryAleatoria();
            int offset = ThreadLocalRandom.current().nextInt(MAX_OFFSET);
            List<AlbumSearchItem> items = spotifyClient.searchAlbums(query, PAGE_SIZE, offset);

            for (AlbumSearchItem item : items) {
                if ("album".equalsIgnoreCase(item.albumType()) && item.id() != null && !item.id().isBlank()) {
                    log.debug("Álbum aleatório selecionado: {} ({})", item.name(), item.id());
                    return item.id();
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
