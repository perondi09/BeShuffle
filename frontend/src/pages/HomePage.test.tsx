import { cleanup, fireEvent, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { Album } from '../api/albums'
import type { Allowance } from '../api/draws'
import type { TodayReviews } from '../api/reviews'
import { json, mockApi, renderAt } from '../test/render'

const daily: Album = {
  id: 'daily-1',
  name: 'Álbum do Dia',
  release_date: '1999-05-10',
  artists: [{ name: 'Artista Diário' }],
  images: [{ url: 'https://example.com/daily.jpg' }],
}

const drawn: Album = { id: 'random-1', name: 'Álbum Sorteado', artists: [{ name: 'Outro Artista' }] }
const ana = { id: 1, email: 'ana@exemplo.com', displayName: 'Ana' }
const resetsAt = '2026-11-13T03:00:00Z'
/** Fim do dia no futuro: o sorteio guardado ainda vale. */
const amanha = '2099-01-01T03:00:00Z'

const visitante = (remaining: number): Allowance => ({ plan: 'VISITANTE', remaining, dailyLimit: 1, resetsAt })
const logado = (remaining: number): Allowance => ({ plan: 'LOGADO', remaining, dailyLimit: 10, resetsAt })
const semLogin = () => json({ status: 401 }, 401)

/** Abre a aba Sorteio do palco. */
async function abrirSorteio() {
  fireEvent.click(await screen.findByRole('tab', { name: 'Sorteio' }))
}

const notasVazias: TodayReviews = {
  albumId: 'daily-1',
  albumName: 'Álbum do Dia',
  albumArtist: 'Artista Diário',
  albumImageUrl: 'https://example.com/daily.jpg',
  date: '2026-11-14',
  average: null,
  count: 0,
  mine: null,
  reviews: [],
}

describe('HomePage (visitante)', () => {
  it('mostra o álbum do dia com artista, ano, capa e link do Spotify', async () => {
    mockApi({ '/api/me': semLogin, '/api/draws/allowance': () => json(visitante(1)), '/api/albums/daily': () => json(daily) })

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

  it('mostra a tela de fora do ar quando a API não responde e tenta de novo', async () => {
    let fora = true
    mockApi({
      '/api/me': semLogin,
      '/api/draws/allowance': () => json(visitante(1)),
      '/api/albums/daily': () => (fora ? json({}, 504) : json(daily)),
    })

    renderAt('/')

    expect(await screen.findByRole('alert')).toHaveTextContent('Não deu para carregar o álbum do dia')
    expect(screen.getByRole('heading', { level: 1, name: 'A jukebox parou de tocar' })).toBeInTheDocument()
    fora = false
    fireEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })).toBeInTheDocument()
  })

  it('o Álbum do Dia fica fixo e o último sorteio continua na aba Sorteio depois de recarregar', async () => {
    mockApi({
      '/api/me': semLogin,
      '/api/draws/allowance': () => json(visitante(1)),
      '/api/albums/daily': () => json(daily),
      '/api/draws': () => json({ album: drawn, allowance: { ...visitante(0), resetsAt: amanha } }),
    })

    renderAt('/')
    expect(await screen.findByRole('tab', { name: 'Álbum do dia' })).toHaveAttribute('aria-selected', 'true')
    await abrirSorteio()
    expect(screen.getByRole('heading', { level: 1, name: 'Sorteie um disco' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))
    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })).toBeInTheDocument()

    cleanup()
    renderAt('/')

    expect(await screen.findByRole('tab', { name: 'Sorteio' })).toHaveAttribute('aria-selected', 'true')
    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })).toBeInTheDocument()
    expect(screen.getByText('Seu último sorteio')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('tab', { name: 'Álbum do dia' }))
    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })).toBeInTheDocument()
  })

  it('não mostra o sorteio de ontem', async () => {
    localStorage.setItem('beshuffle:aba', 'draw')
    localStorage.setItem(
      'beshuffle:ultimo-sorteio',
      JSON.stringify({ owner: null, expiresAt: '2020-01-01T03:00:00Z', album: drawn }),
    )
    mockApi({ '/api/me': semLogin, '/api/draws/allowance': () => json(visitante(1)), '/api/albums/daily': () => json(daily) })

    renderAt('/')

    expect(await screen.findByRole('heading', { level: 1, name: 'Sorteie um disco' })).toBeInTheDocument()
    expect(screen.queryByText('Álbum Sorteado')).not.toBeInTheDocument()
  })

  it('sorteia pela API sem estilos e mostra a ficha gasta que a API devolveu', async () => {
    let body: unknown
    mockApi({
      '/api/me': semLogin,
      '/api/draws/allowance': () => json(visitante(1)),
      '/api/albums/daily': () => json(daily),
      '/api/draws': (init) => {
        body = JSON.parse(String(init?.body))
        return json({ album: drawn, allowance: visitante(0) })
      },
    })

    renderAt('/')
    expect(await screen.findByText('1 ficha hoje')).toBeInTheDocument()
    await abrirSorteio()
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })).toBeInTheDocument()
    expect(screen.getByText('Sorteado agora')).toBeInTheDocument()
    expect(screen.getByText('Ficha usada')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Volte amanhã' })).toBeDisabled()
    expect(body).toEqual({})
    expect(screen.getByRole('link', { name: 'Crie uma conta grátis' })).toHaveAttribute('href', '/cadastro')
  })

  it('mantém a ficha quando o sorteio falha', async () => {
    mockApi({
      '/api/me': semLogin,
      '/api/draws/allowance': () => json(visitante(1)),
      '/api/albums/daily': () => json(daily),
      '/api/draws': () => json({}, 503),
    })

    renderAt('/')
    await abrirSorteio()
    fireEvent.click(await screen.findByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('sua ficha continua com você')
    expect(screen.getByText('1 ficha hoje')).toBeInTheDocument()
  })

  it('mostra a mensagem da API quando o limite do dia acabou (429)', async () => {
    let saldo = visitante(1)
    mockApi({
      '/api/me': semLogin,
      '/api/draws/allowance': () => json(saldo),
      '/api/albums/daily': () => json(daily),
      '/api/draws': () => {
        saldo = visitante(0)
        return json({ error: 'DAILY_LIMIT_REACHED', message: 'Você já usou o sorteio de hoje.' }, 429)
      },
    })

    renderAt('/')
    await abrirSorteio()
    fireEvent.click(await screen.findByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Você já usou o sorteio de hoje.')
    expect(await screen.findByRole('button', { name: 'Volte amanhã' })).toBeDisabled()
  })

  it('não deixa sortear quando a API diz que a ficha do dia já foi', async () => {
    mockApi({ '/api/me': semLogin, '/api/draws/allowance': () => json(visitante(0)), '/api/albums/daily': () => json(daily) })

    renderAt('/')
    await abrirSorteio()

    expect(await screen.findByRole('button', { name: 'Volte amanhã' })).toBeDisabled()
    expect(screen.queryByRole('group', { name: /Estilos/ })).not.toBeInTheDocument()
  })

  it('tranca os sorteados do dia e a nota para visitante', async () => {
    mockApi({ '/api/me': semLogin, '/api/draws/allowance': () => json(visitante(1)), '/api/albums/daily': () => json(daily) })

    renderAt('/')
    await screen.findByRole('heading', { level: 1, name: 'Álbum do Dia' })

    expect(screen.getByText('Crie uma conta grátis para ver o que a galera sorteou hoje.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Criar conta' })).toHaveAttribute('href', '/cadastro')
    expect(screen.getByText('Entre para dar nota e comentar o Álbum do Dia.')).toBeInTheDocument()
  })
})

