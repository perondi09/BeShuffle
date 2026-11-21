package perondi.BeShuffle.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import perondi.BeShuffle.dtos.admin.ComentarioAdmin;
import perondi.BeShuffle.dtos.admin.UsuarioAdmin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Consultas do painel de admin e o registro de ações (tabela admin_actions). Só leitura, fora o registro e a moderação. */
@Repository
public class AdminRepository {

    private final JdbcTemplate jdbc;

    public AdminRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Visitantes únicos no período: total, quantos eram contas logadas e quantos usaram celular. */
    public record Visitas(long total, long logados, long celular) {
    }

    /** Uma linha de admin_actions com os nomes de quem fez e de quem sofreu a ação. */
    public record Acao(String action, String detail, Instant at, String adminName, String targetName) {
    }

    // ---------- acessos

    public Visitas visitas(LocalDate de, LocalDate ate, String area) {
        List<Object> args = new ArrayList<>(List.of(de, ate));
        String sql = """
                SELECT COUNT(DISTINCT visitor_key),
                       COUNT(DISTINCT CASE WHEN logged_in THEN visitor_key END),
                       COUNT(DISTINCT CASE WHEN mobile THEN visitor_key END)
                FROM daily_visits WHERE visit_date BETWEEN ? AND ?""" + filtroArea(area, args);
        return jdbc.queryForObject(sql, (rs, i) -> new Visitas(rs.getLong(1), rs.getLong(2), rs.getLong(3)),
                args.toArray());
    }

    /** Quantos visitantes do período apareceram em pelo menos dois dias diferentes. */
    public long contarQueVoltaram(LocalDate de, LocalDate ate, String area) {
        List<Object> args = new ArrayList<>(List.of(de, ate));
        String sql = "SELECT COUNT(*) FROM (SELECT visitor_key FROM daily_visits WHERE visit_date BETWEEN ? AND ?"
                + filtroArea(area, args)
                + " GROUP BY visitor_key HAVING COUNT(DISTINCT visit_date) >= 2) voltaram";
        return contar(sql, args.toArray());
    }

    public Map<LocalDate, Long> visitasPorDia(LocalDate de, LocalDate ate, String area) {
        List<Object> args = new ArrayList<>(List.of(de, ate));
        String sql = "SELECT visit_date, COUNT(DISTINCT visitor_key) FROM daily_visits WHERE visit_date BETWEEN ? AND ?"
                + filtroArea(area, args) + " GROUP BY visit_date";
        Map<LocalDate, Long> porDia = new HashMap<>();
        jdbc.query(sql, rs -> {
            porDia.put(rs.getObject(1, LocalDate.class), rs.getLong(2));
        }, args.toArray());
        return porDia;
    }

    // ---------- contas, sorteios e notas

    public long contarContas() {
        return contar("SELECT COUNT(*) FROM users");
    }

    public long contarBanidas() {
        return contar("SELECT COUNT(*) FROM users WHERE banned_at IS NOT NULL");
    }

    public long contarContasDesde(Instant desde) {
        return contar("SELECT COUNT(*) FROM users WHERE created_at >= ?", utc(desde));
    }

    public List<Instant> criacoesDeContaDesde(Instant desde) {
        return jdbc.query("SELECT created_at FROM users WHERE created_at >= ?",
                (rs, i) -> instante(rs, "created_at"), utc(desde));
    }

    public long contarSorteios(LocalDate de, LocalDate ate) {
        return contar("SELECT COUNT(*) FROM draws WHERE draw_date BETWEEN ? AND ?", de, ate);
    }

    public long contarNotasDesde(Instant desde) {
        return contar("SELECT COUNT(*) FROM reviews WHERE created_at >= ?", utc(desde));
    }

    public long contarNotasDoAlbum(Long dailyAlbumId) {
        return contar("SELECT COUNT(*) FROM reviews WHERE daily_album_id = ?", dailyAlbumId);
    }

    // ---------- lista de contas

    private static final String SELECT_USUARIO = """
            SELECT u.id, u.email, u.display_name, u.role, u.created_at, u.banned_at, u.banned_reason,
                   (SELECT COUNT(*) FROM draws d WHERE d.user_id = u.id) AS draws,
                   (SELECT COUNT(*) FROM reviews r WHERE r.user_id = u.id) AS reviews
            FROM users u""";

    private static final RowMapper<UsuarioAdmin> USUARIO = (rs, i) -> new UsuarioAdmin(
            rs.getLong("id"), rs.getString("email"), rs.getString("display_name"), rs.getString("role"),
            instante(rs, "created_at"), instante(rs, "banned_at"), rs.getString("banned_reason"),
            rs.getLong("draws"), rs.getLong("reviews"));

    public List<UsuarioAdmin> usuarios(String busca, String status, int limite, int deslocamento) {
        List<Object> args = new ArrayList<>();
        String sql = SELECT_USUARIO + filtroUsuarios(busca, status, args)
                + " ORDER BY u.created_at DESC, u.id DESC LIMIT ? OFFSET ?";
        args.add(limite);
        args.add(deslocamento);
        return jdbc.query(sql, USUARIO, args.toArray());
    }

