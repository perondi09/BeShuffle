/** Erro de resposta da API, com o status HTTP e a mensagem do backend (quando veio uma). */
export class ApiError extends Error {
  readonly status: number
  /** Código do backend, ex.: DAILY_LIMIT_REACHED. */
  readonly code?: string

  constructor(status: number, message: string, code?: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

/** Lê `{error, message}` do corpo de erro da API, sem quebrar se o corpo não for JSON. */
async function errorFrom(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as { error?: unknown; message?: unknown }
    const message = typeof body.message === 'string' ? body.message : `A API respondeu ${response.status}`
    return new ApiError(response.status, message, typeof body.error === 'string' ? body.error : undefined)
  } catch {
    return new ApiError(response.status, `A API respondeu ${response.status}`)
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { Accept: 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    throw await errorFrom(response)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

/** Token CSRF que a API manda no cookie XSRF-TOKEN; todo POST e PUT precisa devolvê-lo no header. */
function xsrfToken(): string | undefined {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : undefined
}

export const getJson = <T>(path: string) => request<T>(path)

function sendJson<T>(method: 'POST' | 'PUT', path: string, body: unknown) {
  const token = xsrfToken()
  return request<T>(path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { 'X-XSRF-TOKEN': token } : {}) },
    body: JSON.stringify(body),
    credentials: 'same-origin',
  })
}

export const postJson = <T>(path: string, body: unknown) => sendJson<T>('POST', path, body)

export const putJson = <T>(path: string, body: unknown) => sendJson<T>('PUT', path, body)
