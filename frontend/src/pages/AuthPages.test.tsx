import { fireEvent, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../test/render'

const type = (label: string, value: string) => fireEvent.change(screen.getByLabelText(label), { target: { value } })

describe('LoginPage', () => {
  it('mostra os erros de validação sem chamar a API', () => {
    const fetchMock = mockApi({})

    renderAt('/entrar')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(screen.getByText('Informe seu e-mail.')).toBeInTheDocument()
    expect(screen.getByText('Informe sua senha.')).toBeInTheDocument()
    expect(screen.getByLabelText('E-mail')).toHaveAttribute('aria-invalid', 'true')
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('envia e-mail e senha e avisa quando o login ainda não existe na API', async () => {
    let body: unknown
    mockApi({
      '/api/auth/login': (init) => {
        body = JSON.parse(String(init?.body))
        return json({}, 404)
      },
    })

    renderAt('/entrar')
    type('E-mail', ' pessoa@example.com ')
    type('Senha', 'segredo-de-teste')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('O login ainda não está disponível')
    expect(body).toEqual({ email: 'pessoa@example.com', password: 'segredo-de-teste' })
  })

  it('volta para a home depois de entrar', async () => {
    mockApi({ '/api/auth/login': () => new Response(null, { status: 204 }), '/api/albums/daily': () => json({}, 503) })

    renderAt('/entrar')
    type('E-mail', 'pessoa@example.com')
    type('Senha', 'segredo-de-teste')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('button', { name: 'Sortear álbum' })).toBeInTheDocument()
  })

  it('leva para o cadastro', () => {
    renderAt('/entrar')
    expect(screen.getByRole('link', { name: 'Criar conta' })).toHaveAttribute('href', '/cadastro')
  })
})

describe('RegisterPage', () => {
  it('exige senha mínima e confirmação igual', () => {
    renderAt('/cadastro')
    type('Como quer ser chamado', 'Gui')
    type('E-mail', 'pessoa@example.com')
    type('Senha', 'curta')
    type('Repita a senha', 'outra')
    fireEvent.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(screen.getByText('A senha precisa de 8 caracteres ou mais.')).toBeInTheDocument()
    expect(screen.getByText('As senhas não são iguais.')).toBeInTheDocument()
  })

  it('avisa quando o e-mail já tem conta', async () => {
    mockApi({ '/api/auth/register': () => json({}, 409) })

    renderAt('/cadastro')
    type('Como quer ser chamado', 'Gui')
    type('E-mail', 'pessoa@example.com')
    type('Senha', 'senha-de-teste')
    type('Repita a senha', 'senha-de-teste')
    fireEvent.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe uma conta com esse e-mail.')
  })
})

describe('NotFoundPage', () => {
  it('mostra a página 404 para rota desconhecida', () => {
    renderAt('/nao-existe')
    expect(screen.getByRole('heading', { name: 'Essa faixa não existe' })).toBeInTheDocument()
  })
})