    public long contarUsuarios(String busca, String status) {
        List<Object> args = new ArrayList<>();
        return contar("SELECT COUNT(*) FROM users u" + filtroUsuarios(busca, status, args), args.toArray());
    }

    public Optional<UsuarioAdmin> usuario(Long id) {
        return jdbc.query(SELECT_USUARIO + " WHERE u.id = ?", USUARIO, id).stream().findFirst();
    }

    /** Busca no e-mail e no nome (sem diferenciar maiúsculas) e filtra por status: todos, ativos, banidos ou admins. */
    private static String filtroUsuarios(String busca, String status, List<Object> args) {
        List<String> condicoes = new ArrayList<>();
        if (busca != null && !busca.isBlank()) {
            String padrao = "%" + escaparLike(busca.trim().toLowerCase(Locale.ROOT)) + "%";
            condicoes.add("(LOWER(u.email) LIKE ? ESCAPE '\\' OR LOWER(u.display_name) LIKE ? ESCAPE '\\')");
            args.add(padrao);
            args.add(padrao);
        }
        switch (status) {
            case "ativos" -> condicoes.add("u.banned_at IS NULL");
            case "banidos" -> condicoes.add("u.banned_at IS NOT NULL");
            case "admins" -> condicoes.add("u.role = 'ADMIN'");
            default -> { }
        }
        return condicoes.isEmpty() ? "" : " WHERE " + String.join(" AND ", condicoes);
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // ---------- comentários (moderação)

    private static final String SELECT_COMENTARIO = """
            SELECT r.id, r.user_id, u.display_name, u.email, r.rating, r.comment, r.updated_at,
                   a.album_name, a.display_date
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            JOIN daily_albums a ON a.id = r.daily_album_id
            WHERE r.comment IS NOT NULL""";

    private static final RowMapper<ComentarioAdmin> COMENTARIO = (rs, i) -> new ComentarioAdmin(
            rs.getLong("id"), rs.getLong("user_id"), rs.getString("display_name"), rs.getString("email"),
            rs.getInt("rating"), rs.getString("comment"), instante(rs, "updated_at"),
            rs.getString("album_name"), rs.getObject("display_date", LocalDate.class), "musica");

    public List<ComentarioAdmin> comentarios(int limite, int deslocamento) {
        return jdbc.query(SELECT_COMENTARIO + " ORDER BY r.updated_at DESC, r.id DESC LIMIT ? OFFSET ?",
                COMENTARIO, limite, deslocamento);
    }

    public long contarComentarios() {
        return contar("SELECT COUNT(*) FROM reviews WHERE comment IS NOT NULL");
    }

    public Optional<ComentarioAdmin> comentario(Long reviewId) {
        return jdbc.query(SELECT_COMENTARIO + " AND r.id = ?", COMENTARIO, reviewId).stream().findFirst();
    }

    /** Apaga só o texto do comentário; a nota continua valendo. */
    public void removerComentario(Long reviewId) {
        jdbc.update("UPDATE reviews SET comment = NULL WHERE id = ?", reviewId);
    }

    // ---------- registro de ações

    public void registrarAcao(Long adminId, String acao, Long alvoId, String detalhe, Instant quando) {
        String texto = detalhe == null ? null : (detalhe.length() > 500 ? detalhe.substring(0, 500) : detalhe);
        jdbc.update("""
                INSERT INTO admin_actions (admin_id, action, target_user_id, detail, created_at)
                VALUES (?, ?, ?, ?, ?)""", adminId, acao, alvoId, texto, utc(quando));
    }

    public List<Acao> acoesRecentes(int limite) {
        return jdbc.query("""
                SELECT a.action, a.detail, a.created_at, adm.display_name AS admin_name, alvo.display_name AS target_name
                FROM admin_actions a
                LEFT JOIN users adm ON adm.id = a.admin_id
                LEFT JOIN users alvo ON alvo.id = a.target_user_id
                ORDER BY a.created_at DESC, a.id DESC
                LIMIT ?""", (rs, i) -> new Acao(rs.getString("action"), rs.getString("detail"),
                instante(rs, "created_at"), rs.getString("admin_name"), rs.getString("target_name")), limite);
    }

    /** Contas mais novas: nome de exibição e quando foram criadas. */
    public List<Map.Entry<String, Instant>> contasRecentes(int limite) {
        return jdbc.query("SELECT display_name, created_at FROM users ORDER BY created_at DESC, id DESC LIMIT ?",
                (rs, i) -> Map.entry(rs.getString("display_name"), instante(rs, "created_at")), limite);
    }

    // ---------- ajudas

    private static String filtroArea(String area, List<Object> args) {
        if (area == null) {
            return "";
        }
        args.add(area);
        return " AND area = ?";
    }

    private long contar(String sql, Object... args) {
        Long n = jdbc.queryForObject(sql, Long.class, args);
        return n == null ? 0 : n;
    }

    private static OffsetDateTime utc(Instant instante) {
        return OffsetDateTime.ofInstant(instante, ZoneOffset.UTC);
    }

    private static Instant instante(ResultSet rs, String coluna) throws SQLException {
        OffsetDateTime valor = rs.getObject(coluna, OffsetDateTime.class);
        return valor == null ? null : valor.toInstant();
    }
}
