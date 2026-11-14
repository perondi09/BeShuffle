import { interpolate, useCurrentFrame } from 'remotion'
import { useMola } from '../animacao'
import { Cena } from '../componentes/Cena'
import { Selo } from '../componentes/Selo'
import { borda, cor, fonteDisplay, sombra } from '../tema'

/** Cena 3: o BeShuffle entra em cena. */
export const Ideia = ({ duracao }: { duracao: number }) => {
  const frame = useCurrentFrame()
  const logo = useMola(0.3, { damping: 8, stiffness: 160 })
  const disco = useMola(0.15, { damping: 12 })
  return (
    <Cena etiqueta="CONHEÇA" legenda="o BeShuffle é uma jukebox que escolhe por você" duracao={duracao} mascote={{ pose: 'comemora', tamanho: 320 }}>
      <div
        style={{
          position: 'absolute',
          left: 540 - 230,
          top: 330,
          width: 460,
          height: 460,
          borderRadius: '50%',
          border: borda(8),
          boxShadow: sombra(16),
          background: `repeating-radial-gradient(circle, ${cor.vinil1} 0 12px, ${cor.vinil2} 12px 16px)`,
          transform: `scale(${disco}) rotate(${frame * 4}deg)`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <div style={{ width: 150, height: 150, borderRadius: '50%', background: cor.tomate, border: borda(6) }} />
      </div>
      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          top: 820,
          textAlign: 'center',
          fontFamily: fonteDisplay,
          fontSize: 170,
          color: cor.tinta,
          textShadow: `8px 8px 0 ${cor.papel}`,
          transform: `scale(${interpolate(logo, [0, 1], [3, 1])})`,
          opacity: Math.min(1, logo * 3),
        }}
      >
        BeShuffle
      </div>
      <Selo x={255} y={1055} atraso={0.9} fundo={cor.menta} giro={-3} tamanho={44}>
        um álbum novo por dia
      </Selo>
    </Cena>
  )
}
