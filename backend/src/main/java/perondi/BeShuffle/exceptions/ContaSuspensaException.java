package perondi.BeShuffle.exceptions;

/** Login de uma conta banida por um admin. Só aparece depois da senha conferir. */
public class ContaSuspensaException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public ContaSuspensaException() {
        super("Esta conta foi suspensa", "ACCOUNT_SUSPENDED", 403);
    }
}
