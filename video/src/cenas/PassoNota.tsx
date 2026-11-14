import { useCurrentFrame, useVideoConfig } from 'remotion'
import { Capa, DISCOS } from '../componentes/Capa'
import { Carimbo } from '../componentes/Carimbo'
import { Cartao } from '../componentes/Cartao'
import { Cena, type PropsDaCena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, fonteTexto } from '../tema'

const COMENTARIO = 'Que disco! Ouvi duas vezes seguidas.'

/** Cena 6: dar nota com os discos de vinil e comentar. */
export const PassoNota = ({ duracao, fala }: PropsDaCena) => {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  const t = frame / fps
  const acesos = Math.max(0, Math.min(5, Math.floor((t - 0.7) / 0.18) + 1))
  const letras = Math.max(0, Math.min(COMENTARIO.length, Math.floor((t - 1.8) * 26)))
  const cursor = Math.floor(frame / 8) % 2 === 0

  return (
    <Cena
      etiqueta="PASSO 3: DÊ SUA NOTA"
      fala={fala}
      duracao={duracao}
      mascote={{ pose: t > 2.6 ? 'comemora' : 'feliz', x: 830, tamanho: 320 }}
    >
      <Cartao atraso={0.15} style={{ left: 70, right: 70, top: 330, padding: 40 }}>
        <div style={{ display: 'flex', gap: 28, alignItems: 'center' }}>
          <Capa disco={DISCOS[0]} tamanho={190} />
          <div style={{ fontFamily: fonteTexto }}>
            <div style={{ fontSize: 32, color: cor.vinil2 }}>Álbum do Dia de sábado</div>
            <div style={{ fontFamily: fonteDisplay, fontSize: 60, color: cor.tinta }}>Ondas de Neon</div>
            <div style={{ fontSize: 34, color: cor.tinta }}>Maré Alta</div>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 22, margin: '40px 0 34px' }}>
          {[1, 2, 3, 4, 5].map((n) => {
            const aceso = n <= acesos
            return (
              <div
                key={n}
                style={{
                  width: 130,
                  height: 130,
                  borderRadius: '50%',
                  border: borda(5),
                  background: aceso
                    ? `repeating-radial-gradient(circle, ${cor.vinil1} 0 8px, ${cor.vinil2} 8px 11px)`
                    : 'repeating-radial-gradient(circle, #e7e1f8 0 8px, #f6f3ff 8px 11px)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transform: `rotate(${aceso ? frame * 6 : 0}deg) scale(${aceso ? 1.06 : 1})`,
                }}
              >
                <div
                  style={{ width: 34, height: 34, borderRadius: '50%', background: aceso ? cor.tomate : cor.lilas, border: borda(4) }}
                />
              </div>
            )
          })}
        </div>
        <div
          style={{
            minHeight: 150,
            border: borda(5),
            borderRadius: 26,
            padding: '24px 30px',
            fontFamily: fonteTexto,
            fontSize: 46,
            color: cor.tinta,
            background: '#fbfaff',
          }}
        >
          {COMENTARIO.slice(0, letras)}
          {t > 1.6 && cursor ? <span style={{ color: cor.tomate }}>|</span> : null}
        </div>
      </Cartao>
      <Carimbo texto="5 DE 5" atraso={2.7} x={330} y={1140} giro={-8} tamanho={104} />
    </Cena>
  )
}
