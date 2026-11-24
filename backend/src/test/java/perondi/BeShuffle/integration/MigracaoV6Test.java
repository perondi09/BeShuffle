package perondi.BeShuffle.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A V6 mexe em dados que já existem em produção (contas sem nick, notas de 1 a 5). Aqui o banco é migrado só até a V5,
 * recebe contas e notas "antigas" e então a V6 é aplicada por cima, como vai acontecer no deploy.
 */
@DisplayName("Migration V6: contas e notas que já existem")
class MigracaoV6Test {

    private static Flyway flyway(DataSource banco, String ate) {
        return Flyway.configure().dataSource(banco).locations("classpath:db/migration").target(ate).load();
    }

    @Test
    @DisplayName("Contas antigas ganham nick user<id> e avatar rosa; notas de 1 a 5 viram o dobro e a coluna antiga some")
    void dadosAntigosSobrevivem() {
        // Uma conexão só, que o Flyway não fecha: o H2 amarra as regras de check à sessão que as criou, e fechá-la no
        // meio quebraria os INSERT seguintes (no app, o pool de conexões mantém a sessão aberta).
        SingleConnectionDataSource banco = new SingleConnectionDataSource(
                "jdbc:h2:mem:migracao_v6;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", "sa", "", true);
        try {
            verificar(banco);
        } finally {
            banco.destroy();
        }
    }

    private static void verificar(DataSource banco) {
        flyway(banco, "5").migrate();

        JdbcTemplate jdbc = new JdbcTemplate(banco);
        // role vai explícito: o H2 não aplica o DEFAULT da V5 num INSERT cru (o Postgres de produção aplica).
        jdbc.update("INSERT INTO users (email, password_hash, display_name, role, created_at) VALUES ('ana@x.co', 'hash', 'Ana', 'USER', CURRENT_TIMESTAMP)");
        jdbc.update("INSERT INTO users (email, password_hash, display_name, role, created_at) VALUES ('bia@x.co', 'hash', 'Bia', 'USER', CURRENT_TIMESTAMP)");
        jdbc.update("INSERT INTO daily_albums (spotify_album_id, album_name, artist_name, display_date, full_album_json)"
                + " VALUES ('id1', 'Rumours', 'Fleetwood Mac', DATE '2026-11-10', '{}')");
        jdbc.update("INSERT INTO daily_albums (spotify_album_id, album_name, artist_name, display_date, full_album_json)"
                + " VALUES ('id2', 'Blonde', 'Frank Ocean', DATE '2026-11-11', '{}')");
        long ana = jdbc.queryForObject("SELECT id FROM users WHERE email = 'ana@x.co'", Long.class);
        long bia = jdbc.queryForObject("SELECT id FROM users WHERE email = 'bia@x.co'", Long.class);
        long rumours = jdbc.queryForObject("SELECT id FROM daily_albums WHERE spotify_album_id = 'id1'", Long.class);
        long blonde = jdbc.queryForObject("SELECT id FROM daily_albums WHERE spotify_album_id = 'id2'", Long.class);
        for (Object[] nota : new Object[][]{{ana, rumours, 5, "Perfeito"}, {ana, blonde, 4, null}, {bia, rumours, 1, "Não curti"}}) {
            jdbc.update("INSERT INTO reviews (user_id, daily_album_id, rating, comment, created_at, updated_at)"
                    + " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", nota);
        }

        flyway(banco, "latest").migrate();

        List<Map<String, Object>> contas = jdbc.queryForList("SELECT id, handle, avatar_key FROM users ORDER BY id");
        assertEquals(2, contas.size());
        for (Map<String, Object> conta : contas) {
            assertEquals("user" + conta.get("id"), conta.get("handle"));
            assertEquals("rosa", conta.get("avatar_key"));
        }

        assertEquals(10, jdbc.queryForObject("SELECT rating_x2 FROM reviews WHERE user_id = ? AND daily_album_id = ?", Integer.class, ana, rumours));
        assertEquals(8, jdbc.queryForObject("SELECT rating_x2 FROM reviews WHERE user_id = ? AND daily_album_id = ?", Integer.class, ana, blonde));
        assertEquals(2, jdbc.queryForObject("SELECT rating_x2 FROM reviews WHERE user_id = ? AND daily_album_id = ?", Integer.class, bia, rumours));
        assertEquals("Perfeito", jdbc.queryForObject("SELECT comment FROM reviews WHERE user_id = ? AND daily_album_id = ?", String.class, ana, rumours));
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'REVIEWS' AND UPPER(COLUMN_NAME) = 'RATING'",
                Integer.class));

        // As travas novas valem para dados novos: nota de 1 a 10, nick único e em minúsculas.
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO reviews (user_id, daily_album_id, rating_x2, created_at, updated_at) VALUES (?, ?, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", bia, blonde));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO reviews (user_id, daily_album_id, rating_x2, created_at, updated_at) VALUES (?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", bia, blonde));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE users SET handle = 'user' || CAST(? AS VARCHAR(10)) WHERE id = ?", ana, bia));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE users SET handle = 'Maiuscula' WHERE id = ?", bia));
    }
}
