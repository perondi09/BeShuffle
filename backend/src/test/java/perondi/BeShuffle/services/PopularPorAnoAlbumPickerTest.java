package perondi.BeShuffle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import perondi.BeShuffle.config.SorteioProperties;
import perondi.BeShuffle.spotify.SpotifyClient;
import perondi.BeShuffle.spotify.SpotifyClient.AlbumSearchItem;
import perondi.BeShuffle.spotify.SpotifyClient.ArtistRef;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Sorteio de álbuns populares por ano")
class PopularPorAnoAlbumPickerTest {

    // 15/03/2026 em São Paulo
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private SpotifyClient spotifyClient;
    private SorteioProperties props;
    private PopularPorAnoAlbumPicker picker;

    @BeforeEach
    void setUp() {
        spotifyClient = mock(SpotifyClient.class);
        props = SorteioProperties.padrao();
        picker = new PopularPorAnoAlbumPicker(spotifyClient, new FiltroDeAlbum(props), props, CLOCK, new Random(42));
    }

    private static AlbumSearchItem item(String id, String tipo, int faixas, String artista) {
        return new AlbumSearchItem(id, "Nome " + id, tipo, faixas, List.of(new ArtistRef("a-" + id, artista)));
    }

    @Test
    @DisplayName("Busca year:ANO no mercado BR, em páginas de 10 entre os 50 primeiros resultados")
    void buscaPorAnoNoComecoDosResultados() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("ok", "album", 12, "Artista")));

        for (int i = 0; i < 200; i++) {
            picker.pickAlbumId();
        }

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> offset = ArgumentCaptor.forClass(Integer.class);
        verify(spotifyClient, times(200)).searchAlbums(query.capture(), eq("BR"), eq(10), offset.capture());

        for (String q : query.getAllValues()) {
            assertTrue(q.matches("year:\\d{4}"), q);
            int ano = Integer.parseInt(q.substring(5));
            assertTrue(ano >= 1960 && ano <= 2026, "ano fora da faixa: " + ano);
        }
        assertTrue(offset.getAllValues().stream().allMatch(o -> o % 10 == 0 && o >= 0 && o <= 40), offset.getAllValues().toString());
        // Com 200 sorteios, os extremos da faixa e todas as páginas aparecem
        assertTrue(query.getAllValues().stream().distinct().count() > 30, "anos pouco variados");
        assertEquals(Set.of(0, 10, 20, 30, 40), Set.copyOf(offset.getAllValues()));
    }

    @Test
    @DisplayName("Devolve só álbuns aprovados pelo filtro, sorteando entre eles")
    void devolveSoAprovados() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt())).thenReturn(List.of(
                item("single", "single", 1, "Artista"),
                item("coletanea", "album", 30, "Various Artists"),
                item("bom1", "album", 12, "Queen"),
                item("bom2", "album", 10, "Pink Floyd")));

        Set<String> sorteados = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) {
            sorteados.add(picker.pickAlbumId());
        }

        assertEquals(Set.of("bom1", "bom2"), sorteados);
    }

    @Test
    @DisplayName("Tenta outro ano quando nenhum álbum da página passa no filtro")
    void tentaDeNovoQuandoPaginaNaoTemCandidatos() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("single", "single", 1, "A")))
                .thenReturn(List.of())
                .thenReturn(List.of(item("achou", "album", 11, "Artista")));

        assertEquals("achou", picker.pickAlbumId());
        verify(spotifyClient, times(3)).searchAlbums(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Continua tentando depois de um erro do Spotify")
    void continuaDepoisDeErro() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Spotify fora do ar"))
                .thenReturn(List.of(item("depois", "album", 9, "Artista")));

        assertEquals("depois", picker.pickAlbumId());
    }

    @Test
    @DisplayName("Desiste e devolve null depois do número configurado de tentativas")
    void devolveNullDepoisDasTentativas() {
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("single", "single", 1, "A")));

        assertNull(picker.pickAlbumId());
        verify(spotifyClient, times(props.tentativas())).searchAlbums(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Usa o ano atual do fuso configurado como limite superior")
    void anoAtualVemDoClock() {
        Clock virada = Clock.fixed(Instant.parse("2027-01-01T02:00:00Z"), ZoneId.of("America/Sao_Paulo")); // ainda 2026 em SP
        SorteioProperties soUmAno = new SorteioProperties("BR", 2026, 5, 10, 5, 6, 40, props.termosBloqueados());
        PopularPorAnoAlbumPicker p = new PopularPorAnoAlbumPicker(
                spotifyClient, new FiltroDeAlbum(soUmAno), soUmAno, virada, new Random(1));
        when(spotifyClient.searchAlbums(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item("ok", "album", 12, "Artista")));

        p.pickAlbumId();

        verify(spotifyClient, atLeastOnce()).searchAlbums(eq("year:2026"), eq("BR"), eq(10), anyInt());
    }
}
