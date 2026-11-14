import type { CSSProperties, ReactNode } from 'react'
import { useMola } from '../animacao'
import { borda, cor, sombra } from '../tema'

interface CartaoProps {
  children: ReactNode
  atraso?: number
  style?: CSSProperties
}

/** Cartão branco com borda grossa e sombra chapada, como os do site. Entra com uma mola. */
export const Cartao = ({ children, atraso = 0.2, style }: CartaoProps) => {
  const m = useMola(atraso, { damping: 13, stiffness: 140 })
  return (
    <div
      style={{
        position: 'absolute',
        background: cor.papel,
        border: borda(6),
        borderRadius: 36,
        boxShadow: sombra(14),
        transform: `translateY(${(1 - m) * 120}px) scale(${0.85 + m * 0.15})`,
        opacity: Math.min(1, m * 1.6),
        ...style,
      }}
    >
      {children}
    </div>
  )
}
