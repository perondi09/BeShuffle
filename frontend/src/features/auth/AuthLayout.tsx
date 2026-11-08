import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { Card, Sticker, VinylDisc } from '../../components/ui'
import styles from './AuthLayout.module.css'

interface AuthLayoutProps {
  title: string
  subtitle: string
  sticker: string
  /** Frase do painel decorativo. */
  pitch: string
  footer: { text: string; linkLabel: string; to: string }
  children: ReactNode
}

/** Moldura das telas de login e cadastro: painel com vinil de um lado, formulário do outro. */
export function AuthLayout({ title, subtitle, sticker, pitch, footer, children }: AuthLayoutProps) {
  return (
    <Card size="palco" className={styles.layout} aria-labelledby="auth-titulo">
      <div className={styles.side} aria-hidden="true">
        <VinylDisc className={styles.vinyl} spinning />
        <p className={`display ${styles.pitch}`}>{pitch}</p>
      </div>

      <div className={styles.content}>
        <Sticker color="menta">{sticker}</Sticker>
        <h1 id="auth-titulo" className={`display ${styles.title}`}>
          {title}
        </h1>
        <p className={styles.subtitle}>{subtitle}</p>
        {children}
        <p className={styles.footer}>
          {footer.text} <Link to={footer.to}>{footer.linkLabel}</Link>
        </p>
      </div>
    </Card>
  )
}
