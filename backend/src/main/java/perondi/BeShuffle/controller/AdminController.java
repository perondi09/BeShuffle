package perondi.BeShuffle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.admin.AlbumRegerado;
import perondi.BeShuffle.dtos.admin.BanirRequest;
import perondi.BeShuffle.dtos.admin.ComentarioAdmin;
import perondi.BeShuffle.dtos.admin.EventoAdmin;
import perondi.BeShuffle.dtos.Pagina;
import perondi.BeShuffle.dtos.admin.PainelAdmin;
import perondi.BeShuffle.dtos.admin.UsuarioAdmin;
import perondi.BeShuffle.dtos.auth.UsuarioLogado;
import perondi.BeShuffle.services.AdminService;

import java.util.List;

/** Painel de admin. Só contas com papel ADMIN chegam aqui (SecurityConfig); POST e DELETE pedem o header CSRF. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** periodo: hoje, 7d ou 30d. area: tudo, musica ou filmes. */
    @GetMapping("/stats")
    public PainelAdmin stats(@RequestParam(defaultValue = "7d") String periodo,
                             @RequestParam(defaultValue = "tudo") String area) {
        return adminService.painel(periodo, area);
    }

    /** status: todos, ativos, banidos ou admins. q busca no e-mail e no nome. page começa em 0. */
    @GetMapping("/users")
    public Pagina<UsuarioAdmin> users(@RequestParam(required = false) String q,
                                      @RequestParam(defaultValue = "todos") String status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return adminService.usuarios(q, status, page, size);
    }

    @PostMapping("/users/{id}/ban")
    public UsuarioAdmin ban(@PathVariable Long id, @RequestBody(required = false) BanirRequest body,
                            @AuthenticationPrincipal UsuarioLogado admin) {
        return adminService.banir(admin.id(), id, body == null ? null : body.reason());
    }

    @PostMapping("/users/{id}/unban")
    public UsuarioAdmin unban(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado admin) {
        return adminService.desbanir(admin.id(), id);
    }

    @GetMapping("/comments")
    public Pagina<ComentarioAdmin> comments(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return adminService.comentarios(page, size);
    }

    /** Apaga o texto do comentário; a nota continua valendo. */
    @DeleteMapping("/comments/{reviewId}")
    public ResponseEntity<Void> removeComment(@PathVariable Long reviewId, @AuthenticationPrincipal UsuarioLogado admin) {
        adminService.removerComentario(admin.id(), reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activity")
    public List<EventoAdmin> activity() {
        return adminService.atividade();
    }

    @PostMapping("/daily-album/regenerate")
    public AlbumRegerado regenerateDailyAlbum(@AuthenticationPrincipal UsuarioLogado admin) {
        return adminService.regerarAlbumDoDia(admin.id());
    }
}
