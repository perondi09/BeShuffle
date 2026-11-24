import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router'
import { AppLayout } from './components/layout/AppLayout'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ProfilePage } from './pages/ProfilePage'
import { RegisterPage } from './pages/RegisterPage'
import { UserProfilePage } from './pages/UserProfilePage'

// O painel de admin só abre para poucas contas: fica fora do pacote que todo mundo baixa.
const AdminPage = lazy(() => import('./pages/AdminPage').then((modulo) => ({ default: modulo.AdminPage })))

/** Rotas do site. Fica separado do roteador para os testes usarem um MemoryRouter. */
export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="entrar" element={<LoginPage />} />
        <Route path="cadastro" element={<RegisterPage />} />
        <Route path="perfil" element={<ProfilePage />} />
        <Route path="u/:handle" element={<UserProfilePage />} />
        <Route
          path="admin"
          element={
            <Suspense fallback={<p role="status">Carregando…</p>}>
              <AdminPage />
            </Suspense>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
