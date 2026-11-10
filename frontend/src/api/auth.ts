import { ApiError, getJson, postJson } from './http'

/* Rotas de conta da API (backend/README.md). A sessão fica num cookie HttpOnly que o navegador manda sozinho. */

/** Quem está logado (GET /api/me). */
export interface SessionUser {
  id: number
  email: string
  displayName: string
}

export interface LoginInput {
  email: string
  password: string
}

export interface RegisterInput {
  name: string
  email: string
  password: string
}

export const login = (input: LoginInput) => postJson<void>('/api/auth/login', input)

export const register = (input: RegisterInput) =>
  postJson<SessionUser>('/api/auth/register', { displayName: input.name, email: input.email, password: input.password })

export const me = () => getJson<SessionUser>('/api/me')

export const logout = () => postJson<void>('/api/auth/logout', {})

/** Traduz a falha de login/cadastro numa mensagem para a pessoa. */
export function authErrorMessage(error: unknown, action: 'login' | 'cadastro'): string {
  if (error instanceof ApiError) {
    if (error.status === 404 || error.status === 405 || error.status === 501) {
      return action === 'login'
        ? 'O login ainda não está disponível. Volte em breve!'
        : 'O cadastro ainda não está disponível. Volte em breve!'
    }
    if (error.status === 401) return 'E-mail ou senha incorretos.'
    if (error.status === 409) return 'Já existe uma conta com esse e-mail.'
    if (error.status === 400) return 'Confira os dados e tente de novo.'
  }
  return 'Não deu para falar com o servidor. Tente de novo em instantes.'
}
