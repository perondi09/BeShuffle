import React, { useState, useEffect, useCallback } from 'react';

const PaginaAlbum = ({ tipo }) => {
  const [album, setAlbum] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  const buscarAlbum = useCallback(async () => {
    setLoading(true);
    setErro(null);
    try {
      const url = tipo === 'diario' ? '/api/albums/daily' : '/api/albums/random';
      const response = await fetch(url);
      
      if (!response.ok) {
        throw new Error('Erro ao buscar o álbum');
      }
      
      const data = await response.json();
      setAlbum(data);
    } catch (err) {
      setErro(err.message);
    } finally {
      setLoading(false);
    }
  }, [tipo]);

  useEffect(() => {
    buscarAlbum();
  }, [buscarAlbum]);

  if (loading) {
    return (
      <div className="loading-state">
        <h2>Carregando...</h2>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="loading-state">
        <h2 style={{ color: '#ff4d4d' }}>{erro}</h2>
      </div>
    );
  }

  if (!album) return null;

  const imageUrl = album.images && album.images.length > 0 ? album.images[0].url : '';
  const artistName = album.artists && album.artists.length > 0 ? album.artists[0].name : 'Artista Desconhecido';
  const spotifyLink = `https://open.spotify.com/album/${album.id}`;

  return (
    <div className="album-section">
      <div className="subtitle" style={{ textAlign: 'center', marginBottom: '25px' }}>
        {tipo === 'diario' ? 'Álbum do Dia' : 'Álbum Aleatório'}
      </div>
      
      <div className="album-image">
        {imageUrl && <img src={imageUrl} alt={album.name} />}
      </div>
      
      <div className="album-info">
        <div className="album-title">{album.name}</div>
        <div className="album-artist">{artistName}</div>
      </div>
      
      <div className="button-group">
        <a href={spotifyLink} target="_blank" rel="noopener noreferrer" className="btn btn-primary">
          Ouvir no Spotify
        </a>
        
        {tipo === 'aleatorio' && (
          <button onClick={buscarAlbum} className="btn btn-secondary">
            Novo Álbum
          </button>
        )}
      </div>
    </div>
  );
};

export default PaginaAlbum;