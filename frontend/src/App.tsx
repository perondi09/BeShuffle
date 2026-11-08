import { useEffect, useState } from 'react'
import { type Album, fetchDailyAlbum, fetchRandomAlbum, spotifyUrl } from './api/albums'
import './App.css'

type Load =
  | { state: 'loading' }
  | { state: 'ready'; album: Album }
  | { state: 'error' }

function AlbumCard({ album }: { album: Album }) {
  const cover = album.images?.[0]?.url
  const artists = album.artists?.map((artist) => artist.name).join(', ')
  const year = album.release_date?.slice(0, 4)

  return (
    <article className="album">
      {cover ? <img className="album__cover" src={cover} alt={`Capa de ${album.name}`} /> : null}
      <div className="album__info">
        <h2 className="album__name">{album.name}</h2>
        {artists ? <p className="album__artists">{artists}</p> : null}
        {year ? <p className="album__year">{year}</p> : null}
        <a className="album__link" href={spotifyUrl(album)} target="_blank" rel="noreferrer">
          Ouvir no Spotify
        </a>
      </div>
    </article>
  )
}

export default function App() {
  const [daily, setDaily] = useState<Load>({ state: 'loading' })
  const [drawn, setDrawn] = useState<Load | null>(null)

  useEffect(() => {
    let active = true
    fetchDailyAlbum()
      .then((album) => active && setDaily({ state: 'ready', album }))
      .catch(() => active && setDaily({ state: 'error' }))
    return () => {
      active = false
    }
  }, [])

  async function draw() {
    setDrawn({ state: 'loading' })
    try {
      setDrawn({ state: 'ready', album: await fetchRandomAlbum() })
    } catch {
      setDrawn({ state: 'error' })
    }
  }

  return (
    <main className="page">
      <header className="page__header">
        <h1>BeShuffle</h1>
        <p>Um álbum novo por dia, sorteado do Spotify.</p>
      </header>

      <section aria-labelledby="daily-title">
        <h2 id="daily-title" className="section-title">Álbum do dia</h2>
        {daily.state === 'loading' && <p role="status">Carregando o álbum do dia…</p>}
        {daily.state === 'error' && (
          <p role="alert">Não deu para carregar o álbum do dia. Confira se a API está rodando e recarregue a página.</p>
        )}
        {daily.state === 'ready' && <AlbumCard album={daily.album} />}
      </section>

      <section aria-labelledby="draw-title">
        <h2 id="draw-title" className="section-title">Sorteio</h2>
        <button className="draw" type="button" onClick={draw} disabled={drawn?.state === 'loading'}>
          {drawn?.state === 'loading' ? 'Sorteando…' : 'Sortear álbum'}
        </button>
        {drawn?.state === 'error' && <p role="alert">O sorteio falhou. Tente de novo.</p>}
        {drawn?.state === 'ready' && <AlbumCard album={drawn.album} />}
      </section>
    </main>
  )
}
