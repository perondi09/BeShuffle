import { cx } from '../../lib/cx'
import { useDailyToken } from './dailyToken'
import styles from './TokenBadge.module.css'

/** Contador de fichas do visitante, no topo da página. */
export function TokenBadge() {
  const { available } = useDailyToken()

  return (
    <div className={styles.badge} aria-live="polite">
      <span className={cx(styles.coin, !available && styles.spent)} aria-hidden="true">
        1
      </span>
      {available ? '1 ficha hoje' : 'Ficha usada'}
    </div>
  )
}
