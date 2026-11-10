package perondi.BeShuffle.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import perondi.BeShuffle.repository.AppUserRepository;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Rotas de conta com a segurança ligada")
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AppUserRepository repository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        repository.deleteAll();
    }

    /**
     * Faz o que o site faz: pega o cookie XSRF-TOKEN de uma resposta qualquer e devolve o valor no header.
     * Não usa o csrf() do spring-security-test, que troca o repositório de tokens do filtro e não desfaz.
     */
    private RequestPostProcessor csrfDeVerdade() throws Exception {
        Cookie xsrf = mvc.perform(get("/actuator/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf, "a API deveria entregar o cookie XSRF-TOKEN");
        return request -> {
            request.setCookies(xsrf);
            request.addHeader("X-XSRF-TOKEN", xsrf.getValue());
            return request;
        };
    }

    private static String cadastro(String nome, String email, String senha) {
        return """
                {"displayName":"%s","email":"%s","password":"%s"}""".formatted(nome, email, senha);
    }

    private static String credenciais(String email, String senha) {
        return """
                {"email":"%s","password":"%s"}""".formatted(email, senha);
    }

    private MockHttpSession cadastrar(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/register").with(csrfDeVerdade())
                        .contentType(MediaType.APPLICATION_JSON).content(cadastro("Ana", email, "senha-forte-1")))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    @Test
    @DisplayName("Cadastro cria a conta e já deixa a pessoa logada")
    void cadastroLoga() throws Exception {
        MockHttpSession sessao = cadastrar("ana@exemplo.com");
        assertNotNull(sessao);

        mvc.perform(get("/api/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.displayName").value("Ana"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Cadastro com e-mail repetido dá 409 e com dados ruins dá 400")
    void cadastroInvalido() throws Exception {
        cadastrar("ana@exemplo.com");

        mvc.perform(post("/api/auth/register").with(csrfDeVerdade())
                        .contentType(MediaType.APPLICATION_JSON).content(cadastro("Ana", "ANA@exemplo.com", "senha-forte-1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_REGISTERED"));

        mvc.perform(post("/api/auth/register").with(csrfDeVerdade())
                        .contentType(MediaType.APPLICATION_JSON).content(cadastro("Ana", "bia@exemplo.com", "curta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Login certo responde 204 e troca o id da sessão; senha errada dá 401")
    void login() throws Exception {
        cadastrar("ana@exemplo.com");
        MockHttpSession anonima = new MockHttpSession();
        String idAntes = anonima.getId();

        MvcResult r = mvc.perform(post("/api/auth/login").with(csrfDeVerdade()).session(anonima)
                        .contentType(MediaType.APPLICATION_JSON).content(credenciais("ana@exemplo.com", "senha-forte-1")))
                .andExpect(status().isNoContent())
                .andReturn();
        MockHttpSession logada = (MockHttpSession) r.getRequest().getSession(false);
        assertNotNull(logada);
        assertNotEquals(idAntes, logada.getId());

        mvc.perform(get("/api/me").session(logada)).andExpect(status().isOk());

        mvc.perform(post("/api/auth/login").with(csrfDeVerdade())
                        .contentType(MediaType.APPLICATION_JSON).content(credenciais("ana@exemplo.com", "errada-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Sem sessão, /api/me responde 401 em JSON, sem redirecionar")
    void meSemSessao() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Logout encerra a sessão: depois dele /api/me volta a ser 401")
    void logout() throws Exception {
        MockHttpSession sessao = cadastrar("ana@exemplo.com");

        mvc.perform(post("/api/auth/logout").with(csrfDeVerdade()).session(sessao))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/me").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST sem token CSRF é recusado com 403")
    void exigeCsrf() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(credenciais("ana@exemplo.com", "senha-forte-1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST com token CSRF errado também é recusado com 403")
    void recusaCsrfErrado() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .cookie(new Cookie("XSRF-TOKEN", "token-de-verdade"))
                        .header("X-XSRF-TOKEN", "outro-token")
                        .contentType(MediaType.APPLICATION_JSON).content(credenciais("ana@exemplo.com", "senha-forte-1")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Qualquer resposta entrega o cookie XSRF-TOKEN legível pelo site (sem HttpOnly)")
    void entregaCookieCsrf() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    @DisplayName("Rotas públicas continuam abertas sem login")
    void rotasPublicas() throws Exception {
        // Os álbuns chamariam o Spotify de verdade; o health basta para provar que só /api/me exige login.
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
