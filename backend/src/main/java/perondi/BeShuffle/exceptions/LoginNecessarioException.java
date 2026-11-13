package perondi.BeShuffle.exceptions;

/** Recurso que só a conta logada usa (ex.: escolher estilos no sorteio). */
public class LoginNecessarioException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public LoginNecessarioException(String mensagem) {
        super(mensagem, "UNAUTHORIZED", 401);
    }
}
