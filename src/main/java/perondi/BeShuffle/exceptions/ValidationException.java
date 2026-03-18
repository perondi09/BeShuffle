package perondi.BeShuffle.exceptions;

public class ValidationException extends AlbumException {
    
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(
                "Erro de validação: " + message,
                "VALIDATION_ERROR",
                400
        );
    }

    public ValidationException(String message, String field) {
        super(
                "Erro de validação no campo '" + field + "': " + message,
                "VALIDATION_ERROR",
                400
        );
    }

    public ValidationException(String message, String field, Throwable cause) {
        super(
                "Erro de validação no campo '" + field + "': " + message,
                "VALIDATION_ERROR",
                400,
                cause
        );
    }
}

