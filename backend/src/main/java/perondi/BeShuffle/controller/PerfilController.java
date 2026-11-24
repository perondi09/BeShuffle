package perondi.BeShuffle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perondi.BeShuffle.dtos.Pagina;
import perondi.BeShuffle.dtos.perfil.PerfilPublico;
import perondi.BeShuffle.dtos.perfil.ReviewDoPerfil;
import perondi.BeShuffle.services.PerfilService;

/** Perfis e diários de notas. Só para conta logada, como o resto da comunidade (ver SecurityConfig). */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping("/{handle}")
    public PerfilPublico perfil(@PathVariable String handle) {
        return perfilService.perfil(handle);
    }

    /** O diário de notas da conta, do Álbum do Dia mais recente para o mais antigo. page começa em 0. */
    @GetMapping("/{handle}/reviews")
    public Pagina<ReviewDoPerfil> diario(@PathVariable String handle,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "12") int size) {
        return perfilService.diario(handle, page, size);
    }
}
