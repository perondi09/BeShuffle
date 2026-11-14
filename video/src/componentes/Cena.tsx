import type { ReactNode } from 'react'
import { AbsoluteFill } from 'remotion'
import { Etiqueta } from './Etiqueta'
import { Fundo } from './Fundo'
import { Legenda } from './Legenda'
import { Mascote, type Pose } from './Mascote'

interface CenaProps {
  etiqueta?: string
  legenda: string
  duracao: number
  mascote?: { pose: Pose; x?: number; y?: number; tamanho?: number } | null
  children?: ReactNode
}

/** Palco comum: etiqueta no topo, conteúdo no meio, mascote no chão e legenda palavra por palavra. */
export const Cena = ({ etiqueta, legenda, duracao, mascote = { pose: 'feliz' }, children }: CenaProps) => (
  <AbsoluteFill style={{ overflow: 'hidden' }}>
    <Fundo />
    {children}
    {mascote ? <Mascote {...mascote} /> : null}
    {etiqueta ? <Etiqueta texto={etiqueta} /> : null}
    <Legenda texto={legenda} duracao={duracao} />
  </AbsoluteFill>
)
