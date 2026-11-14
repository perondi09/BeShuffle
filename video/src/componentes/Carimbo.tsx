import { interpolate } from 'remotion'
import { useMola } from '../animacao'
import { cor, fonteDisplay } from '../tema'

interface CarimboProps {
  texto: string
  atraso: number
  x: number
  y: number
  cor?: string
  giro?: number
  tamanho?: number
}

/** Carimbo que cai na tela ("DE NOVO?", "SORTEADO!"). */
export const Carimbo = ({ texto, atraso, x, y, cor: tinta = cor.tomate, giro = -10, tamanho = 84 }: CarimboProps) => {
  const m = useMola(atraso, { damping: 9, stiffness: 180 })
  if (m <= 0.001) return null
  const escala = interpolate(m, [0, 1], [2.6, 1])
  return (
    <div
      style={{
        position: 'absolute',
        left: x,
        top: y,
        transform: `translate(-50%, -50%) rotate(${giro}deg) scale(${escala})`,
        opacity: interpolate(m, [0, 0.25], [0, 1], { extrapolateRight: 'clamp' }),
        border: `9px solid ${tinta}`,
        borderRadius: 18,
        padding: '10px 30px 16px',
        color: tinta,
        background: 'rgba(255,255,255,0.9)',
        fontFamily: fonteDisplay,
        fontSize: tamanho,
        whiteSpace: 'nowrap',
      }}
    >
      {texto}
    </div>
  )
}
