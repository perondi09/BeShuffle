package perondi.BeShuffle.dtos.admin;

import java.time.Instant;

/**
 * Um item de "Atividade recente". type: ACCOUNT_CREATED, BAN, UNBAN, REMOVE_COMMENT ou REGENERATE_DAILY_ALBUM.
 * area: "musica", "filmes" ou nulo quando o evento vale para o site todo.
 */
public record EventoAdmin(String type, String description, String area, Instant at) {
}
