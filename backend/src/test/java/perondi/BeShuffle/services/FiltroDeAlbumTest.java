package perondi.BeShuffle.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;
import perondi.BeShuffle.spotify.SpotifyClient.ArtistRef;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro de álbuns que entram no sorteio")
class FiltroDeAlbumTest {

    private final FiltroDeAlbum filtro = new FiltroDeAlbum(SorteioProperties.padrao());

    private static AlbumSearchItem album(String nome, String tipo, Integer faixas, String... artistas) {
        List<ArtistRef> refs = Arrays.stream(artistas).map(n -> new ArtistRef("id-" + n, n)).toList();
        return new AlbumSearchItem("id-" + nome, nome, tipo, faixas, refs);
    }

    @Test
    @DisplayName("Aceita um álbum de estúdio comum")
    void aceitaAlbumComum() {
        assertTrue(filtro.aceita(album("A Night At The Opera", "album", 12, "Queen")));
        assertTrue(filtro.aceita(album("Sobrevivendo no Inferno", "album", 12, "Racionais MC's")));
    }

    @ParameterizedTest(name = "recusa o tipo {0}")
    @ValueSource(strings = {"single", "compilation", "appears_on"})
    @DisplayName("Recusa singles, EPs e coletâneas do Spotify")
    void recusaTiposQueNaoSaoAlbum(String tipo) {
        assertFalse(filtro.aceita(album("Algo", tipo, 12, "Artista")));
    }

    @Test
    @DisplayName("Recusa álbuns curtos demais, longos demais ou sem número de faixas")
    void recusaPorNumeroDeFaixas() {
        assertFalse(filtro.aceita(album("Curto", "album", 5, "Artista")));
        assertTrue(filtro.aceita(album("No limite de baixo", "album", 6, "Artista")));
        assertTrue(filtro.aceita(album("No limite de cima", "album", 40, "Artista")));
        assertFalse(filtro.aceita(album("Caixa enorme", "album", 41, "Artista")));
        assertFalse(filtro.aceita(album("Sem contagem", "album", null, "Artista")));
    }

    @ParameterizedTest(name = "recusa artista \"{0}\"")
    @ValueSource(strings = {"Various Artists", "VARIOUS ARTISTS", "Vários Artistas", "Varios Artistas"})
    @DisplayName("Recusa álbuns de vários artistas")
    void recusaVariosArtistas(String artista) {
        assertFalse(filtro.aceita(album("Mix", "album", 12, artista)));
    }

    @ParameterizedTest(name = "recusa \"{0}\"")
    @ValueSource(strings = {
            "Canções de Ninar - Musicas para Bebe Dormir",
            "The Greatest Hits Collection",
            "20 Mejores Canciones",
            "Karaokê Sertanejo",
            "A Tribute to Queen",
            "Grandes Sucessos",
            "Música Infantil para Crianças",
            "Coletânea Romântica"
    })
    @DisplayName("Recusa nomes com termos bloqueados, sem diferenciar acento ou maiúscula")
    void recusaTermosBloqueados(String nome) {
        assertFalse(filtro.aceita(album(nome, "album", 12, "Artista")));
    }

    @Test
    @DisplayName("Também olha o nome do artista")
    void recusaTermoNoArtista() {
        assertFalse(filtro.aceita(album("Volume 1", "album", 12, "Turma Infantil")));
    }

    @Test
    @DisplayName("Só bloqueia a palavra inteira, não pedaços de outras palavras")
    void naoBloqueiaPedacoDePalavra() {
        assertTrue(filtro.aceita(album("Kidsmoke", "album", 10, "Artista")));
        assertTrue(filtro.aceita(album("Hitsville", "album", 10, "Artista")));
    }

    @Test
    @DisplayName("Recusa item sem id")
    void recusaSemId() {
        assertFalse(filtro.aceita(new AlbumSearchItem(null, "Sem id", "album", 10, List.of())));
    }
}
