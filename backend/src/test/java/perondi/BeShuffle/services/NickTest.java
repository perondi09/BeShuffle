package perondi.BeShuffle.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Regras do nick")
class NickTest {

    @Test
    @DisplayName("Formato: 3 a 20 letras minúsculas, números ou _")
    void formato() {
        assertTrue(Nick.formatoValido("ana"));
        assertTrue(Nick.formatoValido("marina_wav_99"));
        assertTrue(Nick.formatoValido("a".repeat(20)));
        assertFalse(Nick.formatoValido("ab"));
        assertFalse(Nick.formatoValido("a".repeat(21)));
        assertFalse(Nick.formatoValido("Ana"));
        assertFalse(Nick.formatoValido("ana lima"));
        assertFalse(Nick.formatoValido("ana-lima"));
        assertFalse(Nick.formatoValido("ação"));
        assertFalse(Nick.formatoValido(""));
    }

    @Test
    @DisplayName("Normalizar tira espaços das pontas e põe em minúsculas; nulo vira vazio")
    void normaliza() {
        assertEquals("ana_lima", Nick.normalizar("  Ana_Lima "));
        assertEquals("", Nick.normalizar(null));
    }

    @Test
    @DisplayName("Nomes que se passariam pela equipe são reservados")
    void reservados() {
        assertTrue(Nick.reservado("admin"));
        assertTrue(Nick.reservado("beshuffle"));
        assertFalse(Nick.reservado("ana"));
    }

    @Test
    @DisplayName("Sugestão a partir do nome: sem acento, minúscula e com _ no lugar dos espaços e símbolos")
    void doNome() {
        assertEquals("marina_wav", Nick.doNome("Marína Wav"));
        assertEquals("joao_silva", Nick.doNome("  João   Silva!! "));
        assertEquals("lu_oliveira_99", Nick.doNome("Lu Oliveira 99"));
    }

    @Test
    @DisplayName("Nome sem letras aproveitáveis vira \"user\"; nome curto ganha \"user\" para chegar a 3")
    void doNomeSemLetras() {
        assertEquals("user", Nick.doNome("🎵🎵"));
        assertEquals("user", Nick.doNome(null));
        assertEquals("user", Nick.doNome("  "));
        assertEquals("aluser", Nick.doNome("Al"));
    }

    @Test
    @DisplayName("Nome longo é cortado em 20 sem deixar _ no fim, e o resultado sempre tem formato válido")
    void doNomeLongo() {
        String nick = Nick.doNome("Um Nome Muito Mas Muito Comprido Mesmo");
        assertTrue(nick.length() <= Nick.MAX);
        assertFalse(nick.endsWith("_"));
        assertTrue(Nick.formatoValido(nick));
    }

    @Test
    @DisplayName("Único: usa o nome se estiver livre; senão põe 2, 3, 4... e nunca passa de 20 letras")
    void unico() {
        assertEquals("ana", Nick.unico("Ana", nick -> false));
        assertEquals("ana2", Nick.unico("Ana", Set.of("ana")::contains));
        assertEquals("ana4", Nick.unico("Ana", Set.of("ana", "ana2", "ana3")::contains));

        String longo = "a".repeat(20);
        String proximo = Nick.unico(longo, Set.of(longo)::contains);
        assertEquals(20, proximo.length());
        assertTrue(proximo.endsWith("2"));
        assertTrue(Nick.formatoValido(proximo));
    }

    @Test
    @DisplayName("Único: pula nome reservado mesmo que ninguém o tenha")
    void unicoPulaReservado() {
        assertEquals("admin2", Nick.unico("Admin", nick -> false));
    }
}
