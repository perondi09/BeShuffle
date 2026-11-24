package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import perondi.BeShuffle.dtos.admin.AlbumRegerado;
import perondi.BeShuffle.dtos.admin.ComentarioAdmin;
import perondi.BeShuffle.dtos.admin.EventoAdmin;
import perondi.BeShuffle.dtos.Pagina;
import perondi.BeShuffle.dtos.admin.PainelAdmin;
import perondi.BeShuffle.dtos.admin.PainelAdmin.PontoDoDia;
import perondi.BeShuffle.dtos.admin.PainelAdmin.ResumoArea;
import perondi.BeShuffle.dtos.admin.UsuarioAdmin;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.AppUser;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.exceptions.AcaoNaoPermitidaException;
import perondi.BeShuffle.exceptions.NaoEncontradoException;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.repository.AdminRepository;
import perondi.BeShuffle.repository.AppUserRepository;
import perondi.BeShuffle.repository.DailyAlbumRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static perondi.BeShuffle.services.AcessoService.FILMES;
import static perondi.BeShuffle.services.AcessoService.MUSICA;

/**
 * Painel de admin: números do site todo (com filtro por área), lista de contas, banimento, moderação de comentários
 * e regerar o Álbum do Dia. Toda ação que muda algo fica em admin_actions. Só sorteios e notas de música existem
 * por enquanto; a área "filmes" já aparece zerada para o painel não mudar quando a Fase E chegar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    static final int TAMANHO_PADRAO = 20;
    static final int TAMANHO_MAXIMO = 50;
    static final int MIN_MOTIVO = 3;
    static final int MAX_MOTIVO = 300;
    static final int EVENTOS = 20;

    private final AdminRepository repository;
    private final AppUserRepository users;
    private final DailyAlbumRepository dailyAlbums;
    private final DailyAlbumService dailyAlbumService;
    private final Clock clock;

    // ---------- painel

    public PainelAdmin painel(String periodo, String area) {
        int dias = switch (periodo == null ? "7d" : periodo) {
            case "hoje" -> 1;
            case "7d" -> 7;
            case "30d" -> 30;
            default -> throw new ValidationException("período inválido: use hoje, 7d ou 30d", "periodo");
        };
        String areaPedida = area == null ? "tudo" : area;
        String filtro = switch (areaPedida) {
            case "tudo" -> null;
            case MUSICA, FILMES -> areaPedida;
            default -> throw new ValidationException("área inválida: use tudo, musica ou filmes", "area");
        };

        ZoneId zona = clock.getZone();
        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicio = hoje.minusDays(dias - 1L);
        Instant desde = inicio.atStartOfDay(zona).toInstant();

        AdminRepository.Visitas visitas = repository.visitas(inicio, hoje, filtro);
        long visitantes = visitas.total() - visitas.logados();
        long contasNovas = repository.contarContasDesde(desde);

        long sorteiosMusica = repository.contarSorteios(inicio, hoje);
        long notasMusica = repository.contarNotasDesde(desde);
        boolean comMusica = filtro == null || MUSICA.equals(filtro);

        LocalDate inicioSemana = hoje.minusDays(6);
        AdminRepository.Visitas semana = repository.visitas(inicioSemana, hoje, filtro);
        long voltaram = repository.contarQueVoltaram(inicioSemana, hoje, filtro);

        Map<String, ResumoArea> porArea = new LinkedHashMap<>();
        porArea.put(MUSICA, new ResumoArea(repository.visitas(inicio, hoje, MUSICA).total(), sorteiosMusica, notasMusica));
        porArea.put(FILMES, new ResumoArea(repository.visitas(inicio, hoje, FILMES).total(), 0, 0));

        return new PainelAdmin(periodo == null ? "7d" : periodo, areaPedida, inicio, hoje,
                contasNovas, repository.contarContas(),
                visitas.total(), visitantes, visitas.logados(), semana.logados(),
                comMusica ? sorteiosMusica : 0, comMusica ? notasMusica : 0, repository.contarBanidas(),
                percentual(contasNovas, visitantes), percentual(visitas.celular(), visitas.total()),
                percentual(voltaram, semana.total()),
                porArea, ultimos30Dias(hoje, zona, filtro));
    }

    private List<PontoDoDia> ultimos30Dias(LocalDate hoje, ZoneId zona, String filtro) {
        LocalDate inicio = hoje.minusDays(29);
        Map<LocalDate, Long> visitasPorDia = repository.visitasPorDia(inicio, hoje, filtro);
        Map<LocalDate, Long> contasPorDia = new HashMap<>();
        for (Instant criada : repository.criacoesDeContaDesde(inicio.atStartOfDay(zona).toInstant())) {
            contasPorDia.merge(LocalDate.ofInstant(criada, zona), 1L, Long::sum);
        }
        List<PontoDoDia> serie = new ArrayList<>(30);
        for (LocalDate dia = inicio; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
            serie.add(new PontoDoDia(dia, visitasPorDia.getOrDefault(dia, 0L), contasPorDia.getOrDefault(dia, 0L)));
        }
        return serie;
    }

    static Double percentual(long parte, long base) {
        if (base <= 0) {
            return null;
        }
        return Math.round(parte * 1000.0 / base) / 10.0;
    }

    // ---------- contas

    public Pagina<UsuarioAdmin> usuarios(String busca, String status, int pagina, int tamanho) {
        String filtro = status == null ? "todos" : status;
        if (!List.of("todos", "ativos", "banidos", "admins").contains(filtro)) {
            throw new ValidationException("status inválido: use todos, ativos, banidos ou admins", "status");
        }
        int tam = tamanho(tamanho);
        int pag = Math.max(pagina, 0);
        return new Pagina<>(repository.usuarios(busca, filtro, tam, pag * tam), pag, tam,
                repository.contarUsuarios(busca, filtro));
    }

    @Transactional
    public UsuarioAdmin banir(Long adminId, Long alvoId, String motivo) {
        AppUser alvo = users.findById(alvoId).orElseThrow(() -> new NaoEncontradoException("Conta não encontrada"));
        if (alvoId.equals(adminId)) {
            throw new AcaoNaoPermitidaException("Você não pode banir a própria conta");
        }
        if (alvo.isAdmin()) {
            throw new AcaoNaoPermitidaException("Não dá para banir uma conta de admin");
        }
        String texto = motivo == null ? "" : motivo.trim();
        if (texto.length() < MIN_MOTIVO || texto.length() > MAX_MOTIVO) {
            throw new ValidationException("o motivo precisa ter entre " + MIN_MOTIVO + " e " + MAX_MOTIVO
                    + " caracteres", "reason");
        }
        Instant agora = clock.instant();
        alvo.setBannedAt(agora);
        alvo.setBannedReason(texto);
        users.save(alvo);
        repository.registrarAcao(adminId, "BAN", alvoId, texto, agora);
        log.info("Admin {} baniu a conta {}", adminId, alvoId);
        return atualizado(alvoId);
    }

    @Transactional
    public UsuarioAdmin desbanir(Long adminId, Long alvoId) {
        AppUser alvo = users.findById(alvoId).orElseThrow(() -> new NaoEncontradoException("Conta não encontrada"));
        if (alvo.isBanida()) {
            alvo.setBannedAt(null);
            alvo.setBannedReason(null);
            users.save(alvo);
            repository.registrarAcao(adminId, "UNBAN", alvoId, null, clock.instant());
            log.info("Admin {} desbaniu a conta {}", adminId, alvoId);
        }
        return atualizado(alvoId);
    }

    private UsuarioAdmin atualizado(Long id) {
        users.flush();
        return repository.usuario(id).orElseThrow(() -> new NaoEncontradoException("Conta não encontrada"));
    }

    // ---------- moderação

    public Pagina<ComentarioAdmin> comentarios(int pagina, int tamanho) {
        int tam = tamanho(tamanho);
        int pag = Math.max(pagina, 0);
        return new Pagina<>(repository.comentarios(tam, pag * tam), pag, tam, repository.contarComentarios());
    }

    @Transactional
    public void removerComentario(Long adminId, Long reviewId) {
        ComentarioAdmin comentario = repository.comentario(reviewId)
                .orElseThrow(() -> new NaoEncontradoException("Comentário não encontrado"));
        repository.removerComentario(reviewId);
        repository.registrarAcao(adminId, "REMOVE_COMMENT", comentario.userId(), comentario.comment(), clock.instant());
        log.info("Admin {} removeu o comentário da nota {}", adminId, reviewId);
    }

    // ---------- Álbum do Dia

    /**
     * Troca o Álbum do Dia de hoje (ex.: veio sem capa ou saiu do Spotify). Recusa se o disco já tem notas,
     * porque apagar o registro apagaria as notas junto.
     */
    public AlbumRegerado regerarAlbumDoDia(Long adminId) {
        LocalDate hoje = dailyAlbumService.hoje();
        Optional<DailyAlbum> atual = dailyAlbums.findByDisplayDate(hoje);
        String anterior = atual.map(DailyAlbum::getAlbumName).orElse(null);
        if (atual.isPresent()) {
            if (repository.contarNotasDoAlbum(atual.get().getId()) > 0) {
                throw new AcaoNaoPermitidaException("O Álbum do Dia de hoje já tem notas; regerar apagaria essas notas");
            }
            dailyAlbums.delete(atual.get());
        }
        Album novo = dailyAlbumService.gerarESalvarAlbumDoDia(hoje);
        String artista = novo.getArtists() == null || novo.getArtists().isEmpty()
                ? null : novo.getArtists().getFirst().getName();
        String detalhe = (anterior == null ? "" : anterior + " → ") + novo.getName();
        repository.registrarAcao(adminId, "REGENERATE_DAILY_ALBUM", null, detalhe, clock.instant());
        log.info("Admin {} regerou o Álbum do Dia de {}: {}", adminId, hoje, detalhe);
        return new AlbumRegerado(novo.getName(), artista, hoje);
    }

    // ---------- atividade

    /** Contas novas e ações de admin, das mais recentes para as mais antigas. */
    public List<EventoAdmin> atividade() {
        List<EventoAdmin> eventos = new ArrayList<>();
        for (Map.Entry<String, Instant> conta : repository.contasRecentes(EVENTOS)) {
            eventos.add(new EventoAdmin("ACCOUNT_CREATED", conta.getKey() + " criou uma conta", null, conta.getValue()));
        }
        for (AdminRepository.Acao acao : repository.acoesRecentes(EVENTOS)) {
            eventos.add(evento(acao));
        }
        eventos.sort(Comparator.comparing(EventoAdmin::at).reversed());
        return eventos.size() > EVENTOS ? eventos.subList(0, EVENTOS) : eventos;
    }

    private static EventoAdmin evento(AdminRepository.Acao acao) {
        String admin = acao.adminName() == null ? "Um admin" : acao.adminName();
        String alvo = acao.targetName() == null ? "uma conta apagada" : acao.targetName();
        return switch (acao.action()) {
            case "BAN" -> new EventoAdmin("BAN", admin + " baniu " + alvo
                    + (acao.detail() == null ? "" : ": " + acao.detail()), null, acao.at());
            case "UNBAN" -> new EventoAdmin("UNBAN", admin + " desbaniu " + alvo, null, acao.at());
            case "REMOVE_COMMENT" -> new EventoAdmin("REMOVE_COMMENT", admin + " removeu um comentário de " + alvo,
                    MUSICA, acao.at());
            case "REGENERATE_DAILY_ALBUM" -> new EventoAdmin("REGENERATE_DAILY_ALBUM",
                    admin + " regerou o Álbum do Dia (" + acao.detail() + ")", MUSICA, acao.at());
            default -> new EventoAdmin(acao.action(), admin + ": " + acao.action(), null, acao.at());
        };
    }

    private static int tamanho(int pedido) {
        if (pedido <= 0) {
            return TAMANHO_PADRAO;
        }
        return Math.min(pedido, TAMANHO_MAXIMO);
    }
}
