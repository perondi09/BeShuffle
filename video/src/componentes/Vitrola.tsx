import { useCurrentFrame } from 'remotion'
import { useMola } from '../animacao'
import { borda, cor, sombra } from '../tema'
import { Capa, type DiscoFicticio } from './Capa'

interface VitrolaProps {
  disco: DiscoFicticio
  x: number
  y: number
  tamanho: number
  atraso?: number
}

/** Capa com o vinil saindo de trás e girando, igual ao palco da home do site. */
export const Vitrola = ({ disco, x, y, tamanho, atraso = 0.3 }: VitrolaProps) => {
  const frame = useCurrentFrame()
  const saida = useMola(atraso + 0.35, { damping: 14, stiffness: 90 })
  const entrada = useMola(atraso, { damping: 12, stiffness: 120 })
  return (
    <div style={{ position: 'absolute', left: x, top: y, width: tamanho * 1.55, height: tamanho, transform: `scale(${entrada})` }}>
      <div
        style={{
          position: 'absolute',
          left: tamanho * 0.55 * saida,
          top: tamanho * 0.04,
          width: tamanho * 0.92,
          height: tamanho * 0.92,
          borderRadius: '50%',
          background: `repeating-radial-gradient(circle, ${cor.vinil1} 0 9px, ${cor.vinil2} 9px 12px)`,
          border: borda(6),
          transform: `rotate(${frame * 5}deg)`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <div style={{ width: tamanho * 0.3, height: tamanho * 0.3, borderRadius: '50%', background: disco.fundo, border: borda(5) }} />
        <div
          style={{
            position: 'absolute',
            top: '10%',
            left: '47%',
            width: 14,
            height: tamanho * 0.18,
            background: 'rgba(255,255,255,0.25)',
            borderRadius: 8,
          }}
        />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, boxShadow: sombra(14), borderRadius: 24 }}>
        <Capa disco={disco} tamanho={tamanho} />
      </div>
    </div>
  )
}
