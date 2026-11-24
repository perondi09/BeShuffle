package perondi.BeShuffle.exceptions;

/** O nick escolhido já é de outra conta. */
public class NickEmUsoException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public NickEmUsoException() {
        super("Esse nick já está em uso", "HANDLE_TAKEN", 409);
    }
}
