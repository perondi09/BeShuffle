package perondi.BeShuffle.services;

import java.util.Arrays;
import java.util.Optional;

/**
 * Estilos musicais que a conta logada pode escolher no sorteio. Cada um vira uma busca de faixas
 * {@code genre:"..."} no Spotify (a busca de álbuns ignora gênero). Só entram gêneros que testamos e
 * que trazem álbuns de verdade: funk, sertanejo, pagode e piseiro quase não têm resultado no Spotify.
 */
public enum Estilo {
    ROCK("rock", "Rock", "rock"),
    INDIE("indie", "Indie", "indie"),
    METAL("metal", "Metal", "metal"),
    PUNK("punk", "Punk", "punk"),
    POP("pop", "Pop", "pop"),
    DANCE("dance", "Dance e eletrônica", "dance"),
    HIP_HOP("hip-hop", "Hip hop", "hip hop"),
    JAZZ("jazz", "Jazz", "jazz"),
    BLUES("blues", "Blues", "blues"),
    SOUL("soul", "Soul", "soul"),
    REGGAE("reggae", "Reggae", "reggae"),
    FOLK("folk", "Folk", "folk"),
    COUNTRY("country", "Country", "country"),
    LATINA("latina", "Música latina", "latin"),
    MPB("mpb", "MPB", "mpb"),
    SAMBA("samba", "Samba", "samba"),
    FORRO("forro", "Forró", "forro"),
    GOSPEL("gospel", "Gospel", "christian"),
    CLASSICA("classica", "Clássica", "classical");

    private final String id;
    private final String nome;
    private final String generoSpotify;

    Estilo(String id, String nome, String generoSpotify) {
        this.id = id;
        this.nome = nome;
        this.generoSpotify = generoSpotify;
    }

    public String id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    /** Consulta para a busca de faixas do Spotify. */
    public String consulta() {
        return "genre:\"" + generoSpotify + "\"";
    }

    public static Optional<Estilo> porId(String id) {
        return Arrays.stream(values()).filter(e -> e.id.equals(id)).findFirst();
    }
}
