import { interpolate, spring, useCurrentFrame, useVideoConfig } from 'remotion'
import { borda, cor, fonteTexto } from '../tema'

interface LegendaProps {
  texto: string
  /** Quantos quadros a fala dura: as palavras se espalham por ela, no ritmo da voz. */
  quadros: number
  /** Quando a fala começa, em segundos. */
  inicio?: number
}

/** Legenda palavra por palavra, com a palavra da vez em destaque (como nos Shorts de referência). */
export const Legenda = ({ texto, quadros, inicio = 0.3 }: LegendaProps) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const palavras = texto.split(' ')
  const comeco = inicio * fps
  const passo = (quadros * 0.9) / palavras.length
  const atual = Math.floor((frame - comeco) / passo)

  return (
    <div
      style={{
        position: 'absolute',
        top: 1600,
        left: 70,
        right: 70,
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',
        gap: '10px 16px',
        fontFamily: fonteTexto,
        fontWeight: 600,
        fontSize: 60,
        lineHeight: 1.15,
        color: cor.tinta,
      }}
    >
      {palavras.map((palavra, i) => {
        if (i > atual) return null
        const p = spring({ frame: frame - (comeco + i * passo), fps, config: { damping: 14, stiffness: 220 } })
        const daVez = i === atual
        return (
          <span
            key={i}
            style={{
              display: 'inline-block',
              padding: '4px 16px 8px',
              borderRadius: 16,
              background: daVez ? cor.gema : 'rgba(255,255,255,0.75)',
              border: daVez ? borda(4) : '4px solid transparent',
              transform: `scale(${interpolate(p, [0, 1], [0.6, 1])}) translateY(${(1 - p) * 20}px)`,
            }}
          >
            {palavra}
          </span>
        )
      })}
    </div>
  )
}
