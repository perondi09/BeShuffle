import type { ReactNode } from 'react'
import { cx } from '../../../lib/cx'
import styles from './Sticker.module.css'

interface StickerProps {
  color?: 'menta' | 'gema' | 'rosa'
  /** Inclinação do adesivo: `leve` (-2°) ou `forte` (-6°). */
  tilt?: 'leve' | 'forte'
  className?: string
  children: ReactNode
}

/** Adesivo em pílula, levemente torto, para destacar um estado ("Álbum do dia", "Sorteado agora"). */
export function Sticker({ color = 'menta', tilt = 'leve', className, children }: StickerProps) {
  return <span className={cx(styles.sticker, styles[color], styles[tilt], className)}>{children}</span>
}
