package perondi.BeShuffle.integration;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sessão de login no banco (Spring Session JDBC), como em produção. Os outros testes desligam o Spring Session
 * para usar MockHttpSession; aqui ele fica ligado e a sessão anda só pelo cookie JSESSIONID.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
@DisplayName("Sessão de login guardada no Postgres")
class SessaoJdbcIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Filter sessaoNoBanco = context.getBean("springSessionRepositoryFilter", Filter.class);
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(sessaoNoBanco).apply(springSecurity()).build();
        jdbc.update("DELETE FROM SPRING_SESSION");
        ApiDeTeste.limparBanco(jdbc);
    }

    private int sessoesNoBanco() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION", Integer.class);
        return total == null ? 0 : total;
    }

    @Test
    @DisplayName("Cadastro grava a sessão no banco; o cookie JSESSIONID sozinho mantém o login; logout apaga")
    void sessaoNoBanco() throws Exception {
        Cookie xsrf = mvc.perform(get("/actuator/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf);

        var resposta = mvc.perform(post("/api/auth/register").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Ana\",\"email\":\"ana@beshuffle.dev\",\"password\":\"senha-forte-1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Cookie sessao = resposta.getCookie("JSESSIONID");
        assertNotNull(sessao, "o login deveria vir no cookie JSESSIONID; Set-Cookie: " + resposta.getHeaders("Set-Cookie"));
        assertEquals(1, sessoesNoBanco());

        mvc.perform(get("/api/me").cookie(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@beshuffle.dev"));

        mvc.perform(post("/api/auth/logout").cookie(sessao, xsrf).header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isNoContent());
        assertEquals(0, sessoesNoBanco());
        mvc.perform(get("/api/me").cookie(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Visitante chamando /api/me não cria sessão no banco")
    void visitanteNaoCriaSessao() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/draws/allowance")).andExpect(status().isOk());
        assertEquals(0, sessoesNoBanco());
    }
}
