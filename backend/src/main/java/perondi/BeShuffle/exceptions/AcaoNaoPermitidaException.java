package perondi.BeShuffle.exceptions;

/** Ação de admin que as regras não deixam fazer (banir a si mesmo, banir outro admin, regerar álbum com notas). */
public class AcaoNaoPermitidaException extends AlbumException {

    private static final long serialVersionUID = 1L;

    public AcaoNaoPermitidaException(String mensagem) {
        super(mensagem, "ACTION_NOT_ALLOWED", 409);
    }
}
