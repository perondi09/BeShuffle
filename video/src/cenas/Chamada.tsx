import { interpolate, useCurrentFrame, useVideoConfig } from 'remotion'
import { useMola } from '../animacao'
import { Cena } from '../componentes/Cena'
import { Selo } from '../componentes/Selo'
import { borda, cor, fonteDisplay, sombra } from '../tema'

/** Cena 8: o convite final com o endereço. */
export const Chamada = ({ duracao }: { duracao: number }) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const url = useMola(0.3, { damping: 10, stiffness: 150 })
  const botao = useMola(0.8)
  const pulso = 1 + Math.sin((frame / fps) * Math.PI * 3) * 0.04
  return (
    <Cena etiqueta="SORTEIE O SEU" legenda="descubra seu próximo disco favorito" duracao={duracao} mascote={{ pose: 'acena' }}>
      <div
        style={{
          position: 'absolute',
          left: 70,
          right: 70,
          top: 400,
          padding: '60px 30px 70px',
          background: cor.papel,
          border: borda(8),
          borderRadius: 44,
          boxShadow: sombra(18),
          textAlign: 'center',
          transform: `rotate(-2deg) scale(${interpolate(url, [0, 1], [0.3, 1])})`,
        }}
      >
        <div style={{ fontFamily: fonteDisplay, fontSize: 118, color: cor.tinta, lineHeight: 1 }}>beshuffle</div>
        <div style={{ fontFamily: fonteDisplay, fontSize: 118, color: cor.tomate, lineHeight: 1.05 }}>.com.br</div>
      </div>
      <div style={{ position: 'absolute', left: 0, right: 0, top: 830, display: 'flex', justifyContent: 'center' }}>
        <div
          style={{
            fontFamily: fonteDisplay,
            fontSize: 64,
            color: cor.papel,
            background: cor.tomate,
            border: borda(6),
            borderRadius: 28,
            padding: '18px 60px 26px',
            boxShadow: sombra(10),
            textShadow: `3px 3px 0 ${cor.tinta}`,
            transform: `scale(${pulso * botao})`,
          }}
        >
          Sortear álbum
        </div>
      </div>
      <Selo x={700} y={340} atraso={1.1} fundo={cor.menta} giro={8} tamanho={44}>
        grátis
      </Selo>
    </Cena>
  )
}
