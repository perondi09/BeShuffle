/** Erro de resposta da API, com o status HTTP para quem chamou decidir a mensagem. */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { Accept: 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    throw new ApiError(response.status, `A API respondeu ${response.status}`)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

/** Token CSRF que a API manda no cookie XSRF-TOKEN; todo POST precisa devolvê-lo no header. */
function xsrfToken(): string | undefined {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : undefined
}

export const getJson = <T>(path: string) => request<T>(path)

export const postJson = <T>(path: string, body: unknown) => {
  const token = xsrfToken()
  return request<T>(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(token ? { 'X-XSRF-TOKEN': token } : {}) },
    body: JSON.stringify(body),
    credentials: 'same-origin',
  })
}
