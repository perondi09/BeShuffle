package perondi.BeShuffle.services;

/** Quem está sorteando: uma conta (userId) ou um visitante (anonId do cookie bs_anon). */
public record Dono(Long userId, String anonId) {

    public Dono {
        if ((userId == null) == (anonId == null || anonId.isBlank())) {
            throw new IllegalArgumentException("Dono precisa de userId ou de anonId, nunca dos dois");
        }
    }

    public static Dono usuario(Long userId) {
        return new Dono(userId, null);
    }

    public static Dono visitante(String anonId) {
        return new Dono(null, anonId);
    }

    public boolean logado() {
        return userId != null;
    }

    public Plano plano() {
        return logado() ? Plano.LOGADO : Plano.VISITANTE;
    }

    /** Chave da tabela draw_quota. */
    public String chave() {
        return logado() ? "u:" + userId : "a:" + anonId;
    }
}
