import { createContext, useContext } from 'react'
import type { SessionUser } from '../../api/auth'

/** Quem está logado no site, lido de GET /api/me. */
export interface Session {
  /** null = visitante (ou ainda carregando). */
  user: SessionUser | null
  loading: boolean
  /** Lê de novo quem está logado (depois de entrar ou criar conta). */
  refresh: () => Promise<void>
  signOut: () => Promise<void>
}

export const SessionContext = createContext<Session | null>(null)

export function useSession(): Session {
  const session = useContext(SessionContext)
  if (!session) throw new Error('useSession precisa estar dentro de <SessionProvider>')
  return session
}
