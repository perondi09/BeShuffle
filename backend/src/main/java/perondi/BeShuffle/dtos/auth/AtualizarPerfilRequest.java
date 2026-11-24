package perondi.BeShuffle.dtos.auth;

/** Corpo de PUT /api/me. Campo ausente (null) fica como está. */
public record AtualizarPerfilRequest(String displayName, String handle, String avatar) {
}
