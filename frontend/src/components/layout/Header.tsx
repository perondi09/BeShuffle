import { useLocation } from 'react-router'
import { TokenBadge } from '../../features/draw/TokenBadge'
import { Button } from '../ui'
import { Brand } from './Brand'
import styles from './Header.module.css'

const AUTH_ROUTES = ['/entrar', '/cadastro']

/** Topo de todas as telas: logo, ficha do dia e botão Entrar. */
export function Header() {
  const { pathname } = useLocation()
  const onAuthPage = AUTH_ROUTES.includes(pathname)

  return (
    <header className={styles.header}>
      <Brand />
      <div className={styles.spacer} />
      {onAuthPage ? null : (
        <>
          <TokenBadge />
          <Button variant="rosa" to="/entrar" className={styles.login}>
            Entrar
          </Button>
        </>
      )}
    </header>
  )
}
