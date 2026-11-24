package perondi.BeShuffle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Conta de usuário. Chama AppUser para não confundir com o User do Spring Security. */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 60)
    private String displayName;

    /** O "@nick" do perfil: único, minúsculo, de 3 a 20 letras, números ou "_" (ver services/Nick). */
    @Column(nullable = false, unique = true, length = 20)
    private String handle;

    /** Cor do avatar (uma das de {@link #AVATARES}); o desenho é a inicial do nome sobre essa cor. */
    @Column(name = "avatar_key", nullable = false, length = 20)
    private String avatarKey = AVATAR_PADRAO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** USER ou ADMIN (ver {@link #ADMIN}). */
    @Column(nullable = false, length = 10)
    private String role = USER;

    /** Quando a conta foi banida; nulo se ela está ativa. */
    @Column(name = "banned_at")
    private Instant bannedAt;

    @Column(name = "banned_reason", length = 300)
    private String bannedReason;

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";

    public static final String AVATAR_PADRAO = "rosa";
    /** Os avatares do design system (docs/DESIGN-SYSTEM.md). Foto de perfil fica para uma etapa futura. */
    public static final java.util.Set<String> AVATARES = java.util.Set.of("rosa", "menta", "gema", "lilas");

    public boolean isAdmin() {
        return ADMIN.equals(role);
    }

    public boolean isBanida() {
        return bannedAt != null;
    }
}
