import { cx } from '../../../lib/cx'
import styles from './Avatar.module.css'
import { avatarColor } from './avatarColors'

interface AvatarProps {
  /** Nome de quem é: a inicial vai dentro do círculo. O avatar é só enfeite, o nome aparece ao lado. */
  name: string
  color?: string | null
  size?: 'p' | 'g'
  className?: string
}

/** Avatar de conta: a inicial do nome sobre uma cor da marca, com a borda grossa do resto do site. */
export function Avatar({ name, color, size = 'p', className }: AvatarProps) {
  const initial = name.trim().charAt(0).toUpperCase() || '?'
  return (
    <span className={cx(styles.avatar, styles[avatarColor(color)], size === 'g' && styles.g, className)} aria-hidden="true">
      {initial}
    </span>
  )
}
