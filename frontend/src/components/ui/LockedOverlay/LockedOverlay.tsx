import type { ReactNode } from 'react'
import styles from './LockedOverlay.module.css'

interface LockedOverlayProps {
  message: string
  action?: ReactNode
}

/** Véu com cadeado sobre um cartão que só usuário logado vê. O pai precisa de `position: relative`. */
export function LockedOverlay({ message, action }: LockedOverlayProps) {
  return (
    <div className={styles.overlay}>
      <div className={styles.padlock} aria-hidden="true" />
      <p className={styles.message}>{message}</p>
      {action}
    </div>
  )
}
