async function requestNewAlbum(button = null) {
    try {
        if (button) {
            button.disabled = true;
            button.textContent = 'Buscando...';
        }

        const response = await fetch('/api/albums/random', { method: 'POST' });
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro HTTP:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const album = await response.json();
        console.log('Álbum recebido:', album);
        
        // Validar dados recebidos
        if (!album || !album.uri) {
            throw new Error('Dados do álbum inválidos: uri não encontrado');
        }
        if (!album.images || album.images.length === 0) {
            throw new Error('Dados do álbum inválidos: imagem não encontrada');
        }
        if (!album.artists || album.artists.length === 0) {
            throw new Error('Dados do álbum inválidos: artista não encontrado');
        }
        
        displayAlbum(album);
    } catch (error) {
        console.error('Erro completo:', error);
        document.getElementById('content').innerHTML = '<h2>Erro ao buscar album</h2><p>' + error.message + '</p>';
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = 'Novo album';
        }
    }
}

function displayAlbum(album) {
    try {
        // Extrair ID do album do uri (spotify:album:ID)
        const albumId = album.uri.split(':')[2];
        const spotifyUrl = `https://open.spotify.com/album/${albumId}`;
        
        // Pegar primeira imagem
        const imageUrl = album.images[0].url;
        
        // Pegar primeiro artista
        const artistName = album.artists[0].name;
        
        // Renderizar HTML
        document.getElementById('content').innerHTML = `
            <div class="album-section">
                <div class="album-image">
                    <img src="${imageUrl}" alt="${album.name}">
                </div>
                <div class="album-info">
                    <p class="album-artist">${artistName}</p>
                    <h2 class="album-title">${album.name}</h2>
                </div>
            </div>
            <div class="button-group">
                <a href="${spotifyUrl}" target="_blank" class="btn btn-primary">
                    Ouvir no Spotify
                </a>
                <button id="new-album-button" class="btn btn-secondary" type="button">
                    Novo album
                </button>
            </div>
        `;

        const newAlbumButton = document.getElementById('new-album-button');
        newAlbumButton.addEventListener('click', () => requestNewAlbum(newAlbumButton));
    } catch (error) {
        console.error('Erro ao exibir album:', error);
        document.getElementById('content').innerHTML = '<h2>Erro ao exibir album</h2><p>' + error.message + '</p>';
    }
}

document.addEventListener('DOMContentLoaded', () => requestNewAlbum());
