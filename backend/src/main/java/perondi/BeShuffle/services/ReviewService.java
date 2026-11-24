package perondi.BeShuffle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import perondi.BeShuffle.dtos.reviews.MinhaNota;
import perondi.BeShuffle.dtos.reviews.NotaRequest;
import perondi.BeShuffle.dtos.reviews.NotasDoDia;
import perondi.BeShuffle.dtos.reviews.ResumoNotas;
import perondi.BeShuffle.entity.DailyAlbum;
import perondi.BeShuffle.entity.Review;
import perondi.BeShuffle.exceptions.ValidationException;
import perondi.BeShuffle.repository.ReviewRepository;

import java.time.Clock;
import java.time.Instant;

/** Notas e comentários. Só o Álbum do Dia recebe nota, e cada conta tem uma nota por álbum. */
@Service
@RequiredArgsConstructor
public class ReviewService {

    static final int MAX_COMENTARIO = 500;
    static final int MAX_NOTAS_LISTADAS = 50;

    private final ReviewRepository reviewRepository;
    private final DailyAlbumService dailyAlbumService;
    private final Clock clock;

    public NotasDoDia notasDeHoje(Long userId) {
        return montar(dailyAlbumService.registroDeHoje(), userId);
    }

    /** Cria a nota da conta para o Álbum do Dia, ou edita a que já existe. */
    public NotasDoDia avaliarHoje(Long userId, NotaRequest pedido) {
        int nota = validarNota(pedido);
        String comentario = normalizarComentario(pedido.comment());
        DailyAlbum album = dailyAlbumService.registroDeHoje();
        try {
            gravar(userId, album.getId(), nota, comentario);
        } catch (DataIntegrityViolationException duasAoMesmoTempo) {
            // Duas requisições da mesma conta criaram a nota juntas; a segunda vira edição.
            gravar(userId, album.getId(), nota, comentario);
        }
        return montar(album, userId);
    }

    private void gravar(Long userId, Long dailyAlbumId, int nota, String comentario) {
        Instant agora = Instant.now(clock);
        Review review = reviewRepository.findByUserIdAndDailyAlbumId(userId, dailyAlbumId).orElseGet(() -> {
            Review nova = new Review();
            nova.setUserId(userId);
            nova.setDailyAlbumId(dailyAlbumId);
            nova.setCreatedAt(agora);
            return nova;
        });
        review.setRatingX2(nota);
        review.setComment(comentario);
        review.setUpdatedAt(agora);
        reviewRepository.saveAndFlush(review);
    }

    private NotasDoDia montar(DailyAlbum album, Long userId) {
        ResumoNotas resumo = reviewRepository.resumo(album.getId());
        MinhaNota minha = reviewRepository.findByUserIdAndDailyAlbumId(userId, album.getId())
                .map(r -> new MinhaNota(r.getRatingX2() / 2.0, r.getComment(), r.getUpdatedAt()))
                .orElse(null);
        Double media = resumo.average() == null ? null : Math.round(resumo.average() * 10) / 10.0;
        long total = resumo.count() == null ? 0 : resumo.count();
        return new NotasDoDia(album.getSpotifyAlbumId(), album.getAlbumName(), album.getArtistName(),
                album.getImageUrl(), album.getDisplayDate(), media, total, minha,
                reviewRepository.recentes(album.getId(), PageRequest.of(0, MAX_NOTAS_LISTADAS)));
    }

    /** A nota chega em estrelas (0,5 a 5, de meia em meia) e é guardada em meias estrelas (1 a 10). */
    static int validarNota(NotaRequest pedido) {
        Double estrelas = pedido == null ? null : pedido.rating();
        if (estrelas == null || estrelas.isNaN() || estrelas < 0.5 || estrelas > 5 || estrelas * 2 != Math.rint(estrelas * 2)) {
            throw new ValidationException("a nota vai de 0,5 a 5, de meia em meia estrela", "rating");
        }
        return (int) Math.rint(estrelas * 2);
    }

    private static String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        String limpo = comentario.strip();
        if (limpo.length() > MAX_COMENTARIO) {
            throw new ValidationException("o comentário pode ter até " + MAX_COMENTARIO + " caracteres", "comment");
        }
        return limpo;
    }
}
