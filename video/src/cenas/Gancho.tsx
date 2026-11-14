import { interpolate, useCurrentFrame, useVideoConfig } from 'remotion'
import { molaManual, useMola } from '../animacao'
import { Cena, type PropsDaCena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, sombra } from '../tema'

const PALAVRAS = ['Qual', 'disco', 'você', 'vai', 'ouvir', 'hoje?']
const INTERROGACOES = [
  { x: 150, y: 1060, tamanho: 110, cor: cor.tomate, giro: -14 },
  { x: 830, y: 1000, tamanho: 90, cor: cor.tinta, giro: 12 },
  { x: 760, y: 1180, tamanho: 70, cor: cor.papel, giro: -6 },
]

/** Cena 1: a pergunta que abre o vídeo. */
export const Gancho = ({ duracao, fala }: PropsDaCena) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const t = frame / fps
  const caixa = useMola(0.1, { damping: 12 })
  return (
    <Cena fala={fala} duracao={duracao} mascote={{ pose: 'pensando', y: 1540 }}>
      <div
        style={{
          position: 'absolute',
          left: 90,
          right: 90,
          top: 300,
          padding: '60px 50px 70px',
          background: cor.papel,
          border: borda(7),
          borderRadius: 44,
          boxShadow: sombra(16),
          transform: `rotate(-2deg) scale(${caixa})`,
          display: 'flex',
          flexWrap: 'wrap',
          gap: '6px 26px',
          justifyContent: 'center',
        }}
      >
        {PALAVRAS.map((p, i) => {
          const m = molaManual(frame, fps, 0.35 + i * 0.16)
          return (
            <span
              key={p}
              style={{
                fontFamily: fonteDisplay,
                fontSize: 150,
                lineHeight: 1.05,
                color: i === 1 ? cor.tomate : cor.tinta,
                display: 'inline-block',
                transform: `translateY(${(1 - m) * 80}px) scale(${interpolate(m, [0, 1], [0.4, 1])})`,
                opacity: Math.min(1, m * 2),
              }}
            >
              {p}
            </span>
          )
        })}
      </div>
      {INTERROGACOES.map((q, i) => (
        <span
          key={i}
          style={{
            position: 'absolute',
            left: q.x,
            top: q.y + Math.sin(t * 3 + i) * 14,
            fontFamily: fonteDisplay,
            fontSize: q.tamanho,
            color: q.cor,
            WebkitTextStroke: i === 2 ? `4px ${cor.tinta}` : undefined,
            transform: `rotate(${q.giro}deg) scale(${molaManual(frame, fps, 1.4 + i * 0.2)})`,
          }}
        >
          ?
        </span>
      ))}
    </Cena>
  )
}
