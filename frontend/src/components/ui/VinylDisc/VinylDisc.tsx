import { cx } from '../../../lib/cx'
import styles from './VinylDisc.module.css'

interface VinylDiscProps {
  /** Imagem do rótulo central (a capa do álbum). Sem imagem, o rótulo fica tomate. */
  labelUrl?: string
  spinning?: boolean
  className?: string
}

/** Disco de vinil decorativo; gira quando `spinning` e respeita `prefers-reduced-motion`. */
export function VinylDisc({ labelUrl, spinning = false, className }: VinylDiscProps) {
  return (
    <div className={cx(styles.disc, spinning && styles.spinning, className)} aria-hidden="true">
      <div className={styles.shine} />
      <div className={styles.label}>{labelUrl ? <img src={labelUrl} alt="" /> : null}</div>
      <div className={styles.hole} />
    </div>
  )
}
