import { fireEvent, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { Album } from '../api/albums'
import { STORAGE_KEY, today } from '../features/draw/dailyToken'
import { json, mockApi, renderAt } from '../test/render'

const daily: Album = {
  id: 'daily-1',
  name: 'Álbum do Dia',
  release_date: '1999-05-10',
  artists: [{ name: 'Artista Diário' }],
  images: [{ url: 'https://example.com/daily.jpg' }],
}

const drawn: Album = { id: 'random-1', name: 'Álbum Sorteado', artists: [{ name: 'Outro Artista' }] }

describe('HomePage', () => {
  it('mostra o álbum do dia com artista, ano, capa e link do Spotify', async () => {
    mockApi({ '/api/albums/daily': () => json(daily) })

    renderAt('/')

    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })).toBeInTheDocument()
    expect(screen.getByText('Artista Diário')).toBeInTheDocument()
    expect(screen.getByText('1999')).toBeInTheDocument()
    expect(screen.getByText('Hoje na jukebox')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Capa de Álbum do Dia' })).toHaveAttribute('src', 'https://example.com/daily.jpg')
    expect(screen.getByRole('link', { name: 'Ouvir no Spotify' })).toHaveAttribute(
      'href',
      'https://open.spotify.com/album/daily-1',
    )
  })

  it('avisa quando o álbum do dia não carrega', async () => {
    mockApi({ '/api/albums/daily': () => json({}, 503) })

    renderAt('/')

    expect(await screen.findByRole('alert')).toHaveTextContent('Não deu para carregar o álbum do dia')
  })

  it('sorteia um álbum e gasta a ficha do visitante', async () => {
    mockApi({ '/api/albums/daily': () => json(daily), '/api/albums/random': () => json(drawn) })

    renderAt('/')
    expect(screen.getByText('1 ficha hoje')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })).toBeInTheDocument()
    expect(screen.getByText('Sorteado agora')).toBeInTheDocument()
    expect(screen.getByText('Ficha usada')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Volte amanhã' })).toBeDisabled()
    expect(localStorage.getItem(STORAGE_KEY)).toBe(today())
  })

  it('mantém a ficha quando o sorteio falha', async () => {
    mockApi({ '/api/albums/daily': () => json(daily), '/api/albums/random': () => json({}, 502) })

    renderAt('/')
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('sua ficha continua com você')
    expect(screen.getByText('1 ficha hoje')).toBeInTheDocument()
  })

  it('não deixa sortear de novo no mesmo dia', async () => {
    localStorage.setItem(STORAGE_KEY, today())
    mockApi({ '/api/albums/daily': () => json(daily) })

    renderAt('/')

    expect(screen.getByRole('button', { name: 'Volte amanhã' })).toBeDisabled()
    expect(screen.getByRole('link', { name: 'Crie uma conta' })).toHaveAttribute('href', '/cadastro')
    await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })
  })

  it('tranca os sorteados do dia e a nota para visitante', async () => {
    mockApi({ '/api/albums/daily': () => json(daily) })

    renderAt('/')
    await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })

    expect(screen.getByText('Crie uma conta grátis para ver o que a galera sorteou hoje.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Criar conta' })).toHaveAttribute('href', '/cadastro')
    expect(screen.getByText('Entre para dar nota e comentar os discos que você sorteia.')).toBeInTheDocument()
  })
})
