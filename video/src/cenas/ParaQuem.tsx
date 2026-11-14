import { useMola } from '../animacao'
import { Carimbo } from '../componentes/Carimbo'
import { Cartao } from '../componentes/Cartao'
import { Cena, type PropsDaCena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, fonteTexto } from '../tema'

const PESSOAS = [
  ['Quem quer sair da bolha', 'e ouvir algo diferente'],
  ['Quem ama descobrir artista', 'antes de todo mundo'],
  ['Quem precisa de trilha', 'para estudar ou trabalhar'],
  ['Quem nunca sabe', 'o que colocar para tocar'],
]

const Pessoa = ({ indice, texto, detalhe }: { indice: number; texto: string; detalhe: string }) => {
  const entra = useMola(0.3 + indice * 0.32, { damping: 14, stiffness: 160 })
  const marca = useMola(0.55 + indice * 0.32, { damping: 9, stiffness: 200 })
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 26, transform: `translateX(${(1 - entra) * -600}px)`, opacity: entra }}>
      <div
        style={{
          width: 76,
          height: 76,
          flex: 'none',
          borderRadius: 20,
          border: borda(5),
          background: marca > 0.05 ? cor.gema : cor.papel,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontFamily: fonteDisplay,
          fontSize: 52,
          color: cor.tinta,
        }}
      >
        <span style={{ transform: `scale(${marca})` }}>✓</span>
      </div>
      <div style={{ fontFamily: fonteTexto }}>
        <div style={{ fontSize: 46, fontWeight: 600, color: cor.tinta, lineHeight: 1.1 }}>{texto}</div>
        <div style={{ fontSize: 34, color: cor.vinil2 }}>{detalhe}</div>
      </div>
    </div>
  )
}

/** Cena 7: para quem o BeShuffle é bom. */
export const ParaQuem = ({ duracao, fala }: PropsDaCena) => (
  <Cena etiqueta="PRA QUEM É?" fala={fala} duracao={duracao} mascote={{ pose: 'comemora', x: 850, tamanho: 300 }}>
    <Cartao atraso={0.15} style={{ left: 70, right: 70, top: 330, padding: '44px 40px', display: 'grid', gap: 34 }}>
      {PESSOAS.map(([texto, detalhe], i) => (
        <Pessoa key={texto} indice={i} texto={texto} detalhe={detalhe} />
      ))}
    </Cartao>
    <Carimbo texto="É PRA VOCÊ!" atraso={1.9} x={400} y={1250} giro={-8} tamanho={86} cor={cor.menta} />
  </Cena>
)
