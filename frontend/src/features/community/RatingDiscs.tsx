import { cx } from '../../lib/cx'
import { formatRating, splitRating } from '../../lib/rating'
import styles from './community.module.css'

interface RatingDiscsProps {
  /** Em estrelas, de 0 (sem nota) a 5, de meia em meia. */
  value: number
  onChange: (value: number) => void
}

const DISCS = [1, 2, 3, 4, 5]

/**
 * Nota em discos de vinil, de meia em meia estrela: cada disco tem duas metades clicáveis (a esquerda vale x,5 e a
 * direita vale x inteiro). Por baixo é um grupo de 10 botões de rádio, então funciona com teclado e leitor de tela.
 */
export function RatingDiscs({ value, onChange }: RatingDiscsProps) {
  const { full, half } = splitRating(value)

  return (
    <div className={styles.ratings} role="radiogroup" aria-label="Nota de 0,5 a 5">
      {DISCS.map((n) => (
        <div key={n} className={styles.discWrap}>
          <span className={cx(styles.rating, n <= full && styles.ratingOn, half && n === full + 1 && styles.ratingHalf)} />
          {[n - 0.5, n].map((stars, i) => (
            <button
              key={stars}
              type="button"
              role="radio"
              aria-checked={stars === value}
              aria-label={`${formatRating(stars)} de 5`}
              className={cx(styles.discHalf, i === 0 ? styles.discHalfLeft : styles.discHalfRight)}
              onClick={() => onChange(stars)}
            />
          ))}
        </div>
      ))}
    </div>
  )
}
