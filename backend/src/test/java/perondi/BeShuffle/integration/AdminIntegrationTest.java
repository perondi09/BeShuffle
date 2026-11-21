package perondi.BeShuffle.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.repository.DailyAlbumRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Painel de admin com a segurança ligada")
class AdminIntegrationTest {

    private static final String ADMIN = "admin@beshuffle.dev";
    private static final String OUTRO_ADMIN = "chefe@beshuffle.dev";
    private static final String CELULAR = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) Mobile/15E148";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DailyAlbumRepository dailyAlbumRepository;

    @Autowired
    private Clock clock;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ApiDeTeste.limparBanco(jdbc);
        DailyAlbum hoje = new DailyAlbum();
        hoje.setSpotifyAlbumId("album-do-dia");
        hoje.setAlbumName("Clube da Esquina");
        hoje.setArtistName("Milton Nascimento");
        hoje.setDisplayDate(LocalDate.now(clock));
        hoje.setFullAlbumJson("{\"id\":\"album-do-dia\",\"name\":\"Clube da Esquina\"}");
        dailyAlbumRepository.save(hoje);
    }

    private long idDe(String email) {
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    private static String motivo(String texto) {
        return "{\"reason\":\"%s\"}".formatted(texto);
    }

    @Test
    @DisplayName("Sem login o painel dá 401; conta comum dá 403; conta da lista ADMIN_EMAILS entra")
    void soAdminEntra() throws Exception {
        mvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());

        MockHttpSession comum = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(get("/api/me").session(comum)).andExpect(jsonPath("$.admin").value(false));
        mvc.perform(get("/api/admin/stats").session(comum)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users").session(comum)).andExpect(status().isForbidden());

        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        mvc.perform(get("/api/me").session(admin)).andExpect(jsonPath("$.admin").value(true));
        mvc.perform(get("/api/admin/stats").session(admin)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ação de admin sem o header CSRF é recusada")
    void exigeCsrf() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(post("/api/admin/users/{id}/ban", idDe("ana@beshuffle.dev")).session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(motivo("spam")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Acessos: cada visitante conta uma vez por dia, separando logados, visitantes e celular")
    void contaAcessos() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        mvc.perform(get("/api/albums/daily").session(admin)).andExpect(status().isOk());
        mvc.perform(get("/api/albums/daily").session(admin)).andExpect(status().isOk());
        mvc.perform(get("/api/albums/daily").header("User-Agent", CELULAR)).andExpect(status().isOk());

        mvc.perform(get("/api/admin/stats").param("periodo", "hoje").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("hoje"))
                .andExpect(jsonPath("$.area").value("tudo"))
                .andExpect(jsonPath("$.visits").value(2))
                .andExpect(jsonPath("$.loggedVisits").value(1))
                .andExpect(jsonPath("$.visitorVisits").value(1))
                .andExpect(jsonPath("$.mobilePct").value(50.0))
                .andExpect(jsonPath("$.newAccounts").value(1))
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.byArea.musica.visits").value(2))
                .andExpect(jsonPath("$.byArea.filmes.visits").value(0))
                .andExpect(jsonPath("$.last30Days", hasSize(30)))
                .andExpect(jsonPath("$.last30Days[29].visits").value(2))
                .andExpect(jsonPath("$.last30Days[29].newAccounts").value(1));

        // A área "filmes" ainda não tem nada: o painel mostra zero em vez de quebrar.
        mvc.perform(get("/api/admin/stats").param("area", "filmes").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visits").value(0))
                .andExpect(jsonPath("$.draws").value(0));
    }

    @Test
    @DisplayName("Período ou área desconhecidos dão 400")
    void validaFiltros() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        mvc.perform(get("/api/admin/stats").param("periodo", "ano").session(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/stats").param("area", "series").session(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/users").param("status", "vips").session(admin)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Banir derruba a sessão na hora e bloqueia o login; desbanir devolve o acesso; tudo fica registrado")
    void baneEDesbane() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        long anaId = idDe("ana@beshuffle.dev");

        mvc.perform(post("/api/admin/users/{id}/ban", anaId).session(admin).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(motivo("Comentários ofensivos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banReason").value("Comentários ofensivos"))
                .andExpect(jsonPath("$.bannedAt").isNotEmpty());

        // Quem já estava logado perde o acesso na próxima requisição.
        mvc.perform(get("/api/me").session(ana))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("ACCOUNT_SUSPENDED"));

        String login = "{\"email\":\"ana@beshuffle.dev\",\"password\":\"senha-forte-1\"}";
        mvc.perform(post("/api/auth/login").with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_SUSPENDED"));

        mvc.perform(get("/api/admin/users").param("status", "banidos").session(admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].email").value("ana@beshuffle.dev"));
        mvc.perform(get("/api/admin/stats").session(admin)).andExpect(jsonPath("$.bannedAccounts").value(1));

        mvc.perform(post("/api/admin/users/{id}/unban", anaId).session(admin).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bannedAt").doesNotExist());
        mvc.perform(post("/api/auth/login").with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isNoContent());

        List<String> acoes = jdbc.queryForList("SELECT action FROM admin_actions ORDER BY id", String.class);
        assertEquals(List.of("BAN", "UNBAN"), acoes);
        mvc.perform(get("/api/admin/activity").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("BAN")))
                .andExpect(jsonPath("$[*].type", hasItem("UNBAN")))
                .andExpect(jsonPath("$[*].type", hasItem("ACCOUNT_CREATED")));
    }

    @Test
    @DisplayName("Não dá para banir a si mesmo, outro admin, conta que não existe ou sem motivo")
    void regrasDoBanimento() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        ApiDeTeste.logar(mvc, "Chefe", OUTRO_ADMIN);
        ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");

        mvc.perform(post("/api/admin/users/{id}/ban", idDe(ADMIN)).session(admin).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(motivo("teste")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/admin/users/{id}/ban", idDe(OUTRO_ADMIN)).session(admin).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(motivo("teste")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/admin/users/{id}/ban", 999_999).session(admin).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(motivo("teste")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/admin/users/{id}/ban", idDe("ana@beshuffle.dev")).session(admin)
                        .with(ApiDeTeste.csrf(mvc)).contentType(MediaType.APPLICATION_JSON).content(motivo(" ")))
                .andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_actions", Integer.class));
    }

    @Test
    @DisplayName("Lista de contas busca por nome ou e-mail e filtra admins")
    void listaContas() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        ApiDeTeste.logar(mvc, "Ana Lima", "ana@beshuffle.dev");
        ApiDeTeste.logar(mvc, "Bruno", "bruno@beshuffle.dev");

        mvc.perform(get("/api/admin/users").session(admin))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.items", hasSize(3)));
        mvc.perform(get("/api/admin/users").param("q", "lima").session(admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].displayName").value("Ana Lima"));
        // "%" é texto, não curinga
        mvc.perform(get("/api/admin/users").param("q", "%").session(admin)).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/admin/users").param("status", "admins").session(admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].role").value("ADMIN"));
        mvc.perform(get("/api/admin/users").param("size", "2").param("page", "1").session(admin))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("Remover comentário apaga só o texto (a nota fica) e regerar o álbum com notas é recusado")
    void moderacao() throws Exception {
        MockHttpSession admin = ApiDeTeste.logar(mvc, "Admin", ADMIN);
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(put("/api/reviews/today").session(ana).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":2,\"comment\":\"compra seguidor aqui\"}"))
                .andExpect(status().isOk());
        long reviewId = jdbc.queryForObject("SELECT id FROM reviews", Long.class);

        mvc.perform(get("/api/admin/comments").session(admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].comment").value("compra seguidor aqui"))
                .andExpect(jsonPath("$.items[0].albumName").value("Clube da Esquina"))
                .andExpect(jsonPath("$.items[0].area").value("musica"));

        mvc.perform(post("/api/admin/daily-album/regenerate").session(admin).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/admin/comments/{id}", reviewId).session(admin).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isNoContent());
        assertNull(jdbc.queryForObject("SELECT comment FROM reviews WHERE id = ?", String.class, reviewId));
        assertEquals(2, jdbc.queryForObject("SELECT rating FROM reviews WHERE id = ?", Integer.class, reviewId));
        mvc.perform(get("/api/admin/comments").session(admin)).andExpect(jsonPath("$.total").value(0));
        mvc.perform(delete("/api/admin/comments/{id}", reviewId).session(admin).with(ApiDeTeste.csrf(mvc)))
                .andExpect(status().isNotFound());
        assertEquals("REMOVE_COMMENT", jdbc.queryForObject("SELECT action FROM admin_actions", String.class));
    }
}
