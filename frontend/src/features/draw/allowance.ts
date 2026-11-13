import { createContext, useContext } from 'react'
import type { Allowance } from '../../api/draws'

/**
 * Sorteios do dia, contados pela API (visitante 1, conta logada 10). O topo mostra o saldo e o palco
 * decide se o botão de sortear fica ativo.
 */
export interface AllowanceState {
  /** null enquanto carrega ou se a API não respondeu. */
  allowance: Allowance | null
  /** Lê o saldo de novo na API. */
  refresh: () => Promise<void>
  /** Guarda o saldo que veio junto com um sorteio, sem outra chamada. */
  update: (allowance: Allowance) => void
}

export const AllowanceContext = createContext<AllowanceState | null>(null)

export function useAllowance(): AllowanceState {
  const state = useContext(AllowanceContext)
  if (!state) throw new Error('useAllowance precisa estar dentro de <AllowanceProvider>')
  return state
}
