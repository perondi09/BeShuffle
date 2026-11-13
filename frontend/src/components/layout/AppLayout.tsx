import { Outlet } from 'react-router'
import { AllowanceProvider } from '../../features/draw/AllowanceProvider'
import { SessionProvider } from '../../features/session/SessionProvider'
import styles from './AppLayout.module.css'
import { Header } from './Header'
import { MobileNav } from './MobileNav'

/** Moldura comum das telas: topo, conteúdo da rota e navegação do celular. */
export function AppLayout() {
  return (
    <SessionProvider>
      <AllowanceProvider>
        <Header />
        <main className={styles.main}>
          <Outlet />
        </main>
        <MobileNav />
      </AllowanceProvider>
    </SessionProvider>
  )
}
