import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router'
import { authErrorMessage, login } from '../../api/auth'
import { Button, TextField } from '../../components/ui'
import { useSession } from '../session/session'
import styles from './AuthForm.module.css'
import { type FieldErrors, hasErrors, type LoginValues, validateLogin } from './validation'

/** Formulário de login: valida no navegador e envia para a API. */
export function LoginForm() {
  const navigate = useNavigate()
  const { refresh } = useSession()
  const [values, setValues] = useState<LoginValues>({ email: '', password: '' })
  const [errors, setErrors] = useState<FieldErrors<LoginValues>>({})
  const [feedback, setFeedback] = useState<string | null>(null)
  const [sending, setSending] = useState(false)

  const update = (field: keyof LoginValues) => (event: { target: { value: string } }) =>
    setValues((current) => ({ ...current, [field]: event.target.value }))

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const found = validateLogin(values)
    setErrors(found)
    setFeedback(null)
    if (hasErrors(found)) return

    setSending(true)
    try {
      await login({ email: values.email.trim(), password: values.password })
      await refresh()
      navigate('/', { replace: true })
    } catch (error) {
      setFeedback(authErrorMessage(error, 'login'))
    } finally {
      setSending(false)
    }
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <TextField
        label="E-mail"
        type="email"
        autoComplete="email"
        value={values.email}
        onChange={update('email')}
        error={errors.email}
      />
      <TextField
        label="Senha"
        type="password"
        autoComplete="current-password"
        value={values.password}
        onChange={update('password')}
        error={errors.password}
      />
      {feedback ? (
        <p role="alert" className={styles.feedback}>
          {feedback}
        </p>
      ) : null}
      <Button type="submit" variant="tomate" size="g" display block disabled={sending}>
        {sending ? 'Entrando…' : 'Entrar'}
      </Button>
    </form>
  )
}
