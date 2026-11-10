package perondi.BeShuffle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;

import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Sorteia álbuns que as pessoas realmente ouvem.
 *
 * <p>Sorteia um ano (de {@code anoInicial} até o ano atual) e uma página entre os primeiros resultados de
 * {@code year:ANO} no mercado configurado. Como o Spotify ordena a busca por relevância, essas páginas trazem
 * os álbuns mais ouvidos de cada ano. Depois descarta o que o {@link FiltroDeAlbum} recusa (singles,
 * coletâneas, infantil, karaokê...) e sorteia um dos que sobraram. Se nada sobrar, tenta outro ano.
 */
@Slf4j
@Service
public class PopularPorAnoAlbumPicker implements AlbumPicker {

    private final SpotifyClient spotifyClient;
    private final FiltroDeAlbum filtro;
    private final SorteioProperties props;
    private final Clock clock;
    private final RandomGenerator random;

    public PopularPorAnoAlbumPicker(SpotifyClient spotifyClient, FiltroDeAlbum filtro, SorteioProperties props,
                                    Clock clock, RandomGenerator random) {
        this.spotifyClient = spotifyClient;
        this.filtro = filtro;
        this.props = props;
        this.clock = clock;
        this.random = random;
    }

    @Override
    public String pickAlbumId() {
        for (int tentativa = 1; tentativa <= props.tentativas(); tentativa++) {
            int ano = sortearAno();
            int offset = random.nextInt(props.paginas()) * props.tamanhoPagina();
            try {
                List<AlbumSearchItem> candidatos = spotifyClient
                        .searchAlbums("year:" + ano, props.mercado(), props.tamanhoPagina(), offset).stream()
                        .filter(filtro::aceita)
                        .toList();
                if (!candidatos.isEmpty()) {
                    AlbumSearchItem escolhido = candidatos.get(random.nextInt(candidatos.size()));
                    log.info("Álbum sorteado: {} ({}), ano {}, offset {}, tentativa {}",
                            escolhido.name(), escolhido.id(), ano, offset, tentativa);
                    return escolhido.id();
                }
                log.info("Nenhum álbum aprovado em year:{} offset {} (tentativa {} de {})",
                        ano, offset, tentativa, props.tentativas());
            } catch (Exception e) {
                log.warn("Erro ao buscar álbuns de {} no Spotify (tentativa {} de {})",
                        ano, tentativa, props.tentativas(), e);
            }
        }
        log.error("Nenhum álbum sorteado depois de {} tentativas", props.tentativas());
        return null;
    }

    private int sortearAno() {
        int anoAtual = Year.now(clock).getValue();
        int inicio = Math.min(props.anoInicial(), anoAtual);
        return inicio + random.nextInt(anoAtual - inicio + 1);
    }
}
