package perondi.BeShuffle.dtos.auth;

/** Corpo de POST /api/auth/login (frontend/src/api/auth.ts). */
public record LoginRequest(String email, String password) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
