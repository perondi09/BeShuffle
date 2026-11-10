package perondi.BeShuffle.exceptions;

/** Mesma mensagem para e-mail inexistente e senha errada, para não revelar quais e-mails têm conta. */
public class CredenciaisInvalidasException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos", "INVALID_CREDENTIALS", 401);
    }
}
