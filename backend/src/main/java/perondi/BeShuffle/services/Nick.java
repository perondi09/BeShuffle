package perondi.BeShuffle.services;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Regras do nick (o "@nick" do perfil): de 3 a 20 letras minúsculas, números ou "_". Quem se cadastra recebe um nick
 * gerado a partir do nome e pode trocar depois; nomes que se passariam pela equipe ficam reservados.
 */
public final class Nick {

    public static final int MIN = 3;
    public static final int MAX = 20;

    private static final Pattern VALIDO = Pattern.compile("^[a-z0-9_]{" + MIN + "," + MAX + "}$");
    private static final Set<String> RESERVADOS =
            Set.of("admin", "administrador", "beshuffle", "moderador", "moderacao", "suporte", "sistema");

    private Nick() {
    }

    /** Tira espaços das pontas e passa para minúsculas, como o nick é guardado. */
    public static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean formatoValido(String nick) {
        return VALIDO.matcher(nick).matches();
    }

    public static boolean reservado(String nick) {
        return RESERVADOS.contains(nick);
    }

    /** "Marina Wav" → "marina_wav"; sem letras aproveitáveis (ex.: só emojis) → "user". Sempre no formato válido. */
    static String doNome(String nome) {
        String semAcento = Normalizer.normalize(nome == null ? "" : nome, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String base = semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.length() > MAX) {
            base = base.substring(0, MAX).replaceAll("_+$", "");
        }
        if (base.isEmpty()) {
            return "user";
        }
        return base.length() < MIN ? base + "user" : base;
    }

    /** O primeiro nick livre a partir do nome: "marina_wav", depois "marina_wav2", "marina_wav3"... */
    public static String unico(String nome, Predicate<String> jaExiste) {
        String base = doNome(nome);
        String candidato = base;
        for (int n = 2; reservado(candidato) || jaExiste.test(candidato); n++) {
            if (n > 9999) {
                throw new IllegalStateException("Não foi possível gerar um nick livre");
            }
            String sufixo = String.valueOf(n);
            candidato = base.substring(0, Math.min(base.length(), MAX - sufixo.length())) + sufixo;
        }
        return candidato;
    }
}
