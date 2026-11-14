import { Mascote } from '../componentes/Mascote'
import { borda, cor, fonteDisplay, sombra } from '../tema'
import { Nota, Palco } from './Palco'

const CHAO = 820

/** Banner do README (mesma proporção do diagrama da arquitetura): a placa do BeShuffle e o Radinho apoiado nela. */
export const Banner = () => (
  <Palco chao={CHAO}>
    {/* disco grande ao fundo */}
    <div
      style={{
        position: 'absolute',
        left: -120,
        top: 60,
        width: 520,
        height: 520,
        borderRadius: '50%',
        border: borda(8),
        background: `repeating-radial-gradient(circle, ${cor.vinil1} 0 12px, ${cor.vinil2} 12px 16px)`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div style={{ width: 170, height: 170, borderRadius: '50%', background: cor.rosa, border: borda(7) }} />
    </div>

    <div
      style={{
        position: 'absolute',
        left: 230,
        top: 470,
        transform: 'rotate(-4deg)',
        background: cor.gema,
        border: borda(6),
        boxShadow: sombra(8),
        borderRadius: 999,
        padding: '12px 38px 18px',
        fontFamily: fonteDisplay,
        fontSize: 44,
        color: cor.tinta,
        whiteSpace: 'nowrap',
      }}
    >
      a jukebox que escolhe por você
    </div>

    {/* placa com o nome */}
    <div
      style={{
        position: 'absolute',
        left: 70,
        top: 590,
        width: 940,
        height: 230,
        background: cor.tinta,
        border: borda(8),
        borderRadius: 44,
        boxShadow: `14px 14px 0 ${cor.tomate}`,
        transform: 'rotate(-2deg)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          fontFamily: fonteDisplay,
          fontSize: 150,
          lineHeight: 1,
          color: cor.papel,
          textShadow: `7px 7px 0 ${cor.tomate}`,
          marginTop: -18,
        }}
      >
        BeShuffle
      </div>
      <div style={{ width: 700, height: 18, borderRadius: 9, background: cor.gema, marginTop: 12 }} />
    </div>

    <Nota x={1000} y={130} tamanho={110} giro={12} />
    <Nota x={1250} y={250} tamanho={80} giro={-10} simbolo="♫" tinta={cor.tomate} />
    <Nota x={560} y={180} tamanho={70} giro={-14} tinta={cor.papel} />

    <Mascote pose="apoiado" x={1180} y={CHAO + 40} tamanho={420} atraso={0} />

    <div style={{ position: 'absolute', left: 80, top: CHAO + 34, fontFamily: fonteDisplay, fontSize: 46, color: cor.tinta }}>
      beshuffle.com.br
    </div>
  </Palco>
)
