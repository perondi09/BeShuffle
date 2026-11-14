import { Link, useLocation, useNavigate } from 'react-router'
import { TokenBadge } from '../../features/draw/TokenBadge'
import { useSession } from '../../features/session/session'
import { Button } from '../ui'
import { Brand } from './Brand'
import styles from './Header.module.css'

const AUTH_ROUTES = ['/entrar', '/cadastro']

/** Topo de todas as telas: logo, ficha do dia e Entrar (ou nome e Sair, para quem está logado). */
export function Header() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { user, signOut } = useSession()
  const onAuthPage = AUTH_ROUTES.includes(pathname)

  async function handleSignOut() {
    await signOut()
    navigate('/', { replace: true })
  }

  return (
    <header className={styles.header}>
      <Brand />
      <div className={styles.spacer} />
      {onAuthPage ? null : (
        <>
          <TokenBadge />
          {user ? (
            <>
              <Link to="/perfil" className={styles.user}>
                Oi, {user.displayName}
              </Link>
              <Button variant="papel" onClick={handleSignOut}>
                Sair
              </Button>
            </>
          ) : (
            <Button variant="rosa" to="/entrar" className={styles.login}>
              Entrar
            </Button>
          )}
        </>
      )}
    </header>
  )
}
