package perondi.BeShuffle.dtos.admin;

/** Corpo de POST /api/admin/users/{id}/ban. O motivo fica no registro de ações e na conta. */
public record BanirRequest(String reason) {
}
