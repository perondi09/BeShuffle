package perondi.BeShuffle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Regras do sorteio de álbuns ({@code beshuffle.sorteio.*}).
 *
 * <p>O app do Spotify em Development Mode não recebe {@code popularity}, então a "popularidade" vem da
 * própria ordem da busca: os primeiros resultados de {@code year:ANO} são os álbuns mais ouvidos daquele ano.
 *
 * @param mercado          país usado na busca (afeta o que é "popular"); vazio para não filtrar
 * @param anoInicial       primeiro ano que pode ser sorteado; o último é o ano atual
 * @param paginas          quantas páginas do começo da busca entram no sorteio
 * @param tamanhoPagina    itens por página (o Spotify aceita no máximo 10 no Development Mode)
 * @param tentativas       buscas antes de desistir
 * @param minFaixas        menos faixas que isso é EP ou single disfarçado
 * @param maxFaixas        mais faixas que isso costuma ser box, deluxe gigante ou coletânea
 * @param termosBloqueados palavras que tiram o álbum do sorteio (nome ou artista; sem acento e minúsculas)
 */
@ConfigurationProperties("beshuffle.sorteio")
public record SorteioProperties(
        @DefaultValue("BR") String mercado,
        @DefaultValue("1960") int anoInicial,
        @DefaultValue("5") int paginas,
        @DefaultValue("10") int tamanhoPagina,
        @DefaultValue("5") int tentativas,
        @DefaultValue("6") int minFaixas,
        @DefaultValue("40") int maxFaixas,
        @DefaultValue({
                "ninar", "infantil", "infantis", "kids", "children", "lullaby", "lullabies",
                "karaoke", "playback", "tribute", "tributo", "homenagem",
                "coletanea", "greatest hits", "grandes sucessos", "grandes exitos", "mejores",
                "best of", "as melhores", "os melhores"
        }) List<String> termosBloqueados
) {

    public SorteioProperties {
        if (tamanhoPagina < 1 || tamanhoPagina > 10) {
            throw new IllegalArgumentException("beshuffle.sorteio.tamanho-pagina deve ficar entre 1 e 10");
        }
        if (paginas < 1 || tentativas < 1) {
            throw new IllegalArgumentException("beshuffle.sorteio.paginas e tentativas devem ser pelo menos 1");
        }
        if (minFaixas > maxFaixas) {
            throw new IllegalArgumentException("beshuffle.sorteio.min-faixas não pode passar de max-faixas");
        }
        termosBloqueados = termosBloqueados == null ? List.of() : List.copyOf(termosBloqueados);
    }

    /** Mesmos valores dos {@code @DefaultValue}, para testes e uso sem Spring. */
    public static SorteioProperties padrao() {
        return new SorteioProperties("BR", 1960, 5, 10, 5, 6, 40, List.of(
                "ninar", "infantil", "infantis", "kids", "children", "lullaby", "lullabies",
                "karaoke", "playback", "tribute", "tributo", "homenagem",
                "coletanea", "greatest hits", "grandes sucessos", "grandes exitos", "mejores",
                "best of", "as melhores", "os melhores"));
    }
}
