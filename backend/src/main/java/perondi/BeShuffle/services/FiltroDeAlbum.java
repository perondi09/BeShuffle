package perondi.BeShuffle.services;

import org.springframework.stereotype.Component;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;
import perondi.BeShuffle.spotify.SpotifyClient.ArtistRef;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Decide se um resultado da busca do Spotify é um álbum que vale sortear: álbum de verdade (não single,
 * EP ou coletânea), com um número normal de faixas, de um artista (não "Various Artists") e sem termos
 * bloqueados no nome do álbum ou do artista.
 */
@Component
public class FiltroDeAlbum {

    private static final Set<String> VARIOS_ARTISTAS = Set.of("various artists", "varios artistas");

    private final SorteioProperties props;
    private final List<Pattern> termosBloqueados;

    public FiltroDeAlbum(SorteioProperties props) {
        this.props = props;
        // Palavra inteira: "kids" bloqueia "Kids Songs", mas não "Kidsmoke".
        this.termosBloqueados = props.termosBloqueados().stream()
                .map(FiltroDeAlbum::normalizar)
                .filter(t -> !t.isBlank())
                .map(t -> Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(t) + "(?![\\p{L}\\p{N}])"))
                .toList();
    }

    public boolean aceita(AlbumSearchItem item) {
        if (item == null || item.id() == null || item.id().isBlank()) {
            return false;
        }
        if (!"album".equalsIgnoreCase(item.albumType())) {
            return false;
        }
        Integer faixas = item.totalTracks();
        if (faixas == null || faixas < props.minFaixas() || faixas > props.maxFaixas()) {
            return false;
        }
        List<String> artistas = item.artists() == null ? List.of()
                : item.artists().stream().map(ArtistRef::name).filter(n -> n != null && !n.isBlank()).toList();
        if (artistas.stream().map(FiltroDeAlbum::normalizar).anyMatch(VARIOS_ARTISTAS::contains)) {
            return false;
        }
        return Stream.concat(Stream.ofNullable(item.name()), artistas.stream())
                .map(FiltroDeAlbum::normalizar)
                .noneMatch(texto -> termosBloqueados.stream().anyMatch(p -> p.matcher(texto).find()));
    }

    /** Minúsculas e sem acento, para "Karaokê" e "karaoke" serem a mesma coisa. */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
