package perondi.BeShuffle.services;

/**
 * Quem está sorteando: uma conta (userId) ou um visitante (anonId do cookie bs_anon). O visitante também leva
 * o hash do IP ({@code ipHash}), que limita quantos visitantes da mesma rede sorteiam por dia.
 */
public record Dono(Long userId, String anonId, String ipHash) {

    public Dono {
        if ((userId == null) == (anonId == null || anonId.isBlank())) {
            throw new IllegalArgumentException("Dono precisa de userId ou de anonId, nunca dos dois");
        }
    }

    public static Dono usuario(Long userId) {
        return new Dono(userId, null, null);
    }

    public static Dono visitante(String anonId, String ipHash) {
        return new Dono(null, anonId, ipHash);
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

    /** Chave da rede do visitante na draw_quota; null para conta logada ou sem IP. */
    public String chaveDaRede() {
        return logado() || ipHash == null || ipHash.isBlank() ? null : "i:" + ipHash;
    }
}
