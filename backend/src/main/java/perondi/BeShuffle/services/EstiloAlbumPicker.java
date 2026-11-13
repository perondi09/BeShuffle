package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Sorteio por estilo musical: escolhe um dos estilos pedidos, busca faixas daquele gênero numa página
 * aleatória entre as mais relevantes e sorteia um dos álbuns dessas faixas que o {@link FiltroDeAlbum} aceita.
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
            int offset = random.nextInt(PAGINAS) * props.tamanhoPagina();
            try {
                List<AlbumSearchItem> candidatos = spotifyClient
                        .searchTrackAlbums(estilo.consulta(), props.mercado(), props.tamanhoPagina(), offset).stream()
                        .filter(filtro::aceita)
                        .toList();
                if (!candidatos.isEmpty()) {
                    AlbumSearchItem escolhido = candidatos.get(random.nextInt(candidatos.size()));
                    log.info("Álbum sorteado por estilo {}: {} ({}), offset {}, tentativa {}",
                            estilo.id(), escolhido.name(), escolhido.id(), offset, tentativa);
                    return escolhido.id();
                }
                log.info("Nenhum álbum aprovado no estilo {} offset {} (tentativa {} de {})",
                        estilo.id(), offset, tentativa, props.tentativas());
            } catch (Exception e) {
                log.warn("Erro ao buscar o estilo {} no Spotify (tentativa {} de {})",
                        estilo.id(), tentativa, props.tentativas(), e);
            }
        }
        log.error("Nenhum álbum sorteado nos estilos {} depois de {} tentativas", estilos, props.tentativas());
        return null;
    }
}
