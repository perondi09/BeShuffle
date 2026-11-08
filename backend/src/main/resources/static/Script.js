const ENDPOINTS = {
    random: '/api/albums/random',
    daily: '/api/albums/daily'
};

const MODE_LABELS = {
    random: 'Álbum Aleatório',
    daily: 'Álbum do Dia'
};

let currentMode = 'random';

function escapeHtml(value) {
    if (typeof value !== 'string') {
        return '';
    }
    return value
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function getToggleHtml(activeMode) {
    return `
        <div class="toggle-group">
            <button class="toggle-btn ${activeMode === 'random' ? 'active' : ''}" data-mode="random" type="button">Aleatório</button>
            <button class="toggle-btn ${activeMode === 'daily' ? 'active' : ''}" data-mode="daily" type="button">Do dia</button>
        </div>
    `;
}

function attachEvents() {
    document.querySelectorAll('.toggle-btn').forEach((button) => {
        button.addEventListener('click', () => {
            const mode = button.getAttribute('data-mode');
            if (mode && mode !== currentMode) {
                loadAlbum(mode);
            }
        });
    });

    const refreshButton = document.getElementById('refresh-album-button');
    if (refreshButton) {
        refreshButton.addEventListener('click', () => loadAlbum(currentMode));
    }
}

function renderLoading(mode) {
    const content = document.getElementById('content');
    content.innerHTML = `
        ${getToggleHtml(mode)}
        <div class="loading-state">
            <div class="loading-spinner" aria-hidden="true"></div>
            <h2>Buscando ${MODE_LABELS[mode].toLowerCase()}...</h2>
            <p class="status-description">Aguarde um instante.</p>
        </div>
    `;
    attachEvents();
}

function renderError(mode, message) {
    const content = document.getElementById('content');
    content.innerHTML = `
        ${getToggleHtml(mode)}
        <div class="status-card status-error">
            <h2>Não foi possível carregar o álbum</h2>
            <p>${escapeHtml(message)}</p>
        </div>
        <div class="button-group">
            <button id="refresh-album-button" class="btn btn-secondary" type="button">Tentar novamente</button>
        </div>
    `;
    attachEvents();
}

function renderAlbum(mode, album) {
    const albumId = album.uri.split(':')[2];
    const spotifyUrl = `https://open.spotify.com/album/${albumId}`;
    const imageUrl = album.images[0].url;
    const artistName = album.artists[0].name;
    const albumName = album.name || 'Álbum sem nome';

    const content = document.getElementById('content');
    const refreshButtonHtml = mode === 'random'
        ? `<button id="refresh-album-button" class="btn btn-secondary" type="button">Novo aleatório</button>`
        : '';

    content.innerHTML = `
        ${getToggleHtml(mode)}
        <div class="album-section">
            <div class="album-image">
                <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(albumName)}">
            </div>
            <div class="album-info">
                <p class="album-artist">${escapeHtml(artistName)}</p>
                <h2 class="album-title">${escapeHtml(albumName)}</h2>
            </div>
        </div>
        <div class="button-group">
            <a href="${escapeHtml(spotifyUrl)}" target="_blank" rel="noopener noreferrer" class="btn btn-primary">
                Ouvir no Spotify
            </a>
            ${refreshButtonHtml}
        </div>
    `;
    attachEvents();
}

function getFriendlyErrorMessage(status, responseText) {
    const normalized = (responseText || '').toLowerCase();

    if (status === 401 || status === 403 || normalized.includes('spotify') || normalized.includes('token')) {
        return 'Não foi possível autenticar com o Spotify. Verifique SPOTIFY_CLIENT_ID e SPOTIFY_CLIENT_SECRET e tente novamente.';
    }
    if (status === 429) {
        return 'Muitas requisições ao Spotify no momento. Aguarde alguns segundos e tente novamente.';
    }
    if (status >= 500) {
        return 'O serviço está indisponível no momento. Tente novamente em instantes.';
    }
    return 'Não foi possível buscar os dados agora. Tente novamente.';
}

function validateAlbum(album) {
    if (!album || !album.uri) {
        throw new Error('Dados do álbum inválidos: URI não encontrada.');
    }
    if (!Array.isArray(album.images) || album.images.length === 0) {
        throw new Error('Dados do álbum inválidos: imagem não encontrada.');
    }
    if (!Array.isArray(album.artists) || album.artists.length === 0) {
        throw new Error('Dados do álbum inválidos: artista não encontrado.');
    }
}

async function loadAlbum(mode) {
    currentMode = mode;
    renderLoading(mode);

    try {
        const response = await fetch(ENDPOINTS[mode], {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(getFriendlyErrorMessage(response.status, errorText));
        }

        const album = await response.json();
        validateAlbum(album);
        renderAlbum(mode, album);
    } catch (error) {
        console.error('Erro ao carregar álbum:', error);
        renderError(mode, error.message || 'Erro inesperado ao carregar álbum.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadAlbum('random');
});
