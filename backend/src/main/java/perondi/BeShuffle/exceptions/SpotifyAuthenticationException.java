package perondi.BeShuffle.exceptions;

public class SpotifyAuthenticationException extends AlbumException {
    
    private static final long serialVersionUID = 1L;

    public SpotifyAuthenticationException(String message) {
        super(
                "Erro de autenticação Spotify: " + message,
                "SPOTIFY_AUTH_ERROR",
                401
        );
    }

    public SpotifyAuthenticationException(String message, Throwable cause) {
        super(
                "Erro de autenticação Spotify: " + message,
                "SPOTIFY_AUTH_ERROR",
                401,
                cause
        );
    }

    public SpotifyAuthenticationException(String message, int httpStatus) {
        super(
                "Erro de autenticação Spotify: " + message,
                "SPOTIFY_AUTH_ERROR",
                httpStatus
        );
    }
}

