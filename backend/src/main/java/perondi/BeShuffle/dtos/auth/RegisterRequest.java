package perondi.BeShuffle.dtos.auth;

/** Corpo de POST /api/auth/register (frontend/src/api/auth.ts). */
public record RegisterRequest(String displayName, String email, String password) {

    @Override
    public String toString() {
        return "RegisterRequest[displayName=" + displayName + ", email=" + email + ", password=***]";
    }
}
