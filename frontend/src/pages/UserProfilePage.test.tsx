import { fireEvent, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../test/render'

const ana = { id: 1, email: 'ana@exemplo.com', displayName: 'Ana', handle: 'ana', avatar: 'rosa' }
const bia = {
  handle: 'bia',
  displayName: 'Bia',
  avatar: 'menta',
  joinedAt: '2026-09-10T12:00:00Z',
  reviewCount: 14,
  average: 3.5,
}

const entrada = (n: number, extra = {}) => ({
  albumId: `a${n}`,
  albumName: `Disco ${n}`,
  albumArtist: `Banda ${n}`,
  albumImageUrl: null,
  date: '2026-11-14',
  rating: 3.5,
  comment: null,
  updatedAt: '2026-11-14T20:00:00Z',
  ...extra,
})

const pagina = (items: unknown[], total: number, page = 0) => ({ items, page, size: 12, total })

describe('UserProfilePage', () => {
  it('sem login, manda para a tela de entrar', async () => {
    mockApi({ '/api/me': () => json({}, 401), '/api/draws/allowance': () => json({}, 503) })

    renderAt('/u/bia')

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('mostra o perfil de outra conta com a média e o diário em meias notas', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({}, 503),
      '/api/users/bia': () => json(bia),
      '/api/users/bia/reviews?page=0&size=12': () =>
        json(pagina([entrada(1, { comment: 'Gostei muito' }), entrada(2, { rating: 2.5 })], 2)),
    })

    renderAt('/u/bia')

    expect(await screen.findByRole('heading', { level: 1, name: 'Bia' })).toBeInTheDocument()
    expect(screen.getByText('@bia')).toBeInTheDocument()
    expect(screen.queryByText('Seu perfil')).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Editar perfil' })).not.toBeInTheDocument()

    const resumo = within(screen.getByRole('list', { name: 'Resumo do perfil' }))
    expect(resumo.getByText('14')).toBeInTheDocument()
    expect(resumo.getByText('3,5')).toBeInTheDocument()

    const diario = within(screen.getByRole('region', { name: 'Diário de notas' }))
    expect(await diario.findByText('Disco 1')).toBeInTheDocument()
    expect(diario.getByText('Gostei muito')).toBeInTheDocument()
    expect(diario.getByText('Disco 2')).toBeInTheDocument()
    expect(diario.queryByRole('button', { name: 'Ver mais notas' })).not.toBeInTheDocument()
  })

  it('no próprio perfil oferece editar e, sem notas, convida a avaliar', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({}, 503),
      '/api/users/ana': () => json({ ...bia, handle: 'ana', displayName: 'Ana', reviewCount: 0, average: null }),
      '/api/users/ana/reviews?page=0&size=12': () => json(pagina([], 0)),
    })

    renderAt('/u/ana')

    expect(await screen.findByRole('heading', { level: 1, name: 'Ana' })).toBeInTheDocument()
    expect(screen.getByText('Seu perfil')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Editar perfil' })).toHaveAttribute('href', '/perfil')
    expect(screen.getByText('Você ainda não avaliou nenhum Álbum do Dia.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Avaliar o Álbum do Dia' })).toHaveAttribute('href', '/')
  })

  it('Ver mais notas carrega a próxima página', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({}, 503),
      '/api/users/bia': () => json(bia),
      '/api/users/bia/reviews?page=0&size=12': () => json(pagina([entrada(1)], 2)),
      '/api/users/bia/reviews?page=1&size=12': () => json(pagina([entrada(2)], 2, 1)),
    })

    renderAt('/u/bia')

    expect(await screen.findByText('Disco 1')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Ver mais notas' }))

    expect(await screen.findByText('Disco 2')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Ver mais notas' })).not.toBeInTheDocument()
  })

  it('nick que não existe mostra a página não encontrada', async () => {
    mockApi({
      '/api/me': () => json(ana),
      '/api/draws/allowance': () => json({}, 503),
      '/api/users/fantasma': () => json({ error: 'nao_encontrado', message: 'Perfil não encontrado' }, 404),
      '/api/users/fantasma/reviews?page=0&size=12': () => json({ error: 'nao_encontrado' }, 404),
    })

    renderAt('/u/fantasma')

    expect(await screen.findByRole('heading', { name: 'Essa faixa não existe' })).toBeInTheDocument()
  })
})
