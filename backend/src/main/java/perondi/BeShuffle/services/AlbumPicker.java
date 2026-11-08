package perondi.BeShuffle.services;

/**
 * Estratégia de sorteio de álbuns. Trocar a implementação muda como os álbuns são escolhidos
 * sem mexer no controller nem no álbum do dia.
 */
public interface AlbumPicker {

    /**
     * @return o id de um álbum do Spotify, ou {@code null} se nenhum álbum pôde ser sorteado
     */
    String pickAlbumId();
}
