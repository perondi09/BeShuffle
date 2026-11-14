import { useMola } from '../animacao'
import { Carimbo } from '../componentes/Carimbo'
import { Cartao } from '../componentes/Cartao'
import { Cena } from '../componentes/Cena'
import { borda, cor, fonteDisplay, fonteTexto } from '../tema'

const ITENS = [
  ['Java 21 + Spring Boot', 'a API'],
  ['React + TypeScript', 'o site'],
  ['AWS: CloudFront, EC2 e RDS', 'a nuvem'],
  ['Terraform', 'infraestrutura em código'],
  ['CI/CD no GitHub Actions', 'testes a cada mudança'],
]

const Item = ({ indice, texto, detalhe }: { indice: number; texto: string; detalhe: string }) => {
  const entra = useMola(0.35 + indice * 0.3, { damping: 14, stiffness: 160 })
  const marca = useMola(0.6 + indice * 0.3, { damping: 9, stiffness: 200 })
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 26, transform: `translateX(${(1 - entra) * -600}px)`, opacity: entra }}>
      <div
        style={{
          width: 76,
          height: 76,
          flex: 'none',
          borderRadius: 20,
          border: borda(5),
          background: marca > 0.05 ? cor.menta : cor.papel,
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
        <div style={{ fontSize: 32, color: cor.vinil2 }}>{detalhe}</div>
      </div>
    </div>
  )
}

/** Cena 7: o que tem por trás do site. */
export const PorTras = ({ duracao }: { duracao: number }) => (
  <Cena
    etiqueta="POR TRÁS DO PALCO"
    legenda="feito do zero, com padrão de projeto profissional"
    duracao={duracao}
    mascote={{ pose: 'feliz', x: 860, tamanho: 300 }}
  >
    <Cartao atraso={0.15} style={{ left: 70, right: 70, top: 330, padding: '44px 40px', display: 'grid', gap: 30 }}>
      {ITENS.map(([texto, detalhe], i) => (
        <Item key={texto} indice={i} texto={texto} detalhe={detalhe} />
      ))}
    </Cartao>
    <Carimbo texto="FEITO DO ZERO" atraso={2.3} x={420} y={1260} giro={-8} tamanho={80} />
  </Cena>
)
