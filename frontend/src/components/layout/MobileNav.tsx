import { NavLink } from 'react-router'
import styles from './MobileNav.module.css'

/** Navegação inferior, só no celular. */
export function MobileNav() {
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
      <NavLink to="/entrar" className={styles.link}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" aria-hidden="true">
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
        </svg>
        Perfil
      </NavLink>
    </nav>
  )
}
