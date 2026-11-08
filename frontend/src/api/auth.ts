import { ApiError, postJson } from './http'

/*
 * Rotas planejadas em docs/CONTEXTO.md §7 (passo 6). A API ainda não as tem:
 * até lá, as telas mostram que o login não está disponível.
 */

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
  postJson<void>('/api/auth/register', { displayName: input.name, email: input.email, password: input.password })

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
