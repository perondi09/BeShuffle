package perondi.BeShuffle.services;

/**
 * Quantos sorteios cada pessoa tem por dia, tudo grátis: visitante sorteia 1, conta logada sorteia 10
 * (e pode escolher estilos musicais). O Álbum do Dia é à parte e não gasta sorteio.
 */
public enum Plano {
    VISITANTE(1),
    LOGADO(10);

    /**
     * Teto de sorteios de visitantes vindos da mesma rede (IP) por dia. Alto o bastante para não barrar quem
     * divide IP com muita gente (CGNAT de operadora, Wi-Fi público), baixo para quem apaga cookies em série.
     */
    public static final int VISITANTES_POR_REDE = 20;

    private final int sorteiosPorDia;

    Plano(int sorteiosPorDia) {
        this.sorteiosPorDia = sorteiosPorDia;
    }

    public int sorteiosPorDia() {
        return sorteiosPorDia;
    }
}
