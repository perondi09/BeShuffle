import { useMola } from '../animacao'
import { borda, cor, fonteDisplay, sombra } from '../tema'

const Fita = ({ lado }: { lado: 'esq' | 'dir' }) => (
  <div
    style={{
      position: 'absolute',
      top: -18,
      [lado === 'esq' ? 'left' : 'right']: -34,
      width: 96,
      height: 36,
      background: 'rgba(255, 201, 60, 0.9)',
      border: '3px solid rgba(28,21,48,0.25)',
      transform: `rotate(${lado === 'esq' ? -28 : 28}deg)`,
    }}
  />
)

/** Etiqueta de papel colada com fita no topo da cena ("PASSO 1: ..."). */
export const Etiqueta = ({ texto, atraso = 0.05 }: { texto: string; atraso?: number }) => {
  const m = useMola(atraso, { damping: 11, stiffness: 160 })
  return (
    <div style={{ position: 'absolute', top: 150, left: 0, right: 0, display: 'flex', justifyContent: 'center' }}>
      <div
        style={{
          position: 'relative',
          transform: `rotate(-2deg) scale(${m}) translateY(${(1 - m) * -60}px)`,
          background: cor.papel,
          border: borda(6),
          boxShadow: sombra(10),
          padding: '22px 52px 26px',
          fontFamily: fonteDisplay,
          fontSize: 56,
          color: cor.tinta,
          letterSpacing: 1,
          whiteSpace: 'nowrap',
        }}
      >
        {texto}
        <Fita lado="esq" />
        <Fita lado="dir" />
      </div>
    </div>
  )
}
