package perondi.BeShuffle.exceptions;

public class SpotifyApiException extends AlbumException {
    
    private static final long serialVersionUID = 1L;

    public SpotifyApiException(String message) {
        super(
                "Erro ao comunicar com API Spotify: " + message,
                "SPOTIFY_API_ERROR",
                503
        );
    }

    public SpotifyApiException(String message, Throwable cause) {
        super(
                "Erro ao comunicar com API Spotify: " + message,
                "SPOTIFY_API_ERROR",
                503,
                cause
        );
    }

    public SpotifyApiException(String message, int httpStatus) {
        super(
                "Erro ao comunicar com API Spotify: " + message,
                "SPOTIFY_API_ERROR",
                httpStatus
        );
    }

    public SpotifyApiException(String message, int httpStatus, Throwable cause) {
        super(
                "Erro ao comunicar com API Spotify: " + message,
                "SPOTIFY_API_ERROR",
                httpStatus,
                cause
        );
    }
}

