import { Link } from 'react-router'
import styles from './Brand.module.css'

/** Logo do BeShuffle: disquinho tomate + nome, leva para a home. */
export function Brand() {
  return (
    <Link className={`display ${styles.brand}`} to="/">
      <span className={styles.disc} aria-hidden="true" />
      BeShuffle
    </Link>
  )
}
