package perondi.BeShuffle.dtos.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Números do painel de admin (GET /api/admin/stats) para o período e a área pedidos.
 * Percentuais com uma casa decimal; nulos quando não há base para calcular (ex.: nenhum acesso no período).
 *
 * @param visits         visitantes únicos (por conta, cookie bs_anon ou IP) que abriram o site no período
 * @param active7Days    contas logadas que abriram o site nos últimos 7 dias
 * @param conversionPct  contas novas sobre visitantes sem conta, no período
 * @param return7DaysPct quem voltou em pelo menos 2 dias diferentes nos últimos 7 dias
 * @param byArea         "musica" e "filmes"
 * @param last30Days     acessos e contas novas por dia, do mais antigo ao de hoje
 */
public record PainelAdmin(
        String period,
        String area,
        LocalDate from,
        LocalDate to,
        long newAccounts,
        long totalAccounts,
        long visits,
        long visitorVisits,
        long loggedVisits,
        long active7Days,
        long draws,
        long reviews,
        long bannedAccounts,
        Double conversionPct,
        Double mobilePct,
        Double return7DaysPct,
        Map<String, ResumoArea> byArea,
        List<PontoDoDia> last30Days) {

    public record ResumoArea(long visits, long draws, long reviews) {
    }

    public record PontoDoDia(LocalDate date, long visits, long newAccounts) {
    }
}