describe('HomePage (conta logada)', () => {
  const base = {
    '/api/me': () => json(ana),
    '/api/albums/daily': () => json(daily),
    '/api/genres': () =>
      json([
        { id: 'rock', name: 'Rock' },
        { id: 'jazz', name: 'Jazz' },
      ]),
    '/api/reviews/today': () => json(notasVazias),
  }

  it('mostra 10 fichas e sorteia com os estilos escolhidos', async () => {
    let body: unknown
    mockApi({
      ...base,
      '/api/draws/allowance': () => json(logado(10)),
      '/api/draws/today': () => json([]),
      '/api/draws': (init) => {
        body = JSON.parse(String(init?.body))
        return json({ album: drawn, allowance: logado(9) })
      },
    })

    renderAt('/')
    expect(await screen.findByText('10 fichas hoje')).toBeInTheDocument()
    await abrirSorteio()
    fireEvent.click(await screen.findByRole('button', { name: 'Rock' }))
    fireEvent.click(screen.getByRole('button', { name: 'Jazz' }))
    expect(screen.getByRole('button', { name: 'Rock' })).toHaveAttribute('aria-pressed', 'true')
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    expect(await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })).toBeInTheDocument()
    expect(body).toEqual({ genres: ['rock', 'jazz'] })
    expect(screen.getByText('9 fichas hoje')).toBeInTheDocument()
    expect(localStorage.getItem('beshuffle:estilos')).toBe('["rock","jazz"]')
  })

  it('"Todos" limpa os estilos e o sorteio vai sem filtro', async () => {
    localStorage.setItem('beshuffle:estilos', '["rock"]')
    let body: unknown
    mockApi({
      ...base,
      '/api/draws/allowance': () => json(logado(3)),
      '/api/draws/today': () => json([]),
      '/api/draws': (init) => {
        body = JSON.parse(String(init?.body))
        return json({ album: drawn, allowance: logado(2) })
      },
    })

    renderAt('/')
    await abrirSorteio()
    expect(await screen.findByRole('button', { name: 'Rock' })).toHaveAttribute('aria-pressed', 'true')
    fireEvent.click(screen.getByRole('button', { name: 'Todos' }))
    fireEvent.click(screen.getByRole('button', { name: 'Sortear álbum' }))

    await screen.findByRole('heading', { level: 1, name: 'Álbum Sorteado' })
    expect(body).toEqual({})
  })

  it('mostra os sorteados de hoje no lugar do cadeado', async () => {
    mockApi({
      ...base,
      '/api/draws/allowance': () => json(logado(10)),
      '/api/draws/today': () =>
        json([{ id: 'x1', name: 'Disco X', artist: 'Banda X', imageUrl: 'https://img/x1.jpg', count: 3, lastDrawnAt: resetsAt }]),
    })

    renderAt('/')

    const card = within(await screen.findByRole('region', { name: 'Sorteados de hoje' }))
    expect(await card.findByText('Disco X')).toBeInTheDocument()
    expect(card.getByText('Banda X (3x)')).toBeInTheDocument()
    expect(card.getByRole('link', { name: 'Disco X no Spotify' })).toHaveAttribute('href', 'https://open.spotify.com/album/x1')
    expect(screen.queryByText('Crie uma conta grátis para ver o que a galera sorteou hoje.')).not.toBeInTheDocument()
  })

  it('em outro aparelho, a aba Sorteio recupera o último sorteio da conta pela API', async () => {
    localStorage.setItem('beshuffle:aba', 'draw')
    mockApi({
      ...base,
      '/api/draws/allowance': () => json(logado(9)),
      '/api/draws/today': () => json([]),
      '/api/draws/mine': () =>
        json([{ id: 'x1', name: 'Disco X', artist: 'Banda X', imageUrl: 'https://img/x1.jpg', drawnAt: resetsAt }]),
    })

    renderAt('/')

    expect(await screen.findByRole('heading', { level: 1, name: 'Disco X' })).toBeInTheDocument()
    expect(screen.getByText('Seu último sorteio')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ouvir no Spotify' })).toHaveAttribute(
      'href',
      'https://open.spotify.com/album/x1',
    )
  })

  it('a nota mostra a capa, o nome e o dia do Álbum do Dia', async () => {
    mockApi({ ...base, '/api/draws/allowance': () => json(logado(10)), '/api/draws/today': () => json([]) })

    renderAt('/')
    const card = within(await screen.findByRole('region', { name: 'Sua nota' }))

    expect(await card.findByText('Álbum do Dia de sábado, 14 de novembro')).toBeInTheDocument()
    expect(card.getByRole('img', { name: 'Capa de Álbum do Dia' })).toHaveAttribute(
      'src',
      'https://example.com/daily.jpg',
    )
    expect(card.getByText('Artista Diário')).toBeInTheDocument()
  })

  it('dá nota e comentário ao Álbum do Dia', async () => {
    let body: unknown
    mockApi({
      ...base,
      '/api/draws/allowance': () => json(logado(10)),
      '/api/draws/today': () => json([]),
      '/api/reviews/today': (init) => {
        if (init?.method !== 'PUT') return json(notasVazias)
        body = JSON.parse(String(init.body))
        return json({
          ...notasVazias,
          average: 4,
          count: 1,
          mine: { rating: 4, comment: 'Muito bom', updatedAt: resetsAt },
          reviews: [{ author: 'Ana', rating: 4, comment: 'Muito bom', updatedAt: resetsAt }],
        })
      },
    })

    renderAt('/')
    const card = within(await screen.findByRole('region', { name: 'Sua nota' }))
    expect(await card.findByText('Seja a primeira pessoa a dar nota.')).toBeInTheDocument()
    fireEvent.click(card.getByRole('radio', { name: '4 de 5' }))
    fireEvent.change(card.getByLabelText('Comentário'), { target: { value: 'Muito bom' } })
    fireEvent.click(card.getByRole('button', { name: 'Publicar' }))

    expect(await card.findByRole('status')).toHaveTextContent('Nota publicada.')
    expect(body).toEqual({ rating: 4, comment: 'Muito bom' })
    expect(card.getByText('Média 4 de 5, 1 nota')).toBeInTheDocument()
    expect(card.getByRole('button', { name: 'Atualizar nota' })).toBeInTheDocument()
  })

  it('não publica sem escolher a nota', async () => {
    mockApi({ ...base, '/api/draws/allowance': () => json(logado(10)), '/api/draws/today': () => json([]) })

    renderAt('/')
    const card = within(await screen.findByRole('region', { name: 'Sua nota' }))
    await card.findByText('Seja a primeira pessoa a dar nota.')
    fireEvent.click(card.getByRole('button', { name: 'Publicar' }))

    expect(card.getByRole('alert')).toHaveTextContent('Escolha uma nota de 1 a 5.')
  })
})
