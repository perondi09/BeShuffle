import { interpolate, useCurrentFrame, useVideoConfig } from 'remotion'
import { useMola } from '../animacao'
import { cor } from '../tema'

/**
 * Poses das imagens de divulgação: `apoiado` encosta na logo (braço esquerdo em cima dela, pernas cruzadas) e `ola`
 * acena com a mão bem para cima.
 */
export type Pose = 'feliz' | 'acena' | 'pensando' | 'surpreso' | 'comemora' | 'apoiado' | 'ola'

interface MascoteProps {
  pose?: Pose
  /** Centro horizontal, em pixels. */
  x?: number
  /** Onde ficam os pés, em pixels. */
  y?: number
  tamanho?: number
  atraso?: number
}

const MENTA_ESCURO = '#27b87b'
const PAINEL = '#fff7e6'

/**
 * O "Radinho": um rádio retrô de desenho animado. O rosto fica no painel do alto-falante, o dial de sintonia
 * anda e os botões giram. Braços e pernas de "mangueira", mãos amarelas e tênis laranja.
 */
export const Mascote = ({ pose = 'feliz', x = 540, y = 1520, tamanho = 400, atraso = 0.15 }: MascoteProps) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const t = frame / fps
  const apoiado = pose === 'apoiado'
  const entrada = useMola(atraso, { damping: 9, stiffness: 140 })
  const pulo = pose === 'comemora' ? -Math.abs(Math.sin(t * Math.PI * 2.4)) * 26 : Math.sin(t * Math.PI * 2) * 6
  const piscando = frame % 70 < 4 ? 0.12 : 1
  const bracoDir =
    pose === 'acena' ? -40 + Math.sin(t * Math.PI * 5) * 28 : pose === 'ola' ? -98 + Math.sin(t * Math.PI * 4) * 10 : pose === 'comemora' ? -70 + Math.sin(t * Math.PI * 6) * 12 : apoiado ? -18 : 0
  const bracoEsq = pose === 'comemora' ? 70 - Math.sin(t * Math.PI * 6) * 12 : pose === 'pensando' ? 60 : apoiado ? 72 : 0
  const olhar = pose === 'pensando' ? { x: 4, y: -5 } : apoiado ? { x: -3, y: 0 } : { x: 1, y: 1 }
  const altura = tamanho * 1.2
  const inclinacao = apoiado ? -7 : interpolate(Math.sin(t * Math.PI), [-1, 1], [-3, 3])
  const ponteiro = 60 + Math.sin(t * Math.PI * 0.9) * 26
  const botao = frame * 4
  const nota = (t * 0.8) % 1
  const sorriso = pose === 'comemora' || pose === 'acena' || pose === 'ola' || apoiado

  return (
    <svg
      viewBox="0 0 200 240"
      width={tamanho}
      height={altura}
      style={{
        position: 'absolute',
        left: x - tamanho / 2,
        top: y - altura,
        overflow: 'visible',
        transform: `translateY(${pulo + (1 - entrada) * 300}px) scale(${entrada}) rotate(${inclinacao}deg)`,
        transformOrigin: '50% 100%',
      }}
    >
      <ellipse cx="100" cy="234" rx="62" ry="9" fill="rgba(28,21,48,0.22)" />
      {/* pernas e tênis */}
      <path
        d={apoiado ? 'M74 170 Q92 198 108 222' : 'M74 170 Q70 196 68 222'}
        stroke={cor.tinta}
        strokeWidth="8"
        fill="none"
        strokeLinecap="round"
      />
      <path
        d={apoiado ? 'M124 170 Q106 198 90 222' : 'M124 170 Q128 196 132 222'}
        stroke={cor.tinta}
        strokeWidth="8"
        fill="none"
        strokeLinecap="round"
      />
      {/* tênis; com as pernas cruzadas, cada um troca de lado */}
      <g transform={`translate(${apoiado ? 40 : 0} 0)`}>
        <path d="M48 226 Q50 212 68 212 Q84 212 86 226 Z" fill={cor.tomate} stroke={cor.tinta} strokeWidth="4" strokeLinejoin="round" />
      </g>
      <g transform={`translate(${apoiado ? -42 : 0} 0)`}>
        <path d="M114 226 Q116 212 132 212 Q150 212 152 226 Z" fill={cor.tomate} stroke={cor.tinta} strokeWidth="4" strokeLinejoin="round" />
      </g>
      {/* braços de mangueira com mãos amarelas */}
      <g transform={`rotate(${bracoEsq} 24 118)`}>
        <path d="M24 118 Q4 128 6 152" stroke={cor.tinta} strokeWidth="8" fill="none" strokeLinecap="round" />
        <circle cx="6" cy="155" r="11" fill={cor.gema} stroke={cor.tinta} strokeWidth="4" />
      </g>
      <g transform={`rotate(${bracoDir} 176 118)`}>
        <path d="M176 118 Q196 128 194 152" stroke={cor.tinta} strokeWidth="8" fill="none" strokeLinecap="round" />
        <circle cx="194" cy="155" r="11" fill={cor.gema} stroke={cor.tinta} strokeWidth="4" />
      </g>
      {/* antena e uma nota musical saindo */}
      <path d="M128 56 L166 10" stroke={cor.tinta} strokeWidth="5" strokeLinecap="round" />
      <circle cx="167" cy="9" r="6" fill={cor.tomate} stroke={cor.tinta} strokeWidth="3.5" />
      <text
        x={150 + nota * 16}
        y={36 - nota * 26}
        fontSize="26"
        fontWeight="700"
        fill={cor.tinta}
        opacity={interpolate(nota, [0, 0.2, 0.8, 1], [0, 1, 1, 0])}
      >
        ♪
      </text>
      {/* lateral do rádio (profundidade) */}
      <rect x="32" y="50" width="150" height="114" rx="22" fill={MENTA_ESCURO} stroke={cor.tinta} strokeWidth="5" />
      {/* frente */}
      <rect x="20" y="58" width="150" height="114" rx="22" fill={cor.menta} stroke={cor.tinta} strokeWidth="5" />
      <path d="M32 74 Q32 66 42 66 L90 66" stroke="rgba(255,255,255,0.6)" strokeWidth="5" fill="none" strokeLinecap="round" />
      {/* painel do alto-falante, com o rosto */}
      <rect x="32" y="72" width="126" height="58" rx="14" fill={PAINEL} stroke={cor.tinta} strokeWidth="4" />
      {[46, 58, 70, 120, 132, 144].map((gx) => (
        <line key={gx} x1={gx} y1="80" x2={gx} y2="122" stroke="rgba(28,21,48,0.10)" strokeWidth="3" strokeLinecap="round" />
      ))}
      {[74, 118].map((cx) => (
        <g key={cx}>
          <ellipse cx={cx} cy="96" rx="13" ry={15 * piscando} fill={cor.papel} stroke={cor.tinta} strokeWidth="3.5" />
          {piscando > 0.5 ? (
            <>
              <circle cx={cx + olhar.x} cy={97 + olhar.y} r="7" fill={cor.tinta} />
              <circle cx={cx + olhar.x + 2.5} cy={94 + olhar.y} r="2.2" fill={cor.papel} />
            </>
          ) : null}
        </g>
      ))}
      <circle cx="56" cy="114" r="5" fill={cor.rosa} opacity="0.85" />
      <circle cx="136" cy="114" r="5" fill={cor.rosa} opacity="0.85" />
      {pose === 'surpreso' ? (
        <ellipse cx="96" cy="117" rx="4.5" ry="5.5" fill={cor.tinta} />
      ) : sorriso ? (
        <path d="M86 112 Q96 126 106 112 Z" fill={cor.tomate} stroke={cor.tinta} strokeWidth="3" strokeLinejoin="round" />
      ) : (
        <path d="M88 114 Q96 121 104 114" stroke={cor.tinta} strokeWidth="3.5" fill="none" strokeLinecap="round" />
      )}
      {/* dial de sintonia e botões */}
      <rect x="54" y="138" width="82" height="22" rx="6" fill={cor.papel} stroke={cor.tinta} strokeWidth="3.5" />
      {[64, 76, 88, 100, 112, 124].map((mx) => (
        <line key={mx} x1={mx} y1="143" x2={mx} y2="149" stroke={cor.tinta} strokeWidth="2" />
      ))}
      <line x1={ponteiro} y1="141" x2={ponteiro} y2="157" stroke={cor.tomate} strokeWidth="3.5" strokeLinecap="round" />
      {[38, 152].map((bx) => (
        <g key={bx} transform={`rotate(${botao} ${bx} 149)`}>
          <circle cx={bx} cy="149" r="10" fill={cor.gema} stroke={cor.tinta} strokeWidth="3.5" />
          <line x1={bx} y1="149" x2={bx} y2="141" stroke={cor.tinta} strokeWidth="3" strokeLinecap="round" />
        </g>
      ))}
    </svg>
  )
}
