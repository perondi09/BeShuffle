async function fetchRandomAlbum() {
    const randomResponse = await fetch('/api/albums/random', { method: 'POST' });

    if (!randomResponse.ok) {
        throw new Error('Erro ao buscar novo album');
    }

    return randomResponse.json();
}

async function requestNewAlbum(button = null) {
    try {
        if (button) {
            button.disabled = true;
            button.textContent = 'Buscando...';
        }

        const album = await fetchRandomAlbum();
        displayAlbum(album);
    } catch (error) {
        console.error('Erro:', error);
        document.getElementById('content').innerHTML = '<h2>Erro ao carregar album</h2>';
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = 'Novo album';
        }
    }
}

async function loadAlbum() {
    try {
        const response = await fetch('/api/albums/today');

        if (response.status === 204) {
            await requestNewAlbum();
            return;
        }

        if (!response.ok) {
            console.error('Erro ao carregar:', response.status);
            document.getElementById('content').innerHTML = '<h2>Erro ao carregar album</h2>';
            return;
        }

        const album = await response.json();
        displayAlbum(album);

    } catch (error) {
        console.error('Erro:', error);
        document.getElementById('content').innerHTML = '<h2>Erro ao carregar album</h2>';
    }
}

function displayAlbum(album) {
    const spotifyUrl = `https://open.spotify.com/album/${album.albumUrl.split(':')[2]}`;
    const year = album.releaseDate.split('-')[0];

    document.getElementById('content').innerHTML = `
        <div class="album-section">
            <div class="album-image">
                <img src="${album.imageUrl}" alt="${album.albumName}">
            </div>
            <div class="album-info">
                <p class="album-artist">${album.artistName}</p>
                <h2 class="album-title">${album.albumName}</h2>
                <p class="album-date">Lancado em ${year}</p>
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
}

document.addEventListener('DOMContentLoaded', loadAlbum);

setInterval(loadAlbum, 3600000);