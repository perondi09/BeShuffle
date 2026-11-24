import { NavLink } from 'react-router'
import { useSession } from '../../features/session/session'
import styles from './MobileNav.module.css'

const personIcon = (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" aria-hidden="true">
    <circle cx="12" cy="8" r="4" />
    <path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
  </svg>
)

/** Navegação inferior, só no celular. A última aba é Entrar (visitante) ou Perfil (conta logada). */
export function MobileNav() {
  const { user } = useSession()

  return (
    <nav className={styles.nav} aria-label="Principal">
      <NavLink to="/" end className={styles.link}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" aria-hidden="true">
          <circle cx="12" cy="12" r="9" />
          <circle cx="12" cy="12" r="2.5" />
        </svg>
        Sortear
      </NavLink>
      <a href="/#hoje" className={styles.link}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" aria-hidden="true">
          <rect x="3" y="3" width="7" height="7" rx="2" />
          <rect x="14" y="3" width="7" height="7" rx="2" />
          <rect x="3" y="14" width="7" height="7" rx="2" />
          <rect x="14" y="14" width="7" height="7" rx="2" />
        </svg>
        Hoje
      </a>
      {user?.admin ? (
        <NavLink to="/admin" className={styles.link}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" aria-hidden="true">
            <path d="M3 20h18" />
            <rect x="5" y="11" width="3.5" height="9" rx="1" />
            <rect x="10.25" y="5" width="3.5" height="15" rx="1" />
            <rect x="15.5" y="8" width="3.5" height="12" rx="1" />
          </svg>
          Admin
        </NavLink>
      ) : null}
      {user ? (
        <NavLink to="/perfil" className={styles.link}>
          {personIcon}
          Perfil
        </NavLink>
      ) : (
        <NavLink to="/entrar" className={styles.link}>
          {personIcon}
          Entrar
        </NavLink>
      )}
    </nav>
  )
}
