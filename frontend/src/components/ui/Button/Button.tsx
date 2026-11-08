import type { AnchorHTMLAttributes, ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router'
import { cx } from '../../../lib/cx'
import styles from './Button.module.css'

export type ButtonVariant = 'papel' | 'tomate' | 'menta' | 'rosa' | 'gema'
export type ButtonSize = 'm' | 'g'

interface BaseProps {
  variant?: ButtonVariant
  size?: ButtonSize
  /** Usa a fonte de display (o botão principal da página). */
  display?: boolean
  /** Ocupa toda a largura disponível. */
  block?: boolean
  className?: string
  children: ReactNode
}

type AsButton = BaseProps & ButtonHTMLAttributes<HTMLButtonElement> & { to?: never; href?: never }
type AsRoute = BaseProps & { to: string; href?: never }
type AsAnchor = BaseProps & AnchorHTMLAttributes<HTMLAnchorElement> & { href: string; to?: never }

export type ButtonProps = AsButton | AsRoute | AsAnchor

/** Botão padrão do BeShuffle: borda grossa, sombra chapada e "afunda" ao clicar. Vira link com `to` ou `href`. */
export function Button(props: ButtonProps) {
  const { variant = 'papel', size = 'm', display = false, block = false, className, children, ...rest } = props
  const classes = cx(
    styles.button,
    styles[variant],
    size === 'g' && styles.g,
    display && 'display',
    block && styles.block,
    className,
  )

  if (rest.to !== undefined) {
    return (
      <Link className={classes} to={rest.to}>
        {children}
      </Link>
    )
  }

  if (rest.href !== undefined) {
    return (
      <a className={classes} {...(rest as AnchorHTMLAttributes<HTMLAnchorElement>)}>
        {children}
      </a>
    )
  }

  const { type = 'button', ...buttonProps } = rest as ButtonHTMLAttributes<HTMLButtonElement>
  return (
    <button className={classes} type={type} {...buttonProps}>
      {children}
    </button>
  )
}
