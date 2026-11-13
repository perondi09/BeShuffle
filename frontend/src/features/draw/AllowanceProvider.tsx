import { type ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { type Allowance, fetchAllowance } from '../../api/draws'
import { useSession } from '../session/session'
import { AllowanceContext } from './allowance'

/** Busca os sorteios do dia ao abrir o site e de novo quando alguém entra ou sai da conta. */
export function AllowanceProvider({ children }: { children: ReactNode }) {
  const { user, loading } = useSession()
  const [allowance, setAllowance] = useState<Allowance | null>(null)

  const refresh = useCallback(async () => {
    try {
      setAllowance(await fetchAllowance())
    } catch {
      // API fora do ar: o botão continua ativo e o próprio sorteio mostra o erro.
      setAllowance(null)
    }
  }, [])

  const userId = user?.id
  useEffect(() => {
    if (!loading) void refresh()
  }, [loading, userId, refresh])

  const value = useMemo(() => ({ allowance, refresh, update: setAllowance }), [allowance, refresh])

  return <AllowanceContext.Provider value={value}>{children}</AllowanceContext.Provider>
}
