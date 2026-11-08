import { AuthLayout } from '../features/auth/AuthLayout'
import { RegisterForm } from '../features/auth/RegisterForm'

export function RegisterPage() {
  return (
    <AuthLayout
      sticker="É grátis"
      title="Criar conta"
      subtitle="Fichas à vontade, histórico dos seus sorteios e espaço para dar nota e comentar."
      pitch="Sua próxima obsessão está a um sorteio."
      footer={{ text: 'Já tem conta?', linkLabel: 'Entrar', to: '/entrar' }}
    >
      <RegisterForm />
    </AuthLayout>
  )
}
