import { cx } from '../../lib/cx'
import { useAllowance } from './allowance'
import styles from './TokenBadge.module.css'

/** Fichas de hoje no topo da página: 1 para visitante, 10 para conta logada. */
export function TokenBadge() {
  const { allowance } = useAllowance()
  if (!allowance) return null

  const { remaining } = allowance
  const label =
    remaining === 0
      ? allowance.dailyLimit === 1
        ? 'Ficha usada'
        : 'Fichas usadas'
      : `${remaining} ${remaining === 1 ? 'ficha' : 'fichas'} hoje`

  return (
    <div className={styles.badge} aria-live="polite">
      <span className={cx(styles.coin, remaining === 0 && styles.spent)} aria-hidden="true">
        {remaining}
      </span>
      {label}
    </div>
  )
}
