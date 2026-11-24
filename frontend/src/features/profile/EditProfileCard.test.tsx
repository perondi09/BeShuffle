import { fireEvent, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../../test/render'

const ana = { id: 1, email: 'ana@exemplo.com', displayName: 'Ana', handle: 'ana', avatar: 'rosa' }
const resetsAt = '2026-11-15T03:00:00Z'
const hoje = {
  albumId: 'd',
  albumName: 'D',
  albumArtist: null,
  albumImageUrl: null,
  date: '2026-11-14',
  average: null,
  count: 0,
  mine: null,
  reviews: [],
}

function prepararPerfil(onPut: (corpo: Record<string, unknown>) => Response) {
  let atual = ana
  return mockApi({
    '/api/me': (init) => {
      if (init?.method === 'PUT') {
        const corpo = JSON.parse(String(init.body)) as Record<string, unknown>
        const resposta = onPut(corpo)
        if (resposta.ok) atual = { ...atual, ...corpo } as typeof ana
        return resposta
      }
      return json(atual)
    },
    '/api/draws/allowance': () => json({ plan: 'LOGADO', remaining: 10, dailyLimit: 10, resetsAt }),
    '/api/draws/mine': () => json([]),
    '/api/reviews/today': () => json(hoje),
  })
}

describe('EditProfileCard', () => {
  it('nick fora do formato é barrado antes de chamar a API', async () => {
    const corpos: unknown[] = []
    prepararPerfil((corpo) => {
      corpos.push(corpo)
      return json(ana)
    })

    renderAt('/perfil')
    fireEvent.change(await screen.findByLabelText('Nick'), { target: { value: 'A!' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByText('O nick precisa ter de 3 a 20 letras minúsculas, números ou _.')).toBeInTheDocument()
    expect(corpos).toHaveLength(0)
  })

  it('salva nome, nick e avatar', async () => {
    const corpos: unknown[] = []
    prepararPerfil((corpo) => {
      corpos.push(corpo)
      return json({ ...ana, ...corpo })
    })

    renderAt('/perfil')
    fireEvent.change(await screen.findByLabelText('Nick'), { target: { value: 'Ana_Nova' } })
    fireEvent.click(screen.getByRole('radio', { name: 'Avatar Menta' }))
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByText('Perfil salvo.')).toBeInTheDocument()
    expect(corpos).toEqual([{ displayName: 'Ana', handle: 'ana_nova', avatar: 'menta' }])
  })

  it('nick já em uso (409) aparece no campo', async () => {
    prepararPerfil(() => json({ error: 'nick_em_uso', message: 'Esse nick já está em uso.' }, 409))

    renderAt('/perfil')
    fireEvent.change(await screen.findByLabelText('Nick'), { target: { value: 'bia' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByText('Esse nick já está em uso.')).toBeInTheDocument()
  })
})
