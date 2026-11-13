import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, getJson, postJson, putJson } from './http'

const ok = () => new Response(null, { status: 204 })

function setCookie(value: string) {
  document.cookie = value
}

describe('http', () => {
  afterEach(() => {
    setCookie('XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/')
  })

  it('manda o token CSRF do cookie no header X-XSRF-TOKEN em todo POST', async () => {
    setCookie('XSRF-TOKEN=abc%2D123; path=/')
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(ok())

    await postJson('/api/auth/login', { email: 'a@b.co', password: 'x' })

    const headers = fetchMock.mock.calls[0][1]?.headers as Record<string, string>
    expect(headers['X-XSRF-TOKEN']).toBe('abc-123')
    expect(headers['Content-Type']).toBe('application/json')
  })

  it('sem o cookie, envia o POST sem o header (a API responde 403)', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(ok())

    await postJson('/api/auth/logout', {})

    const headers = fetchMock.mock.calls[0][1]?.headers as Record<string, string>
    expect(headers).not.toHaveProperty('X-XSRF-TOKEN')
  })

  it('GET não precisa do token', async () => {
    setCookie('XSRF-TOKEN=abc; path=/')
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('{}', { status: 200 }))

    await getJson('/api/me')

    const headers = fetchMock.mock.calls[0][1]?.headers as Record<string, string>
    expect(headers).not.toHaveProperty('X-XSRF-TOKEN')
  })

  it('PUT também manda o token CSRF', async () => {
    setCookie('XSRF-TOKEN=xyz; path=/')
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('{}', { status: 200 }))

    await putJson('/api/reviews/today', { rating: 5 })

    const init = fetchMock.mock.calls[0][1]
    expect(init?.method).toBe('PUT')
    expect((init?.headers as Record<string, string>)['X-XSRF-TOKEN']).toBe('xyz')
  })

  it('erro da API traz o status, o código e a mensagem do backend', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ error: 'DAILY_LIMIT_REACHED', message: 'Volte amanhã!' }), { status: 429 }),
    )

    const error = await postJson('/api/draws', {}).catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 429, code: 'DAILY_LIMIT_REACHED', message: 'Volte amanhã!' })
  })

  it('erro sem corpo JSON vira uma mensagem genérica', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('Bad Gateway', { status: 502 }))

    await expect(getJson('/api/me')).rejects.toMatchObject({ status: 502, message: 'A API respondeu 502' })
  })
})
