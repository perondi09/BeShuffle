package perondi.BeShuffle.exceptions;

import perondi.BeShuffle.services.Plano;

/** Os sorteios do dia acabaram. 429 para o site mostrar "volte amanhã" em vez de erro genérico. */
public class LimiteDeSorteiosException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public LimiteDeSorteiosException(Plano plano) {
        this(mensagem(plano));
    }

    private LimiteDeSorteiosException(String mensagem) {
        super(mensagem, "DAILY_LIMIT_REACHED", 429);
    }

    /** Muitos visitantes da mesma rede sortearam hoje (teto por IP). */
    public static LimiteDeSorteiosException daRede() {
        return new LimiteDeSorteiosException(
                "Muitos sorteios saíram desta rede hoje. Crie uma conta grátis para sortear até 10 álbuns por dia!");
    }

    private static String mensagem(Plano plano) {
        return plano == Plano.VISITANTE
                ? "Você já usou o sorteio de hoje. Crie uma conta grátis para sortear até 10 álbuns por dia!"
                : "Você já usou os " + plano.sorteiosPorDia() + " sorteios de hoje. Volte amanhã!";
    }
}
