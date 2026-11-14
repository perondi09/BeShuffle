import { AbsoluteFill } from 'remotion'
import { borda, cor } from '../tema'

/** Fundo lilás pontilhado do site, com um "chão" de palco onde o mascote pisa. */
export const Fundo = () => (
  <AbsoluteFill
    style={{
      background: cor.lilas,
      backgroundImage: `radial-gradient(${cor.lilasEscuro} 4px, transparent 4.5px)`,
      backgroundSize: '44px 44px',
    }}
  >
    <div
      style={{
        position: 'absolute',
        left: 0,
        right: 0,
        top: 1470,
        bottom: 0,
        background: cor.lilasEscuro,
        borderTop: borda(8),
        backgroundImage: 'repeating-linear-gradient(90deg, transparent 0 70px, rgba(28,21,48,0.08) 70px 140px)',
      }}
    />
  </AbsoluteFill>
)
