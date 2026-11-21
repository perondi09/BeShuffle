package perondi.BeShuffle.exceptions;

/** Conta, nota ou outro registro que o pedido citou e não existe. */
public class NaoEncontradoException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public NaoEncontradoException(String mensagem) {
        super(mensagem, "NOT_FOUND", 404);
    }
}
