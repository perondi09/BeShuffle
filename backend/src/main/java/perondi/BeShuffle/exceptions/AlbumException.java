package perondi.BeShuffle.exceptions;

public class AlbumException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    private int httpStatus;

    public AlbumException(String message) {
        super(message);
        this.httpStatus = 500;
    }

    public AlbumException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 500;
    }

    public AlbumException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public AlbumException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AlbumException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return "AlbumException{" + "errorCode='" + errorCode + '\'' + ", httpStatus=" + httpStatus + ", message='" + getMessage() + '\'' + '}';
    }
}

