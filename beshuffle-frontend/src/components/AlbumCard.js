import React from 'react';

const AlbumCard = ({ album, isDiario, onNovoAlbum }) => {
    if (!album) return null;

    const albumId = album.uri ? album.uri.split(':')[2] : album.id;
    const spotifyUrl = `https://open.spotify.com/album/${albumId}`;
    const imageUrl = album.images && album.images.length > 0 ? album.images[0].url : '';
    const artistName = album.artists && album.artists.length > 0 ? album.artists[0].name : 'Artista Desconhecido';

    return (
        <div>
            <div className="album-section">
                <div className="album-image">
                    <img src={imageUrl} alt={album.name} />
                </div>
                <div className="album-info">
                    <p className="album-artist">{artistName}</p>
                    <h2 className="album-title">{album.name}</h2>
                </div>
            </div>
            <div className="button-group">
                <a href={spotifyUrl} target="_blank" rel="noopener noreferrer" className="btn btn-primary">
                    Ouvir no Spotify
                </a>
                
                {/* O botão 'Novo Álbum' só aparece se NÃO for o álbum do dia */}
                {!isDiario && (
                    <button onClick={onNovoAlbum} className="btn btn-secondary" type="button">
                        Novo Álbum
                    </button>
                )}
            </div>
        </div>
    );
};

export default AlbumCard;