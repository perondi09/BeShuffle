import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from './App'
import type { Album } from './api/albums'

const daily: Album = {
  id: 'daily-1',
  name: 'Álbum do Dia',
  release_date: '1999-05-10',
  artists: [{ name: 'Artista Diário' }],
  images: [{ url: 'https://example.com/daily.jpg' }],
}

const drawn: Album = { id: 'random-1', name: 'Álbum Sorteado', artists: [{ name: 'Outro Artista' }] }

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })

function mockApi(responses: Record<string, () => Response>) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(async (input) => {
    const respond = responses[String(input)]
    if (!respond) throw new Error(`rota não mockada: ${String(input)}`)
    return respond()
  })
}

describe('App', () => {
  it('mostra o álbum do dia com artista, ano e link do Spotify', async () => {
    mockApi({ '/api/albums/daily': () => json(daily) })

    render(<App />)

    expect(await screen.findByText('Álbum do Dia')).toBeInTheDocument()
    expect(screen.getByText('Artista Diário')).toBeInTheDocument()
    expect(screen.getByText('1999')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ouvir no Spotify' })).toHaveAttribute(
      'href',
      'https://open.spotify.com/album/daily-1',
    )
  })

  it('avisa quando o álbum do dia não carrega', async () => {
    mockApi({ '/api/albums/daily': () => json({}, 503) })

    render(<App />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Não deu para carregar o álbum do dia')
  })

  it('sorteia um álbum ao clicar no botão', async () => {
    mockApi({ '/api/albums/daily': () => json(daily), '/api/albums/random': () => json(drawn) })

    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByText('Álbum Sorteado')).toBeInTheDocument()
    expect(screen.getByText('Outro Artista')).toBeInTheDocument()
  })
})
