import { useEffect } from 'react'
import { Navigate, useSearchParams } from 'react-router'
import { Sticker } from '../components/ui'
import { AdminModeration } from '../features/admin/AdminModeration'
import { AdminPanel } from '../features/admin/AdminPanel'
import { AdminUsers } from '../features/admin/AdminUsers'
import { Segmentado } from '../features/admin/Segmentado'
import { useSession } from '../features/session/session'
import styles from './AdminPage.module.css'
import { NotFoundPage } from './NotFoundPage'

const ABAS = [
  { id: 'painel', label: 'Painel' },
  { id: 'usuarios', label: 'Usuários' },
  { id: 'moderacao', label: 'Moderação' },
] as const

type Aba = (typeof ABAS)[number]['id']

const ehAba = (valor: string | null): valor is Aba => ABAS.some((aba) => aba.id === valor)

/**
 * Painel de admin do site todo (música agora, filmes depois). Só conta admin vê: visitante vai para o login e conta
 * comum recebe a página 404, para a rota nem aparecer. A proteção de verdade está na API (/api/admin/** dá 403).
 */
export function AdminPage() {
  const { user, loading } = useSession()
  const [params, setParams] = useSearchParams()
  const permitido = user?.admin === true
  const pedida = params.get('aba')
  const aba: Aba = ehAba(pedida) ? pedida : 'painel'

  // Troca o lilás por um fundo neutro enquanto o painel está aberto.
  useEffect(() => {
    if (!permitido) return
    document.body.dataset.tema = 'admin'
    return () => {
      delete document.body.dataset.tema
    }
  }, [permitido])

  if (loading) {
    return (
      <p role="status" className={styles.loading}>
        Carregando…
      </p>
    )
  }
  if (!user) return <Navigate to="/entrar" replace />
  if (!permitido) return <NotFoundPage />

  return (
    <div className={styles.page}>
      <header className={styles.topo}>
        <div className={styles.titulos}>
          <Sticker color="gema">Só para admins</Sticker>
          <h1 className={`display ${styles.titulo}`}>Painel do admin</h1>
        </div>
        <Segmentado
          label="Seções do admin"
          value={aba}
          options={ABAS}
          onChange={(id) => setParams(id === 'painel' ? {} : { aba: id })}
        />
      </header>

      {aba === 'painel' ? <AdminPanel /> : null}
      {aba === 'usuarios' ? <AdminUsers /> : null}
      {aba === 'moderacao' ? <AdminModeration /> : null}
    </div>
  )
}
