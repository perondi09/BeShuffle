package perondi.BeShuffle.exceptions;

import perondi.BeShuffle.services.Plano;

/** Os sorteios do dia acabaram. 429 para o site mostrar "volte amanhã" em vez de erro genérico. */
public class LimiteDeSorteiosException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public LimiteDeSorteiosException(Plano plano) {
        super(mensagem(plano), "DAILY_LIMIT_REACHED", 429);
    }

    private static String mensagem(Plano plano) {
        return plano == Plano.VISITANTE
                ? "Você já usou o sorteio de hoje. Crie uma conta grátis para sortear até 10 álbuns por dia!"
                : "Você já usou os " + plano.sorteiosPorDia() + " sorteios de hoje. Volte amanhã!";
    }
}
