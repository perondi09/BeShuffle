package perondi.BeShuffle.exceptions;

/**
 * Exceção lançada quando um álbum já foi utilizado anteriormente como álbum do dia
 * HTTP Status: 400 Bad Request
 */
public class AlbumAlreadyUsedException extends AlbumException {
    
    private static final long serialVersionUID = 1L;

    public AlbumAlreadyUsedException(String albumId) {
        super(
                "Este álbum já foi exibido: " + albumId,
                "ALBUM_ALREADY_USED",
                400
        );
    }

    public AlbumAlreadyUsedException(String albumId, String albumName) {
        super(
                "Este álbum já foi exibido: " + albumName + " (ID: " + albumId + ")",
                "ALBUM_ALREADY_USED",
                400
        );
    }

    public AlbumAlreadyUsedException(String message, String albumId, Throwable cause) {
        super(
                message + " (ID: " + albumId + ")",
                "ALBUM_ALREADY_USED",
                400,
                cause
        );
    }
}

