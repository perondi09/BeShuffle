import { AuthLayout } from '../features/auth/AuthLayout'
import { LoginForm } from '../features/auth/LoginForm'

export function LoginPage() {
  return (
    <AuthLayout
      sticker="Bem-vindo de volta"
      title="Entrar"
      subtitle="Entre para sortear quantas vezes quiser, ver os sorteados do dia e dar nota aos discos."
      pitch="A jukebox guardou seu lugar."
      footer={{ text: 'Ainda não tem conta?', linkLabel: 'Criar conta', to: '/cadastro' }}
    >
      <LoginForm />
    </AuthLayout>
  )
}
