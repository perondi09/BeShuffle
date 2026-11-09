import { createContext, useContext } from 'react'

/**
 * Ficha diária do visitante: 1 sorteio por dia (docs/CONTEXTO.md §1).
 * Guardada no navegador só para a interface; a API ainda precisa garantir a regra.
 */
export interface DailyToken {
  available: boolean
  spend: () => void
}

export const STORAGE_KEY = 'beshuffle:ficha-usada'

/** Data local no formato AAAA-MM-DD. */
export function today(now: Date = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

export function readSpentDay(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

export function saveSpentDay(day: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, day)
  } catch {
    // Sem armazenamento (aba privada, bloqueio): a ficha vale só até recarregar a página.
  }
}

export const DailyTokenContext = createContext<DailyToken | null>(null)

export function useDailyToken(): DailyToken {
  const token = useContext(DailyTokenContext)
  if (!token) throw new Error('useDailyToken precisa estar dentro de <DailyTokenProvider>')
  return token
}
