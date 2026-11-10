import { afterEach, describe, expect, it, vi } from 'vitest'
import { getJson, postJson } from './http'

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
})
