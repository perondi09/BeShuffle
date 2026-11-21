package perondi.BeShuffle.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/** Acessos por dia (tabela daily_visits): uma linha por visitante, dia e área. */
@Repository
public class AcessoRepository {

    private final JdbcTemplate jdbc;

    public AcessoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Grava o acesso; se o visitante já foi contado hoje nesta área, não faz nada. */
    public void registrar(LocalDate dia, String chave, String area, boolean logado, boolean celular) {
        jdbc.update("""
                INSERT INTO daily_visits (visit_date, visitor_key, area, logged_in, mobile)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT DO NOTHING""", dia, chave, area, logado, celular);
    }
}
