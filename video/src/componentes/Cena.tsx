import type { ReactNode } from 'react'
import { AbsoluteFill, interpolate, useCurrentFrame } from 'remotion'
import { Etiqueta } from './Etiqueta'
import { Fundo } from './Fundo'
import { Legenda } from './Legenda'
import { Mascote, type Pose } from './Mascote'

interface CenaProps {
  etiqueta?: string
  legenda: string
  duracao: number
  mascote?: { pose: Pose; x?: number; y?: number; tamanho?: number } | null
  /** Quadros finais em que tudo encolhe e some, deixando só o palco vazio (o primeiro quadro do vídeo). */
  saida?: number
  children?: ReactNode
}

/** Palco comum: etiqueta no topo, conteúdo no meio, mascote no chão e legenda palavra por palavra. */
export const Cena = ({ etiqueta, legenda, duracao, mascote = { pose: 'feliz' }, saida = 0, children }: CenaProps) => {
  const frame = useCurrentFrame()
  const fator =
    saida > 0 ? interpolate(frame, [duracao - saida, duracao - 1], [1, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }) : 1
  return (
    <AbsoluteFill style={{ overflow: 'hidden' }}>
      <Fundo />
      <AbsoluteFill style={{ transform: `scale(${fator})`, opacity: fator, transformOrigin: '50% 60%' }}>
        {children}
        {mascote ? <Mascote {...mascote} /> : null}
        {etiqueta ? <Etiqueta texto={etiqueta} /> : null}
        <Legenda texto={legenda} duracao={duracao - saida} />
      </AbsoluteFill>
    </AbsoluteFill>
  )
}
