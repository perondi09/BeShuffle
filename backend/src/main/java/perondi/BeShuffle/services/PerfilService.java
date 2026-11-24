package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.Pagina;
import perondi.BeShuffle.dtos.perfil.PerfilPublico;
import perondi.BeShuffle.dtos.perfil.ReviewDoPerfil;
import perondi.BeShuffle.dtos.reviews.ResumoNotas;
import perondi.BeShuffle.entity.AppUser;
import perondi.BeShuffle.exceptions.NaoEncontradoException;
import perondi.BeShuffle.repository.AppUserRepository;
import perondi.BeShuffle.repository.ReviewRepository;

/**
 * Perfil de uma conta e o diário das notas que ela deu aos Álbuns do Dia, como as outras contas logadas veem.
 * Conta banida some: o perfil responde 404 como se não existisse.
 */
@Service
@RequiredArgsConstructor
public class PerfilService {

    static final int TAMANHO_PADRAO = 12;
    static final int TAMANHO_MAXIMO = 50;

    private final AppUserRepository users;
    private final ReviewRepository reviews;

    public PerfilPublico perfil(String handle) {
        AppUser user = achar(handle);
        ResumoNotas resumo = reviews.resumoDaConta(user.getId());
        Double media = resumo.average() == null ? null : Math.round(resumo.average() * 10) / 10.0;
        return new PerfilPublico(user.getHandle(), user.getDisplayName(), user.getAvatarKey(), user.getCreatedAt(),
                total(resumo), media);
    }

    /** O diário: uma página das notas da conta, do Álbum do Dia mais recente para o mais antigo. */
    public Pagina<ReviewDoPerfil> diario(String handle, int pagina, int tamanho) {
        AppUser user = achar(handle);
        int tam = tamanho <= 0 ? TAMANHO_PADRAO : Math.min(tamanho, TAMANHO_MAXIMO);
        int pag = Math.max(pagina, 0);
        return new Pagina<>(reviews.doPerfil(user.getId(), PageRequest.of(pag, tam)), pag, tam,
                total(reviews.resumoDaConta(user.getId())));
    }

    private AppUser achar(String handle) {
        return users.findByHandle(Nick.normalizar(handle))
                .filter(user -> !user.isBanida())
                .orElseThrow(() -> new NaoEncontradoException("Perfil não encontrado"));
    }

    private static long total(ResumoNotas resumo) {
        return resumo.count() == null ? 0 : resumo.count();
    }
}
