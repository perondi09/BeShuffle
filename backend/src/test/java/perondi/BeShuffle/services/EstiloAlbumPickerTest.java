package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;
import perondi.BeShuffle.spotify.SpotifyClient.ArtistRef;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Sorteio de álbuns por estilo musical")
class EstiloAlbumPickerTest {

    private SpotifyClient spotifyClient;
    private SorteioProperties props;
    private EstiloAlbumPicker picker;

    @BeforeEach
    void setUp() {
        spotifyClient = mock(SpotifyClient.class);
        props = SorteioProperties.padrao();
        picker = new EstiloAlbumPicker(spotifyClient, new FiltroDeAlbum(props), props, new Random(7));
    }

    private static AlbumSearchItem item(String id, String tipo, int faixas) {
        return new AlbumSearchItem(id, "Nome " + id, tipo, faixas, List.of(new ArtistRef("a-" + id, "Artista")));
    }

    @Test
    @DisplayName("Busca faixas genre:\"...\" dos estilos pedidos, em páginas de 10 entre os 100 primeiros")
    void buscaPorGeneroDosEstilosPedidos() {
        when(spotifyClient.searchTrackAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("ok", "album", 10)));

        for (int i = 0; i < 100; i++) {
            picker.pickAlbumId(List.of(Estilo.ROCK, Estilo.GOSPEL));
        }

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> offset = ArgumentCaptor.forClass(Integer.class);
        verify(spotifyClient, times(100)).searchTrackAlbums(query.capture(), eq("BR"), eq(10), offset.capture());

        assertEquals(Set.of("genre:\"rock\"", "genre:\"christian\""), new HashSet<>(query.getAllValues()),
                "deveria sortear entre os dois estilos pedidos");
        for (int o : offset.getAllValues()) {
            assertTrue(o >= 0 && o <= 90 && o % 10 == 0, "offset fora das 10 primeiras páginas: " + o);
        }
    }

    @Test
    @DisplayName("Só sorteia álbuns que o filtro aceita (singles e coletâneas ficam de fora)")
    void usaOFiltro() {
        when(spotifyClient.searchTrackAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("single", "single", 1), item("bom", "album", 12),
                        item("colet", "compilation", 20)));

        for (int i = 0; i < 20; i++) {
            assertEquals("bom", picker.pickAlbumId(List.of(Estilo.JAZZ)));
        }
    }

    @Test
    @DisplayName("Sem álbum aprovado depois das tentativas, devolve null")
    void desisteDepoisDasTentativas() {
        when(spotifyClient.searchTrackAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("single", "single", 1)))
                .thenThrow(new RuntimeException("Spotify fora"));

        assertNull(picker.pickAlbumId(List.of(Estilo.MPB)));
        verify(spotifyClient, times(props.tentativas()))
                .searchTrackAlbums(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Lista de estilos vazia é erro de programação")
    void exigeEstilo() {
        assertThrows(IllegalArgumentException.class, () -> picker.pickAlbumId(List.of()));
    }

    @Test
    @DisplayName("Estilo por artistas busca artist:\"...\" de um artista da lista, sem passar pela busca de gênero")
    void estiloPorArtistas() {
        // Devolve sempre um álbum do próprio artista buscado, para cada sorteio fazer uma busca só.
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(chamada -> {
            String query = chamada.getArgument(0);
            String artista = query.substring("artist:\"".length(), query.length() - 1);
            return List.of(new AlbumSearchItem("ok", "Disco", "album", 12, List.of(new ArtistRef("a", artista))));
        });

        for (int i = 0; i < 50; i++) {
            assertEquals("ok", picker.pickAlbumId(List.of(Estilo.SERTANEJO)));
        }

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        verify(spotifyClient, times(50)).searchAlbums(query.capture(), eq("BR"), eq(10), eq(0));
        Set<String> esperadas = new HashSet<>();
        Estilo.SERTANEJO.artistas().forEach(a -> esperadas.add("artist:\"" + a + "\""));
        assertTrue(esperadas.containsAll(query.getAllValues()), "só artistas da lista: " + query.getAllValues());
        verify(spotifyClient, times(0)).searchTrackAlbums(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Estilo por artistas só aceita álbuns em que o artista sorteado é um dos artistas")
    void soAlbunsDoArtista() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(chamada -> {
            String query = chamada.getArgument(0);
            String artista = query.substring("artist:\"".length(), query.length() - 1);
            return List.of(
                    new AlbumSearchItem("outro", "Participação", "album", 12, List.of(new ArtistRef("x", "Outra Banda"))),
                    new AlbumSearchItem("dele", "Disco Dele", "album", 12, List.of(new ArtistRef("y", artista.toUpperCase()))));
        });

        for (int i = 0; i < 20; i++) {
            assertEquals("dele", picker.pickAlbumId(List.of(Estilo.PAGODE)));
        }
    }

    @Test
    @DisplayName("Todo estilo é por gênero ou por artistas, e os ids são únicos")
    void catalogoConsistente() {
        Set<String> ids = new HashSet<>();
        for (Estilo e : Estilo.values()) {
            assertTrue(ids.add(e.id()), "id repetido: " + e.id());
            if (e.porArtistas()) {
                assertThrows(IllegalStateException.class, e::consulta);
            } else {
                assertTrue(e.consulta().startsWith("genre:\""), e.id());
            }
        }
        assertTrue(Estilo.FUNK.porArtistas() && Estilo.HIP_HOP.porArtistas() && !Estilo.ROCK.porArtistas());
    }
}
