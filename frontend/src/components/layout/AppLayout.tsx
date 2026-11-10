import { Outlet } from 'react-router'
import { DailyTokenProvider } from '../../features/draw/DailyTokenProvider'
import { SessionProvider } from '../../features/session/SessionProvider'
import styles from './AppLayout.module.css'
import { Header } from './Header'
import { MobileNav } from './MobileNav'

/** Moldura comum das telas: topo, conteúdo da rota e navegação do celular. */
export function AppLayout() {
  return (
    <SessionProvider>
      <DailyTokenProvider>
        <Header />
        <main className={styles.main}>
          <Outlet />
        </main>
        <MobileNav />
      </DailyTokenProvider>
    </SessionProvider>
  )
}
