package perondi.BeShuffle.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Estilos musicais que a conta logada pode escolher no sorteio. Há dois jeitos de sortear um estilo:
 *
 * <ul>
 *   <li><b>Por gênero:</b> busca de faixas {@code genre:"..."} no Spotify (a busca de álbuns ignora gênero).
 *       Funciona bem para gêneros internacionais (rock, jazz, metal...).</li>
 *   <li><b>Por artistas:</b> uma lista nossa de artistas do gênero, e o sorteio busca {@code artist:"..."}.
 *       Usado onde a busca por gênero quase não traz nada (funk, sertanejo, pagode, piseiro, rap nacional) ou
 *       só traz artistas desconhecidos (hip hop).</li>
 * </ul>
 *
 * <p>Todos os artistas e gêneros foram testados contra a API do Spotify em 13/11/2026: cada artista tem álbuns
 * de verdade (não só singles) no mercado BR.
 */
public enum Estilo {
    ROCK("rock", "Rock", "rock"),
    INDIE("indie", "Indie", "indie"),
    METAL("metal", "Metal", "metal"),
    PUNK("punk", "Punk", "punk"),
    POP("pop", "Pop", "pop"),
    DANCE("dance", "Dance e eletrônica", "dance"),
    HIP_HOP("hip-hop", "Hip hop", List.of(
            "Kendrick Lamar", "Drake", "Kanye West", "J. Cole", "Travis Scott", "Eminem", "Tyler, The Creator",
            "Outkast", "A Tribe Called Quest", "50 Cent", "Snoop Dogg", "Mac Miller", "Lil Wayne")),
    RAP_NACIONAL("rap-nacional", "Rap nacional", List.of(
            "Emicida", "Criolo", "Djonga", "Matuê", "BK", "Filipe Ret", "Sabotage", "Marcelo D2",
            "Baco Exu do Blues", "Rashid", "Projota", "Xamã", "L7NNON", "Teto")),
    FUNK("funk", "Funk", List.of(
            "Anitta", "Ludmilla", "Pedro Sampaio", "MC Livinho", "MC Hariel", "Pocah", "Lexa", "MC Daniel",
            "Nego do Borel", "MC Cabelinho", "Tati Quebra Barraco")),
    SERTANEJO("sertanejo", "Sertanejo", List.of(
            "Jorge & Mateus", "Henrique & Juliano", "Marília Mendonça", "Gusttavo Lima", "Zé Neto & Cristiano",
            "Maiara & Maraisa", "Chitãozinho & Xororó", "Zezé Di Camargo & Luciano", "Luan Santana",
            "Bruno & Marrone", "Matheus & Kauan", "Simone Mendes", "Ana Castela")),
    PAGODE("pagode", "Pagode", List.of(
            "Thiaguinho", "Péricles", "Sorriso Maroto", "Turma do Pagode", "Ferrugem", "Exaltasamba",
            "Grupo Revelação", "Raça Negra", "Dilsinho", "Belo", "Pixote", "Só Pra Contrariar", "Zeca Pagodinho",
            "Mumuzinho")),
    PISEIRO("piseiro", "Piseiro", List.of(
            "João Gomes", "Vitor Fernandes", "Zé Vaqueiro", "Tarcísio do Acordeon", "Nattan", "Mari Fernandez",
            "Japãozin", "Felipe Amorim")),
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
    private final List<String> artistas;

    /** Estilo sorteado pela busca de gênero do Spotify. */
    Estilo(String id, String nome, String generoSpotify) {
        this(id, nome, generoSpotify, List.of());
    }

    /** Estilo sorteado a partir de uma lista de artistas. */
    Estilo(String id, String nome, List<String> artistas) {
        this(id, nome, null, artistas);
    }

    Estilo(String id, String nome, String generoSpotify, List<String> artistas) {
        this.id = id;
        this.nome = nome;
        this.generoSpotify = generoSpotify;
        this.artistas = List.copyOf(artistas);
    }

    public String id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    /** true quando o sorteio usa a lista de artistas em vez da busca por gênero. */
    public boolean porArtistas() {
        return !artistas.isEmpty();
    }

    public List<String> artistas() {
        return artistas;
    }

    /** Consulta para a busca de faixas do Spotify (só para estilos por gênero). */
    public String consulta() {
        if (porArtistas()) {
            throw new IllegalStateException("O estilo " + id + " é sorteado por artistas, não por gênero");
        }
        return "genre:\"" + generoSpotify + "\"";
    }

    public static Optional<Estilo> porId(String id) {
        return Arrays.stream(values()).filter(e -> e.id.equals(id)).findFirst();
    }
}
