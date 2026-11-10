import { fireEvent, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../../test/render'

const ana = { id: 1, email: 'ana@exemplo.com', displayName: 'Ana' }
const semAlbum = () => json({}, 503)
const type = (label: string, value: string) => fireEvent.change(screen.getByLabelText(label), { target: { value } })
/** Só o topo da página: a home tem outros "Entrar" nos cards trancados. */
const topo = () => within(screen.getByRole('banner'))

describe('Sessão no topo do site', () => {
  it('sem login, mostra o botão Entrar', async () => {
    mockApi({ '/api/me': () => json({ status: 401 }, 401), '/api/albums/daily': semAlbum })

    renderAt('/')

    expect(await topo().findByRole('link', { name: 'Entrar' })).toBeInTheDocument()
    expect(topo().queryByRole('button', { name: 'Sair' })).not.toBeInTheDocument()
  })

  it('logado, mostra o nome e o botão Sair no lugar de Entrar', async () => {
    mockApi({ '/api/me': () => json(ana), '/api/albums/daily': semAlbum })

    renderAt('/')

    expect(await topo().findByText('Oi, Ana')).toBeInTheDocument()
    expect(topo().getByRole('button', { name: 'Sair' })).toBeInTheDocument()
    expect(topo().queryByRole('link', { name: 'Entrar' })).not.toBeInTheDocument()
  })

  it('Sair chama o logout e volta a mostrar Entrar', async () => {
    let saiu = false
    mockApi({
      '/api/me': () => (saiu ? json({}, 401) : json(ana)),
      '/api/auth/logout': () => {
        saiu = true
        return new Response(null, { status: 204 })
      },
      '/api/albums/daily': semAlbum,
    })

    renderAt('/')
    fireEvent.click(await topo().findByRole('button', { name: 'Sair' }))

    expect(await topo().findByRole('link', { name: 'Entrar' })).toBeInTheDocument()
    expect(saiu).toBe(true)
  })

  it('depois do login, o topo já mostra quem entrou', async () => {
    let logado = false
    mockApi({
      '/api/me': () => (logado ? json(ana) : json({}, 401)),
      '/api/auth/login': () => {
        logado = true
        return new Response(null, { status: 204 })
      },
      '/api/albums/daily': semAlbum,
    })

    renderAt('/entrar')
    type('E-mail', 'ana@exemplo.com')
    type('Senha', 'senha-forte-1')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await topo().findByText('Oi, Ana')).toBeInTheDocument()
  })

  it('depois do cadastro, o topo já mostra quem entrou', async () => {
    let logado = false
    mockApi({
      '/api/me': () => (logado ? json(ana) : json({}, 401)),
      '/api/auth/register': () => {
        logado = true
        return json(ana, 201)
      },
      '/api/albums/daily': semAlbum,
    })

    renderAt('/cadastro')
    type('Como quer ser chamado', 'Ana')
    type('E-mail', 'ana@exemplo.com')
    type('Senha', 'senha-forte-1')
    type('Repita a senha', 'senha-forte-1')
    fireEvent.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(await topo().findByText('Oi, Ana')).toBeInTheDocument()
  })

  it('se a API cair, trata como visitante sem quebrar a página', async () => {
    mockApi({ '/api/me': () => json({}, 503), '/api/albums/daily': semAlbum })

    renderAt('/')

    expect(await topo().findByRole('link', { name: 'Entrar' })).toBeInTheDocument()
  })
})
