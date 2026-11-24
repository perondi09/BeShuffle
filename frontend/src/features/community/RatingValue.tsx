import { cx } from '../../lib/cx'
import { formatRating, splitRating } from '../../lib/rating'
import styles from './community.module.css'

const DISCS = [1, 2, 3, 4, 5]

/** A nota de alguém em discos pequenos (só mostra, não edita). O leitor de tela ouve "Nota 3,5 de 5". */
export function RatingValue({ value, className }: { value: number; className?: string }) {
  const { full, half } = splitRating(value)

  return (
    <span className={cx(styles.value, className)} role="img" aria-label={`Nota ${formatRating(value)} de 5`}>
      {DISCS.map((n) => (
        <span
          key={n}
          className={cx(styles.valueDisc, n <= full && styles.ratingOn, half && n === full + 1 && styles.ratingHalf)}
        />
      ))}
    </span>
  )
}
