package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.draws.AlbumSorteado;
import perondi.BeShuffle.dtos.draws.MeuSorteio;
import perondi.BeShuffle.dtos.draws.SorteioFeito;
import perondi.BeShuffle.entity.Album;
import perondi.BeShuffle.entity.Draw;
import perondi.BeShuffle.exceptions.LoginNecessarioException;
import perondi.BeShuffle.exceptions.SpotifyApiException;
import perondi.BeShuffle.repository.DrawRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Sorteio com limite diário: gasta um sorteio, sorteia e grava no histórico. Se falhar, o sorteio volta. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    static final int MAX_SORTEADOS_DO_DIA = 24;

    private final DrawAllowanceService allowanceService;
    private final AlbumPicker albumPicker;
    private final EstiloAlbumPicker estiloAlbumPicker;
    private final AlbumService albumService;
    private final DrawRepository drawRepository;
    private final Clock clock;

    /** Sorteia com um sorteio do dia. Estilos vazios = sorteio geral; com estilos, só para conta logada. */
    public SorteioFeito sortear(Dono dono, List<Estilo> estilos) {
        boolean porEstilo = estilos != null && !estilos.isEmpty();
        if (porEstilo && !dono.logado()) {
            throw new LoginNecessarioException("Entre na sua conta para escolher estilos musicais");
        }
        LocalDate dia = allowanceService.gastar(dono);
        try {
            String albumId = porEstilo ? estiloAlbumPicker.pickAlbumId(estilos) : albumPicker.pickAlbumId();
            if (albumId == null) {
                throw new SpotifyApiException("Não foi possível obter um álbum aleatório do Spotify", 503);
            }
            Album album = albumService.getAlbumById(albumId);
            drawRepository.save(registro(album, dono, dia));
            return new SorteioFeito(album, allowanceService.consultar(dono));
        } catch (RuntimeException e) {
            log.warn("Sorteio falhou ({}); devolvendo o sorteio ({})", e.getMessage(), dono.plano());
            allowanceService.devolver(dono, dia);
            throw e;
        }
    }

    /** Os sorteios que a própria conta fez hoje (página de perfil). */
    public List<MeuSorteio> meusSorteiosDeHoje(Long userId) {
        return drawRepository.findByUserIdAndDrawDateOrderByCreatedAtDesc(userId, LocalDate.now(clock)).stream()
                .map(d -> new MeuSorteio(d.getSpotifyAlbumId(), d.getAlbumName(), d.getArtistName(), d.getImageUrl(),
                        d.getCreatedAt()))
                .toList();
    }

    public List<AlbumSorteado> sorteadosDeHoje() {
        return drawRepository.sorteadosDoDia(LocalDate.now(clock), PageRequest.of(0, MAX_SORTEADOS_DO_DIA));
    }

    private Draw registro(Album album, Dono dono, LocalDate dia) {
        Draw draw = new Draw();
        draw.setUserId(dono.userId());
        draw.setAnonId(dono.anonId());
        draw.setSpotifyAlbumId(album.getId());
        draw.setAlbumName(album.getName());
        if (album.getArtists() != null && !album.getArtists().isEmpty()) {
            draw.setArtistName(album.getArtists().getFirst().getName());
        }
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            draw.setImageUrl(album.getImages().getFirst().getUrl());
        }
        draw.setDrawDate(dia);
        draw.setCreatedAt(Instant.now(clock));
        return draw;
    }
}
