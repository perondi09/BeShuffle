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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Notas e comentários do Álbum do Dia")
class ReviewIntegrationTest {

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

    private static String nota(int rating, String comentario) {
        return comentario == null
                ? "{\"rating\":%d}".formatted(rating)
                : "{\"rating\":%d,\"comment\":\"%s\"}".formatted(rating, comentario);
    }

    @Test
    @DisplayName("Sem login: ler e dar nota recebem 401")
    void exigeLogin() throws Exception {
        mvc.perform(get("/api/reviews/today")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/reviews/today").with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(nota(5, null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sem notas ainda: média vazia e total zero")
    void semNotas() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(get("/api/reviews/today").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.albumId").value("album-do-dia"))
                .andExpect(jsonPath("$.albumName").value("Clube da Esquina"))
                .andExpect(jsonPath("$.average").doesNotExist())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.mine").doesNotExist());
    }

    @Test
    @DisplayName("Dar nota cria; dar de novo edita (continua uma nota só)")
    void criaEEdita() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(put("/api/reviews/today").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(nota(4, "  Muito bom  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.mine.rating").value(4))
                .andExpect(jsonPath("$.mine.comment").value("Muito bom"));

        mvc.perform(put("/api/reviews/today").session(sessao).with(ApiDeTeste.csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON).content(nota(5, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.average").value(5.0))
                .andExpect(jsonPath("$.mine.rating").value(5))
                .andExpect(jsonPath("$.mine.comment").doesNotExist());
    }

    @Test
    @DisplayName("Média e lista da comunidade mostram o nome, nunca o e-mail")
    void comunidade() throws Exception {
        MockHttpSession ana = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        MockHttpSession bia = ApiDeTeste.logar(mvc, "Bia", "bia@beshuffle.dev");
        mvc.perform(put("/api/reviews/today").session(ana).with(ApiDeTeste.csrf(mvc))
                .contentType(MediaType.APPLICATION_JSON).content(nota(5, "Obra-prima"))).andExpect(status().isOk());
        mvc.perform(put("/api/reviews/today").session(bia).with(ApiDeTeste.csrf(mvc))
                .contentType(MediaType.APPLICATION_JSON).content(nota(2, null))).andExpect(status().isOk());

        mvc.perform(get("/api/reviews/today").session(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(3.5))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[?(@.author == 'Ana')].comment").value("Obra-prima"))
                .andExpect(content().string(not(containsString("@beshuffle.dev"))));
    }

    @Test
    @DisplayName("Nota fora de 1 a 5, sem nota ou comentário longo demais são 400")
    void validacao() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        for (String corpo : new String[]{nota(0, null), nota(6, null), "{\"comment\":\"sem nota\"}",
                nota(3, "a".repeat(501))}) {
            mvc.perform(put("/api/reviews/today").session(sessao).with(ApiDeTeste.csrf(mvc))
                            .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Dar nota exige CSRF")
    void exigeCsrf() throws Exception {
        MockHttpSession sessao = ApiDeTeste.logar(mvc, "Ana", "ana@beshuffle.dev");
        mvc.perform(put("/api/reviews/today").session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content(nota(5, null)))
                .andExpect(status().isForbidden());
    }
}
