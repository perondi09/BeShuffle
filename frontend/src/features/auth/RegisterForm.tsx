import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router'
import { authErrorMessage, register } from '../../api/auth'
import { Button, TextField } from '../../components/ui'
import { useSession } from '../session/session'
import styles from './AuthForm.module.css'
import { type FieldErrors, hasErrors, MIN_PASSWORD, type RegisterValues, validateRegister } from './validation'

const EMPTY: RegisterValues = { name: '', email: '', password: '', confirm: '' }

/** Formulário de cadastro: valida no navegador e envia para a API. */
export function RegisterForm() {
  const navigate = useNavigate()
  const { refresh } = useSession()
  const [values, setValues] = useState<RegisterValues>(EMPTY)
  const [errors, setErrors] = useState<FieldErrors<RegisterValues>>({})
  const [feedback, setFeedback] = useState<string | null>(null)
  const [sending, setSending] = useState(false)

  const update = (field: keyof RegisterValues) => (event: { target: { value: string } }) =>
    setValues((current) => ({ ...current, [field]: event.target.value }))

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const found = validateRegister(values)
    setErrors(found)
    setFeedback(null)
    if (hasErrors(found)) return

    setSending(true)
    try {
      await register({ name: values.name.trim(), email: values.email.trim(), password: values.password })
      await refresh()
      navigate('/', { replace: true })
    } catch (error) {
      setFeedback(authErrorMessage(error, 'cadastro'))
    } finally {
      setSending(false)
    }
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <TextField
        label="Como quer ser chamado"
        autoComplete="nickname"
        value={values.name}
        onChange={update('name')}
        error={errors.name}
      />
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
        autoComplete="new-password"
        hint={`Pelo menos ${MIN_PASSWORD} caracteres.`}
        value={values.password}
        onChange={update('password')}
        error={errors.password}
      />
      <TextField
        label="Repita a senha"
        type="password"
        autoComplete="new-password"
        value={values.confirm}
        onChange={update('confirm')}
        error={errors.confirm}
      />
      {feedback ? (
        <p role="alert" className={styles.feedback}>
          {feedback}
        </p>
      ) : null}
      <Button type="submit" variant="tomate" size="g" display block disabled={sending}>
        {sending ? 'Criando conta…' : 'Criar conta'}
      </Button>
    </form>
  )
}
