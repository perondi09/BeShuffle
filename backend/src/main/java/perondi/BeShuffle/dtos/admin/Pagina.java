package perondi.BeShuffle.dtos.admin;

import java.util.List;

/** Uma página de resultados: page começa em 0; total é o número de itens em todas as páginas. */
public record Pagina<T>(List<T> items, int page, int size, long total) {
}
