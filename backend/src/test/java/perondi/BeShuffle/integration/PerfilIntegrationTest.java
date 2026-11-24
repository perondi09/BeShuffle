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

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Perfil editável e diário de notas")
class PerfilIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Clock clock;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ApiDeTeste.limparBanco(jdbc);
    }

    private static String perfil(String nome, String nick, String avatar) {
        StringBuilder json = new StringBuilder("{");
        String[][] campos = {{"displayName", nome}, {"handle", nick}, {"avatar", avatar}};
        for (String[] campo : campos) {
            if (campo[1] != null) {
                json.append(json.length() > 1 ? "," : "").append("\"%s\":\"%s\"".formatted(campo[0], campo[1]));
            }
        }
        return json.append("}").toString();
    }

    private void salvarPerfil(MockHttpSession sessao, String corpo, int status) throws Exception {
        mvc.perform(put("/api/me").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(status));
    }

    /** Cria um Álbum do Dia numa data e a nota (em meias estrelas) de uma conta para ele. */
    private void nota(String email, LocalDate dia, String album, int estrelasX2, String comentario) {
        jdbc.update("INSERT INTO daily_albums (spotify_album_id, album_name, artist_name, image_url, display_date, full_album_json)"
                + " SELECT ?, ?, ?, ?, ?, '{}' WHERE NOT EXISTS (SELECT 1 FROM daily_albums WHERE display_date = ?)",
                "id-" + dia, album, "Artista " + album, "https://img/" + album, dia, dia);
        jdbc.update("INSERT INTO reviews (user_id, daily_album_id, rating_x2, comment, created_at, updated_at)"
                + " VALUES ((SELECT id FROM users WHERE email = ?), (SELECT id FROM daily_albums WHERE display_date = ?), ?, ?,"
                + " CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", email, dia, estrelasX2, comentario);
    }

    @Test
    @DisplayName("Cadastro já nasce com nick (do nome) e avatar padrão, e GET /api/me devolve os dois")
    void nasceComNick() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana Lima", "ana@beshuffle.dev");
        mvc.perform(get("/api/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("ana_lima"))
                .andExpect(jsonPath("$.avatar").value("rosa"))
                .andExpect(jsonPath("$.displayName").value("Ana Lima"))
                .andExpect(jsonPath("$.admin").value(false));

        // Outra pessoa com o mesmo nome ganha um número em vez de um erro.
        MockHttpSession outra = ApiDeTeste.logar(mvc, "Ana Lima", "outra@beshuffle.dev");
        mvc.perform(get("/api/me").session(outra)).andExpect(jsonPath("$.handle").value("ana_lima2"));
    }

    @Test
    @DisplayName("Muda nome, nick e avatar; o GET /api/me seguinte já mostra, e o nick vira minúsculo")
    void editaPerfil() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");

        mvc.perform(put("/api/me").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(perfil("Ana Lima", " Ana_Lima ", "menta")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Ana Lima"))
                .andExpect(jsonPath("$.handle").value("ana_lima"))
                .andExpect(jsonPath("$.avatar").value("menta"));

        mvc.perform(get("/api/me").session(sessao))
                .andExpect(jsonPath("$.handle").value("ana_lima"))
                .andExpect(jsonPath("$.avatar").value("menta"));
        // Só o avatar: o resto fica como está.
        salvarPerfil(sessao, perfil(null, null, "lilas"), 200);
        mvc.perform(get("/api/me").session(sessao))
                .andExpect(jsonPath("$.handle").value("ana_lima"))
                .andExpect(jsonPath("$.avatar").value("lilas"));
    }

    @Test
    @DisplayName("Recusa nick inválido, reservado ou de outra conta (409), avatar desconhecido e nome curto; nada muda")
    void recusaPerfilInvalido() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        ApiDeTeste.logar(mvc, "Bruno", "bruno@beshuffle.dev");

        salvarPerfil(sessao, perfil(null, "ab", null), 400);
        salvarPerfil(sessao, perfil(null, "ana lima", null), 400);
        salvarPerfil(sessao, perfil(null, "admin", null), 400);
        salvarPerfil(sessao, perfil(null, "BRUNO", null), 409);
        salvarPerfil(sessao, perfil(null, null, "roxo"), 400);
        salvarPerfil(sessao, perfil("A", null, null), 400);

        mvc.perform(put("/api/me").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(perfil(null, "bruno", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("HANDLE_TAKEN"));
        mvc.perform(get("/api/me").session(sessao))
                .andExpect(jsonPath("$.handle").value("ana"))
                .andExpect(jsonPath("$.avatar").value("rosa"));
    }

    @Test
    @DisplayName("Editar o perfil exige login e CSRF")
    void exigeLoginECsrf() throws Exception {
        mvc.perform(put("/api/me").with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(perfil("Ana", null, null)))
                .andExpect(status().isUnauthorized());

        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(put("/api/me").session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content(perfil("Ana Lima", null, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Perfil e diário só para conta logada; visitante recebe 401")
    void perfilExigeLogin() throws Exception {
        mvc.perform(get("/api/users/ana")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/ana/reviews")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Perfil mostra nome, nick, avatar, quantas notas e a média em estrelas, sem e-mail")
    void perfilPublico() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        LocalDate hoje = LocalDate.now(clock);
        nota("ana@beshuffle.dev", hoje.minusDays(2), "Rumours", 10, "Perfeito");
        nota("ana@beshuffle.dev", hoje.minusDays(1), "Blonde", 7, null);

        mvc.perform(get("/api/users/ana").session(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("ana"))
                .andExpect(jsonPath("$.displayName").value("Ana"))
                .andExpect(jsonPath("$.avatar").value("rosa"))
                .andExpect(jsonPath("$.reviewCount").value(2))
                // (5 + 3,5) / 2 = 4,25 → 4,3
                .andExpect(jsonPath("$.average").value(4.3))
                .andExpect(jsonPath("$.joinedAt").isNotEmpty())
                .andExpect(content().string(not(containsString("@beshuffle.dev"))));

        // O nick vale em qualquer caixa e outra conta logada também vê o perfil.
        MockHttpSession bia = ApiDeTeste.logar(mvc, "Bia", "bia@beshuffle.dev");
        mvc.perform(get("/api/users/ANA").session(bia)).andExpect(jsonPath("$.handle").value("ana"));
    }

    @Test
    @DisplayName("Conta sem notas: total zero e média nula")
    void perfilSemNotas() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(get("/api/users/ana").session(ana))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.average").doesNotExist());
        mvc.perform(get("/api/users/ana/reviews").session(ana))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @DisplayName("Diário: todas as notas da conta, do Álbum do Dia mais recente para o mais antigo, com a capa e a nota em estrelas")
    void diario() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        ApiDeTeste.logar(mvc, "Bia", "bia@beshuffle.dev");
        LocalDate hoje = LocalDate.now(clock);
        nota("ana@beshuffle.dev", hoje.minusDays(5), "Rumours", 10, "Perfeito");
        nota("ana@beshuffle.dev", hoje.minusDays(1), "Blonde", 7, null);
        nota("ana@beshuffle.dev", hoje.minusDays(3), "OK Computer", 9, "Clássico");
        nota("bia@beshuffle.dev", hoje.minusDays(1), "Blonde", 2, "Não gostei");

        mvc.perform(get("/api/users/ana/reviews").session(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].albumName").value("Blonde"))
                .andExpect(jsonPath("$.items[0].rating").value(3.5))
                .andExpect(jsonPath("$.items[0].albumArtist").value("Artista Blonde"))
                .andExpect(jsonPath("$.items[0].albumImageUrl").value("https://img/Blonde"))
                .andExpect(jsonPath("$.items[0].date").value(hoje.minusDays(1).toString()))
                .andExpect(jsonPath("$.items[0].comment").doesNotExist())
                .andExpect(jsonPath("$.items[1].albumName").value("OK Computer"))
                .andExpect(jsonPath("$.items[1].rating").value(4.5))
                .andExpect(jsonPath("$.items[2].albumName").value("Rumours"))
                .andExpect(jsonPath("$.items[2].comment").value("Perfeito"));
    }

    @Test
    @DisplayName("Diário pagina e limita o tamanho da página")
    void diarioPagina() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        LocalDate hoje = LocalDate.now(clock);
        for (int i = 1; i <= 5; i++) {
            nota("ana@beshuffle.dev", hoje.minusDays(i), "Disco " + i, 2 * i, null);
        }

        mvc.perform(get("/api/users/ana/reviews").param("size", "2").param("page", "1").session(ana))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].albumName").value("Disco 3"));
        mvc.perform(get("/api/users/ana/reviews").param("size", "9999").session(ana))
                .andExpect(jsonPath("$.size").value(50));
        mvc.perform(get("/api/users/ana/reviews").param("page", "-4").session(ana))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("Perfil que não existe é 404; conta banida some do perfil e do diário")
    void naoEncontradoEBanida() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        ApiDeTeste.logar(mvc, "Bruno", "bruno@beshuffle.dev");

        mvc.perform(get("/api/users/ninguem").session(ana)).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/ninguem/reviews").session(ana)).andExpect(status().isNotFound());

        mvc.perform(get("/api/users/bruno").session(ana)).andExpect(status().isOk());
        jdbc.update("UPDATE users SET banned_at = CURRENT_TIMESTAMP WHERE email = ?", "bruno@beshuffle.dev");
        mvc.perform(get("/api/users/bruno").session(ana)).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/bruno/reviews").session(ana)).andExpect(status().isNotFound());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE banned_at IS NOT NULL", Integer.class));
    }
}
