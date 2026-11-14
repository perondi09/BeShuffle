import type { ReactNode } from 'react'
import { useMola } from '../animacao'
import { borda, cor, fonteDisplay, sombra } from '../tema'

interface SeloProps {
  children: ReactNode
  x: number
  y: number
  fundo?: string
  giro?: number
  atraso?: number
  tamanho?: number
}

/** Adesivo inclinado como o "Álbum do dia" do site. */
export const Selo = ({ children, x, y, fundo = cor.gema, giro = -8, atraso = 0.6, tamanho = 46 }: SeloProps) => {
  const m = useMola(atraso, { damping: 10, stiffness: 170 })
  return (
    <div
      style={{
        position: 'absolute',
        left: x,
        top: y,
        transform: `rotate(${giro}deg) scale(${m})`,
        background: fundo,
        border: borda(5),
        boxShadow: sombra(7),
        borderRadius: 999,
        padding: '10px 30px 14px',
        fontFamily: fonteDisplay,
        fontSize: tamanho,
        color: cor.tinta,
        whiteSpace: 'nowrap',
      }}
    >
      {children}
    </div>
  )
}
