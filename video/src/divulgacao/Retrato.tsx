import { Mascote } from '../componentes/Mascote'
import { cor, fonteDisplay } from '../tema'
import { Nota, Palco } from './Palco'

const CHAO = 1170

/** Imagem principal de divulgação (4:5, para feed): só a logo e o Radinho acenando. */
export const Retrato = () => (
  <Palco chao={CHAO}>
    <div
      style={{
        position: 'absolute',
        left: 0,
        right: 0,
        top: 90,
        textAlign: 'center',
        fontFamily: fonteDisplay,
        fontSize: 210,
        lineHeight: 1,
        color: cor.tinta,
        textShadow: `9px 9px 0 ${cor.papel}`,
      }}
    >
      BeShuffle
    </div>

    <Nota x={110} y={470} tamanho={110} giro={-12} />
    <Nota x={190} y={760} tamanho={80} giro={10} simbolo="♫" tinta={cor.tomate} />
    <Nota x={900} y={430} tamanho={90} giro={14} tinta={cor.papel} />

    <Mascote pose="ola" x={540} y={CHAO + 60} tamanho={700} atraso={0} />
  </Palco>
)
