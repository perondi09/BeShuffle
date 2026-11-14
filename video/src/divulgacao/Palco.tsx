import type { ReactNode } from 'react'
import { AbsoluteFill } from 'remotion'
import { borda, cor, fonteTexto } from '../tema'

/** Fundo das imagens de divulgação: lilás pontilhado do site e um chão de palco a partir de `chao` px. */
export const Palco = ({ chao, children }: { chao: number; children: ReactNode }) => (
  <AbsoluteFill
    style={{
      background: cor.lilas,
      backgroundImage: `radial-gradient(${cor.lilasEscuro} 4px, transparent 4.5px)`,
      backgroundSize: '44px 44px',
      overflow: 'hidden',
    }}
  >
    <div
      style={{
        position: 'absolute',
        left: 0,
        right: 0,
        top: chao,
        bottom: 0,
        background: cor.lilasEscuro,
        borderTop: borda(8),
        backgroundImage: 'repeating-linear-gradient(90deg, transparent 0 70px, rgba(28,21,48,0.08) 70px 140px)',
      }}
    />
    {children}
  </AbsoluteFill>
)

interface NotaProps {
  x: number
  y: number
  tamanho: number
  giro: number
  simbolo?: string
  tinta?: string
}

/** Nota musical solta, para decorar. */
export const Nota = ({ x, y, tamanho, giro, simbolo = '♪', tinta = cor.tinta }: NotaProps) => (
  <span
    style={{
      position: 'absolute',
      left: x,
      top: y,
      fontFamily: fonteTexto,
      fontWeight: 600,
      fontSize: tamanho,
      color: tinta,
      transform: `rotate(${giro}deg)`,
      lineHeight: 1,
    }}
  >
    {simbolo}
  </span>
)
