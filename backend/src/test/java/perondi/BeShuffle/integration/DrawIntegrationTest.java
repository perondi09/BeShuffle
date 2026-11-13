package perondi.BeShuffle.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AlbumImage;
import perondi.BeShuffle.entity.Artist;
import perondi.BeShuffle.services.AlbumPicker;
import perondi.BeShuffle.services.AlbumService;
import perondi.BeShuffle.services.Estilo;
import perondi.BeShuffle.services.EstiloAlbumPicker;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sorteios com limite diário no servidor")
class DrawIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private AlbumPicker albumPicker;

    @MockitoBean
    private EstiloAlbumPicker estiloAlbumPicker;

    @MockitoBean
    private AlbumService albumService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ApiDeTeste.limparBanco(jdbc);
        when(albumPicker.pickAlbumId()).thenReturn("alb1");
        when(estiloAlbumPicker.pickAlbumId(anyList())).thenReturn("alb2");
        when(albumService.getAlbumById("alb1")).thenReturn(album("alb1", "Disco Um", "Banda Um"));
        when(albumService.getAlbumById("alb2")).thenReturn(album("alb2", "Disco de Rock", "Banda Dois"));
    }

    @Test
    @DisplayName("Visitante sorteia 1 vez por dia: ganha o cookie bs_anon e a segunda tentativa recebe 429")
    void visitanteUmPorDia() throws Exception {
        var primeira = mvc.perform(post("/api/draws").with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.album.id").value("alb1"))
                .andExpect(jsonPath("$.allowance.plan").value("VISITANTE"))
                .andExpect(jsonPath("$.allowance.remaining").value(0))
                .andReturn().getResponse();
        Cookie anon = primeira.getCookie("bs_anon");
        assertNotNull(anon, "o visitante deveria receber o cookie bs_anon");
        assertTrue(anon.isHttpOnly(), "bs_anon precisa ser HttpOnly");

        mvc.perform(post("/api/draws").cookie(anon).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("DAILY_LIMIT_REACHED"));

        mvc.perform(get("/api/draws/allowance").cookie(anon))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.dailyLimit").value(1));
    }

    @Test
    @DisplayName("Visitante novo tem 1 sorteio e consultar o saldo não cria cookie")
    void saldoDeVisitanteNovo() throws Exception {
        var resposta = mvc.perform(get("/api/draws/allowance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("VISITANTE"))
                .andExpect(jsonPath("$.remaining").value(1))
                .andExpect(jsonPath("$.resetsAt").exists())
                .andReturn().getResponse();
        assertTrue(resposta.getCookie("bs_anon") == null, "consultar o saldo não deveria criar o cookie");
    }

    @Test
    @DisplayName("Sortear exige CSRF")
    void exigeCsrf() throws Exception {
        mvc.perform(post("/api/draws")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Conta logada sorteia 10 vezes por dia; a 11ª recebe 429")
    void logadoDezPorDia() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        for (int i = 1; i <= 10; i++) {
            mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.allowance.plan").value("LOGADO"))
                    .andExpect(jsonPath("$.allowance.remaining").value(10 - i));
        }
        mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("DAILY_LIMIT_REACHED"));
    }

    @Test
    @DisplayName("Conta logada escolhe estilos: o sorteio vai para o picker de estilos")
    void logadoComEstilos() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Bia", "bia@beshuffle.dev");
        mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"genres\":[\"rock\",\"jazz\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.album.id").value("alb2"));
        verify(estiloAlbumPicker).pickAlbumId(List.of(Estilo.ROCK, Estilo.JAZZ));
    }

    @Test
    @DisplayName("Visitante não escolhe estilos (401) e não perde o sorteio do dia")
    void visitanteSemEstilos() throws Exception {
        var resposta = mvc.perform(post("/api/draws").with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"genres\":[\"rock\"]}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse();
        verify(estiloAlbumPicker, never()).pickAlbumId(anyList());

        Cookie anon = resposta.getCookie("bs_anon");
        var depois = anon == null ? get("/api/draws/allowance") : get("/api/draws/allowance").cookie(anon);
        mvc.perform(depois).andExpect(jsonPath("$.remaining").value(1));
    }

    @Test
    @DisplayName("Estilo desconhecido é 400")
    void estiloDesconhecido() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Caio", "caio@beshuffle.dev");
        mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"genres\":[\"axe-music\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Se o Spotify não devolve álbum, a resposta é 503 e o sorteio do dia volta")
    void falhaDevolveSorteio() throws Exception {
        when(albumPicker.pickAlbumId()).thenReturn(null);
        var resposta = mvc.perform(post("/api/draws").with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResponse();
        Cookie anon = resposta.getCookie("bs_anon");
        assertNotNull(anon);

        mvc.perform(get("/api/draws/allowance").cookie(anon)).andExpect(jsonPath("$.remaining").value(1));
    }

    @Test
    @DisplayName("Sorteados de hoje: exige login e junta o mesmo álbum com a contagem")
    void sorteadosDeHoje() throws Exception {
        mvc.perform(get("/api/draws/today")).andExpect(status().isUnauthorized());

        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Duda", "duda@beshuffle.dev");
        mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc))).andExpect(status().isOk());
        mvc.perform(post("/api/draws").session(sessao).with(ApiDeTeste.csrf(mvc))).andExpect(status().isOk());
        mvc.perform(post("/api/draws").with(ApiDeTeste.csrf(mvc))).andExpect(status().isOk());

        mvc.perform(get("/api/draws/today").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("alb1"))
                .andExpect(jsonPath("$[0].name").value("Disco Um"))
                .andExpect(jsonPath("$[0].artist").value("Banda Um"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://img/alb1.jpg"))
                .andExpect(jsonPath("$[0].count").value(3));
    }

    @Test
    @DisplayName("A lista de estilos é pública")
    void listaDeEstilos() throws Exception {
        mvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rock"))
                .andExpect(jsonPath("$[0].name").value("Rock"));
    }

    private static Album album(String id, String nome, String artista) {
        Album album = new Album();
        album.setId(id);
        album.setName(nome);
        Artist a = new Artist();
        a.setName(artista);
        album.setArtists(List.of(a));
        AlbumImage img = new AlbumImage();
        img.setUrl("https://img/" + id + ".jpg");
        album.setImages(List.of(img));
        return album;
    }
}
