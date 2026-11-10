package perondi.BeShuffle.exceptions;

public class EmailJaCadastradoException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public EmailJaCadastradoException() {
        super("Já existe uma conta com esse e-mail", "EMAIL_ALREADY_REGISTERED", 409);
    }
}
