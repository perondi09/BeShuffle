import { fireEvent, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../test/render'

const ana = { id: 1, email: 'ana@exemplo.com', displayName: 'Ana' }
const resetsAt = '2026-11-15T03:00:00Z'
const notas = {
  albumId: 'daily-1',
  albumName: 'Clube da Esquina',
  average: 4,
  count: 1,
  mine: { rating: 4, comment: 'Lindo demais', updatedAt: resetsAt },
  reviews: [],
}

describe('ProfilePage', () => {
  it('sem login, manda para a tela de entrar', async () => {
    mockApi({ '/api/me': () => json({}, 401), '/api/draws/allowance': () => json({}, 503) })

    renderAt('/perfil')

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('mostra a conta, as fichas, os sorteios de hoje e a nota do Álbum do Dia', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({ plan: 'LOGADO', remaining: 7, dailyLimit: 10, resetsAt }),
      '/api/draws/mine': () =>
        json([{ id: 'x1', name: 'Disco X', artist: 'Banda X', imageUrl: null, drawnAt: '2026-11-14T15:30:00Z' }]),
      '/api/reviews/today': () => json(notas),
    })

    renderAt('/perfil')

    expect(await screen.findByRole('heading', { level: 1, name: 'Ana' })).toBeInTheDocument()
    expect(screen.getByText('ana@exemplo.com')).toBeInTheDocument()
    expect(await screen.findByText('Você ainda tem 7 de 10 fichas hoje.')).toBeInTheDocument()

    const sorteios = within(screen.getByRole('region', { name: 'Seus sorteios de hoje' }))
    expect(await sorteios.findByRole('link', { name: 'Disco X' })).toHaveAttribute(
      'href',
      'https://open.spotify.com/album/x1',
    )

    const nota = within(screen.getByRole('region', { name: 'Sua nota de hoje' }))
    expect(await nota.findByText('Lindo demais')).toBeInTheDocument()
    expect(nota.getByRole('link', { name: 'Mudar a nota' })).toHaveAttribute('href', '/')
  })

  it('sem sorteios nem nota, convida a sortear e a avaliar', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({ plan: 'LOGADO', remaining: 10, dailyLimit: 10, resetsAt }),
      '/api/draws/mine': () => json([]),
      '/api/reviews/today': () => json({ ...notas, mine: null, count: 0, average: null }),
    })

    renderAt('/perfil')

    expect(await screen.findByText('Você ainda não sorteou nada hoje.')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'Dar nota' })).toHaveAttribute('href', '/')
  })

  it('Sair da conta faz logout e volta para a home', async () => {
    let saiu = false
    mockApi({
      '/api/me': () => (saiu ? json({}, 401) : json(ana)),
      '/api/auth/logout': () => {
        saiu = true
        return new Response(null, { status: 204 })
      },
      '/api/draws/allowance': () => json({ plan: 'LOGADO', remaining: 10, dailyLimit: 10, resetsAt }),
      '/api/draws/mine': () => json([]),
      '/api/reviews/today': () => json(notas),
      '/api/albums/daily': () => json({}, 503),
    })

    renderAt('/perfil')
    fireEvent.click(await screen.findByRole('button', { name: 'Sair da conta' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Não deu para carregar o álbum do dia')
    expect(saiu).toBe(true)
  })

  it('no topo, o nome da conta leva ao perfil', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({ plan: 'LOGADO', remaining: 10, dailyLimit: 10, resetsAt }),
      '/api/draws/mine': () => json([]),
      '/api/reviews/today': () => json(notas),
    })

    renderAt('/perfil')

    const topo = within(screen.getByRole('banner'))
    expect(await topo.findByRole('link', { name: 'Oi, Ana' })).toHaveAttribute('href', '/perfil')
  })
})
