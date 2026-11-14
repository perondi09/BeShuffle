package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Sorteio por estilo musical: escolhe um dos estilos pedidos e sorteia um álbum que o {@link FiltroDeAlbum}
 * aceita. Estilo por gênero: busca faixas {@code genre:"..."} numa página aleatória entre as mais relevantes.
 * Estilo por artistas: sorteia um artista da lista e busca os álbuns dele ({@code artist:"..."}).
 */
@Slf4j
@Service
public class EstiloAlbumPicker {

    /** Páginas do começo da busca que entram no sorteio (mais páginas que no sorteio por ano: menos repetição). */
    static final int PAGINAS = 10;

    private final SpotifyClient spotifyClient;
    private final FiltroDeAlbum filtro;
    private final SorteioProperties props;
    private final RandomGenerator random;

    public EstiloAlbumPicker(SpotifyClient spotifyClient, FiltroDeAlbum filtro, SorteioProperties props,
                             RandomGenerator random) {
        this.spotifyClient = spotifyClient;
        this.filtro = filtro;
        this.props = props;
        this.random = random;
    }

    /** @return o id de um álbum de um dos estilos, ou {@code null} se nada foi aprovado */
    public String pickAlbumId(List<Estilo> estilos) {
        if (estilos == null || estilos.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos um estilo");
        }
        for (int tentativa = 1; tentativa <= props.tentativas(); tentativa++) {
            Estilo estilo = estilos.get(random.nextInt(estilos.size()));
            try {
                List<AlbumSearchItem> candidatos = estilo.porArtistas() ? deUmArtista(estilo) : deUmGenero(estilo);
                if (!candidatos.isEmpty()) {
                    AlbumSearchItem escolhido = candidatos.get(random.nextInt(candidatos.size()));
                    log.info("Álbum sorteado por estilo {}: {} ({}), tentativa {}",
                            estilo.id(), escolhido.name(), escolhido.id(), tentativa);
                    return escolhido.id();
                }
                log.info("Nenhum álbum aprovado no estilo {} (tentativa {} de {})",
                        estilo.id(), tentativa, props.tentativas());
            } catch (Exception e) {
                log.warn("Erro ao buscar o estilo {} no Spotify (tentativa {} de {})",
                        estilo.id(), tentativa, props.tentativas(), e);
            }
        }
        log.error("Nenhum álbum sorteado nos estilos {} depois de {} tentativas", estilos, props.tentativas());
        return null;
    }

    /** Faixas do gênero numa página aleatória entre as mais relevantes; devolve os álbuns aceitos. */
    private List<AlbumSearchItem> deUmGenero(Estilo estilo) {
        int offset = random.nextInt(PAGINAS) * props.tamanhoPagina();
        return spotifyClient.searchTrackAlbums(estilo.consulta(), props.mercado(), props.tamanhoPagina(), offset)
                .stream()
                .filter(filtro::aceita)
                .toList();
    }

    /**
     * Álbuns de um artista sorteado da lista do estilo. Só valem álbuns em que ele é um dos artistas: a busca
     * {@code artist:"..."} também traz discos de quem só participou ou tem nome parecido.
     */
    private List<AlbumSearchItem> deUmArtista(Estilo estilo) {
        String artista = estilo.artistas().get(random.nextInt(estilo.artistas().size()));
        return spotifyClient.searchAlbums("artist:\"" + artista + "\"", props.mercado(), props.tamanhoPagina(), 0)
                .stream()
                .filter(filtro::aceita)
                .filter(album -> ehDoArtista(album, artista))
                .toList();
    }

    private static boolean ehDoArtista(AlbumSearchItem album, String artista) {
        return album.artists() != null && album.artists().stream()
                .anyMatch(a -> a != null && a.name() != null && a.name().equalsIgnoreCase(artista));
    }
}
