import { type ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { logout, me, type SessionUser } from '../../api/auth'
import { SessionContext } from './session'

/** Descobre quem está logado ao abrir o site e compartilha com o topo e os formulários. */
export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null)
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    try {
      setUser(await me())
    } catch {
      // 401 = visitante; API fora do ar também vira visitante, sem quebrar a página.
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  const signOut = useCallback(async () => {
    try {
      await logout()
    } finally {
      setUser(null)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const value = useMemo(() => ({ user, loading, refresh, signOut }), [user, loading, refresh, signOut])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
