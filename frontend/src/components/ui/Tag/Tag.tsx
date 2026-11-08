import type { ReactNode } from 'react'
import styles from './Tag.module.css'

/** Etiqueta pequena com informação do álbum (ano, nº de artistas). */
export function Tag({ children }: { children: ReactNode }) {
  return <span className={styles.tag}>{children}</span>
}

/** Lista de etiquetas com espaçamento padrão. */
export function TagList({ children }: { children: ReactNode }) {
  return <div className={styles.list}>{children}</div>
}
