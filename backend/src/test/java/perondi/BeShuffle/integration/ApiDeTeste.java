package perondi.BeShuffle.integration;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ajudas dos testes de integração com a segurança ligada: CSRF de verdade, conta logada e banco limpo. */
final class ApiDeTeste {

    private ApiDeTeste() {
    }

    /**
     * Faz o que o site faz: pega o cookie XSRF-TOKEN de uma resposta e devolve o valor no header.
     * Não usa o csrf() do spring-security-test, que troca o repositório de tokens do filtro e não desfaz.
     * Mantém os outros cookies da requisição (ex.: bs_anon do visitante).
     */
    static RequestPostProcessor csrf(MockMvc mvc) throws Exception {
        Cookie xsrf = mvc.perform(get("/actuator/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf, "a API deveria entregar o cookie XSRF-TOKEN");
        return request -> {
            Cookie[] atuais = request.getCookies();
            request.setCookies(atuais == null ? new Cookie[]{xsrf} : comMais(atuais, xsrf));
            request.addHeader("X-XSRF-TOKEN", xsrf.getValue());
            return request;
        };
    }

    /** Cria uma conta e devolve a sessão já logada. */
    static MockHttpSession logar(MockMvc mvc, String nome, String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/register").with(csrf(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"%s","email":"%s","password":"senha-forte-1"}""".formatted(nome, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    /** Apaga o que os testes de sorteio e nota gravam (filhos antes dos pais). */
    static void limparBanco(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM reviews");
        jdbc.update("DELETE FROM draws");
        jdbc.update("DELETE FROM draw_quota");
        jdbc.update("DELETE FROM daily_albums");
        jdbc.update("DELETE FROM users");
    }

    private static Cookie[] comMais(Cookie[] atuais, Cookie extra) {
        Cookie[] todos = new Cookie[atuais.length + 1];
        System.arraycopy(atuais, 0, todos, 0, atuais.length);
        todos[atuais.length] = extra;
        return todos;
    }
}
