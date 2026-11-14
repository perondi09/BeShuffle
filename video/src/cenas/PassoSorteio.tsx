import { interpolate, useCurrentFrame, useVideoConfig } from 'remotion'
import { useMola } from '../animacao'
import { Capa, DISCOS } from '../componentes/Capa'
import { Carimbo } from '../componentes/Carimbo'
import { Cartao } from '../componentes/Cartao'
import { Cena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, fonteTexto, sombra } from '../tema'

const ESTILOS = ['Rock', 'Jazz', 'Pagode', 'Funk', 'Indie', 'MPB']
const ESCOLHIDOS: Record<string, number> = { Rock: 0.7, Pagode: 1.0 }

/** Cena 5: escolher estilos, sortear e ver o disco cair. */
export const PassoSorteio = ({ duracao }: { duracao: number }) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const t = frame / fps
  const apertou = t > 1.5 && t < 1.75
  const embaralhando = t >= 1.7 && t < 3.0
  const atual = embaralhando ? DISCOS[Math.floor(frame / 3) % DISCOS.length] : DISCOS[4]
  const capa = useMola(1.7, { damping: 12 })
  const fichas = t < 1.75 ? 10 : 9

  return (
    <Cena
      etiqueta="PASSO 2: SORTEIE"
      legenda="escolha os estilos e sorteie até 10 discos por dia"
      duracao={duracao}
      mascote={{ pose: t > 3 ? 'comemora' : 'feliz', x: 880, tamanho: 280 }}
    >
      <Cartao atraso={0.15} style={{ left: 70, right: 70, top: 320, padding: 34 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ fontFamily: fonteDisplay, fontSize: 50, color: cor.tinta }}>Estilos</div>
          <div
            style={{
              fontFamily: fonteTexto,
              fontWeight: 600,
              fontSize: 36,
              padding: '8px 22px',
              border: borda(4),
              borderRadius: 999,
              background: cor.lilasClaro,
              color: cor.tinta,
            }}
          >
            {fichas} fichas hoje
          </div>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, marginTop: 24 }}>
          {ESTILOS.map((nome) => {
            const marcado = ESCOLHIDOS[nome] !== undefined && t > ESCOLHIDOS[nome]
            return (
              <span
                key={nome}
                style={{
                  fontFamily: fonteTexto,
                  fontWeight: 600,
                  fontSize: 40,
                  padding: '8px 26px 12px',
                  border: borda(4),
                  borderRadius: 999,
                  background: marcado ? cor.gema : cor.papel,
                  boxShadow: marcado ? sombra(5) : 'none',
                  color: cor.tinta,
                }}
              >
                {nome}
              </span>
            )
          })}
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 40 }}>
          <div style={{ transform: `scale(${0.9 + capa * 0.1}) rotate(${embaralhando ? Math.sin(frame) * 4 : 0}deg)` }}>
            <Capa disco={atual} tamanho={420} />
          </div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 40 }}>
          <div
            style={{
              fontFamily: fonteDisplay,
              fontSize: 60,
              color: cor.papel,
              background: cor.tomate,
              border: borda(6),
              borderRadius: 26,
              padding: '16px 56px 24px',
              boxShadow: apertou ? 'none' : sombra(10),
              transform: `translate(${apertou ? 8 : 0}px, ${apertou ? 8 : 0}px)`,
              textShadow: `3px 3px 0 ${cor.tinta}`,
            }}
          >
            {embaralhando ? 'Sorteando…' : 'Sortear álbum'}
          </div>
        </div>
      </Cartao>
      <div
        style={{
          position: 'absolute',
          left: 620,
          top: interpolate(t, [1.0, 1.5, 1.8, 2.3], [1500, 1240, 1250, 1500], {
            extrapolateLeft: 'clamp',
            extrapolateRight: 'clamp',
          }),
          fontSize: 120,
          transform: 'rotate(-20deg)',
          opacity: interpolate(t, [2.0, 2.3], [1, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }),
        }}
      >
        👆
      </div>
      <Carimbo texto="SORTEADO!" atraso={3.05} x={540} y={900} giro={-10} cor={cor.menta} tamanho={110} />
    </Cena>
  )
}
