package perondi.BeShuffle.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Fichas gastas por dono e dia (tabela draw_quota). Cada operação é um único comando SQL, sem transação
 * aberta: o gasto é um UPDATE condicional, então duas requisições ao mesmo tempo nunca passam do limite.
 */
@Repository
public class DrawQuotaRepository {

    private final JdbcTemplate jdbc;

    public DrawQuotaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Gasta uma ficha se o dono ainda não chegou ao limite do dia. Devolve false quando não há ficha. */
    public boolean tentarGastar(String dono, LocalDate dia, int limite) {
        if (limite < 1) {
            return false;
        }
        if (incrementar(dono, dia, limite)) {
            return true;
        }
        try {
            jdbc.update("INSERT INTO draw_quota (owner_key, quota_date, used) VALUES (?, ?, 1)", dono, dia);
            return true;
        } catch (DataIntegrityViolationException jaExiste) {
            // A linha do dia já existia (ou outra requisição acabou de criá-la): tenta pelo UPDATE de novo.
            return incrementar(dono, dia, limite);
        }
    }

    /** Devolve uma ficha gasta (sorteio que falhou depois do gasto). */
    public void devolver(String dono, LocalDate dia) {
        jdbc.update("UPDATE draw_quota SET used = used - 1 WHERE owner_key = ? AND quota_date = ? AND used > 0",
                dono, dia);
    }

    public int usados(String dono, LocalDate dia) {
        List<Integer> linhas = jdbc.queryForList(
                "SELECT used FROM draw_quota WHERE owner_key = ? AND quota_date = ?", Integer.class, dono, dia);
        return linhas.isEmpty() ? 0 : linhas.getFirst();
    }

    private boolean incrementar(String dono, LocalDate dia, int limite) {
        return jdbc.update(
                "UPDATE draw_quota SET used = used + 1 WHERE owner_key = ? AND quota_date = ? AND used < ?",
                dono, dia, limite) == 1;
    }
}
