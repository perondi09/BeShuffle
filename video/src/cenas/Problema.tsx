import { useMola } from '../animacao'
import { Capa, DISCOS } from '../componentes/Capa'
import { Carimbo } from '../componentes/Carimbo'
import { Cartao } from '../componentes/Cartao'
import { Cena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, fonteTexto } from '../tema'

const Linha = ({ indice }: { indice: number }) => {
  const m = useMola(0.5 + indice * 0.28, { damping: 14, stiffness: 150 })
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 28,
        padding: '18px 22px',
        borderRadius: 24,
        background: indice % 2 ? cor.papel : cor.lilasClaro,
        border: borda(4),
        transform: `translateX(${(1 - m) * 700}px)`,
      }}
    >
      <Capa disco={DISCOS[3]} tamanho={120} />
      <div style={{ flex: 1, fontFamily: fonteTexto }}>
        <div style={{ fontSize: 44, fontWeight: 600, color: cor.tinta }}>Sempre o Mesmo</div>
        <div style={{ fontSize: 32, color: cor.vinil2 }}>A Banda de Sempre</div>
      </div>
      <div style={{ fontFamily: fonteDisplay, fontSize: 64, color: cor.tomate }}>↻</div>
    </div>
  )
}

/** Cena 2: por que o projeto existe. */
export const Problema = ({ duracao }: { duracao: number }) => (
  <Cena
    etiqueta="SEMPRE O MESMO?"
    legenda="cansou de ouvir sempre os mesmos discos?"
    duracao={duracao}
    mascote={{ pose: 'surpreso', x: 820, tamanho: 330 }}
  >
    <Cartao atraso={0.25} style={{ left: 80, right: 80, top: 330, padding: 36 }}>
      <div style={{ fontFamily: fonteDisplay, fontSize: 52, color: cor.tinta, marginBottom: 22 }}>Tocando agora</div>
      <div style={{ display: 'grid', gap: 18 }}>
        <Linha indice={0} />
        <Linha indice={1} />
        <Linha indice={2} />
      </div>
    </Cartao>
    <Carimbo texto="DE NOVO?" atraso={1.9} x={540} y={760} giro={-12} tamanho={120} />
  </Cena>
)
