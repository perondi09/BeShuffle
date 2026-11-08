import { Outlet } from 'react-router'
import { DailyTokenProvider } from '../../features/draw/DailyTokenProvider'
import styles from './AppLayout.module.css'
import { Header } from './Header'
import { MobileNav } from './MobileNav'

/** Moldura comum das telas: topo, conteúdo da rota e navegação do celular. */
export function AppLayout() {
  return (
    <DailyTokenProvider>
      <Header />
      <main className={styles.main}>
        <Outlet />
      </main>
      <MobileNav />
    </DailyTokenProvider>
  )
}
