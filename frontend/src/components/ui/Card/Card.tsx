import type { HTMLAttributes, ReactNode } from 'react'
import { cx } from '../../../lib/cx'
import styles from './Card.module.css'

interface CardProps extends HTMLAttributes<HTMLElement> {
  /** `palco` é o cartão grande do topo; `cartao` é o bloco comum. */
  size?: 'cartao' | 'palco'
  as?: 'section' | 'div' | 'article'
  children: ReactNode
}

/** Superfície branca com borda e sombra chapada. */
export function Card({ size = 'cartao', as: Tag = 'section', className, children, ...rest }: CardProps) {
  return (
    <Tag className={cx(styles.card, styles[size], className)} {...rest}>
      {children}
    </Tag>
  )
}
