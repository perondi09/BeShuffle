import type { PontoDoDia } from '../../api/admin'
import { cx } from '../../lib/cx'
import styles from './admin.module.css'
import { dataCurta, diaMes, numero } from './format'

const LARGURA = 600
const ALTURA = 170
const BASE = ALTURA - 4

/**
 * Acessos (barras, papel) e contas novas (linha com pontos gema) por dia. Cada série tem a própria escala:
 * os acessos são muito mais numerosos que as contas novas, e o que interessa em cada uma é o formato da curva.
 */
export function AcessosChart({ pontos }: { pontos: PontoDoDia[] }) {
  if (pontos.length === 0) return <p className={styles.vazio}>Ainda não há dados para mostrar.</p>

  const maxAcessos = Math.max(1, ...pontos.map((p) => p.visits))
  const maxContas = Math.max(1, ...pontos.map((p) => p.newAccounts))
  const passo = LARGURA / pontos.length
  const largura = Math.max(3, passo - 4)
  const x = (i: number) => i * passo + passo / 2
  const yContas = (valor: number) => BASE - (valor / maxContas) * (ALTURA - 24)

  const pico = pontos.reduce((melhor, p) => (p.visits > melhor.visits ? p : melhor), pontos[0])
  const resumo = `Acessos e contas novas por dia, de ${dataCurta(pontos[0].date)} a ${dataCurta(pontos[pontos.length - 1].date)}. Pico de acessos: ${numero(pico.visits)} em ${dataCurta(pico.date)}.`

  return (
    <div>
      <svg className={styles.grafico} viewBox={`0 0 ${LARGURA} ${ALTURA}`} role="img" aria-label={resumo}>
        {pontos.map((p, i) => {
          const altura = Math.round((p.visits / maxAcessos) * (ALTURA - 24))
          return (
            <rect
              key={p.date}
              className={cx(styles.barraAcesso, i === pontos.length - 1 && styles.barraHoje)}
              x={x(i) - largura / 2}
              y={BASE - altura}
              width={largura}
              height={altura}
              rx={2}
            />
          )
        })}
        <polyline className={styles.linhaContas} points={pontos.map((p, i) => `${x(i)},${yContas(p.newAccounts)}`).join(' ')} />
        {pontos.map((p, i) => (
          <circle key={p.date} className={styles.pontoConta} cx={x(i)} cy={yContas(p.newAccounts)} r={3} />
        ))}
        <line className={styles.chao} x1={0} y1={BASE + 1} x2={LARGURA} y2={BASE + 1} />
      </svg>
      <div className={styles.eixo} aria-hidden="true">
        <span>{diaMes(pontos[0].date)}</span>
        <span>{diaMes(pontos[Math.floor(pontos.length / 2)].date)}</span>
        <span>hoje</span>
      </div>
    </div>
  )
}
