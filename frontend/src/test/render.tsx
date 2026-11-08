import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { vi } from 'vitest'
import { AppRoutes } from '../AppRoutes'

/** Renderiza o site inteiro já numa rota. */
export function renderAt(path = '/') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AppRoutes />
    </MemoryRouter>,
  )
}

export const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })

type Responder = (init?: RequestInit) => Response

/** Mocka o fetch por rota; rota não listada falha o teste. */
export function mockApi(responses: Record<string, Responder>) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
    const respond = responses[String(input)]
    if (!respond) throw new Error(`rota não mockada: ${String(input)}`)
    return respond(init)
  })
}
