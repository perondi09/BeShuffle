import { type ReactNode, useCallback, useMemo, useState } from 'react'
import { DailyTokenContext, readSpentDay, saveSpentDay, today } from './dailyToken'

/** Compartilha a ficha do dia entre o topo (contador) e o palco (botão de sortear). */
export function DailyTokenProvider({ children }: { children: ReactNode }) {
  const [spentDay, setSpentDay] = useState(readSpentDay)

  const spend = useCallback(() => {
    const day = today()
    saveSpentDay(day)
    setSpentDay(day)
  }, [])

  const value = useMemo(() => ({ available: spentDay !== today(), spend }), [spentDay, spend])

  return <DailyTokenContext.Provider value={value}>{children}</DailyTokenContext.Provider>
}
