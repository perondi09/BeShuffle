import { cx } from '../../lib/cx'
import styles from './community.module.css'

interface RatingDiscsProps {
  value: number
  onChange: (value: number) => void
}

const VALUES = [1, 2, 3, 4, 5]

/** Nota de 1 a 5 em discos de vinil: os discos até a nota escolhida ficam "tocando". */
export function RatingDiscs({ value, onChange }: RatingDiscsProps) {
  return (
    <div className={styles.ratings} role="radiogroup" aria-label="Nota de 1 a 5">
      {VALUES.map((n) => (
        <button
          key={n}
          type="button"
          role="radio"
          aria-checked={n === value}
          aria-label={`${n} de 5`}
          className={cx(styles.rating, n <= value && styles.ratingOn)}
          onClick={() => onChange(n)}
        />
      ))}
    </div>
  )
}
