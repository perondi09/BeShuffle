export type FieldErrors<T> = Partial<Record<keyof T, string>>

export interface LoginValues {
  email: string
  password: string
}

export interface RegisterValues {
  name: string
  email: string
  password: string
  confirm: string
}

export const MIN_PASSWORD = 8
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function emailError(email: string): string | undefined {
  if (!email.trim()) return 'Informe seu e-mail.'
  if (!EMAIL.test(email.trim())) return 'Esse e-mail não parece válido.'
  return undefined
}

export function validateLogin(values: LoginValues): FieldErrors<LoginValues> {
  const errors: FieldErrors<LoginValues> = {}
  const email = emailError(values.email)
  if (email) errors.email = email
  if (!values.password) errors.password = 'Informe sua senha.'
  return errors
}

export function validateRegister(values: RegisterValues): FieldErrors<RegisterValues> {
  const errors: FieldErrors<RegisterValues> = {}
  if (values.name.trim().length < 2) errors.name = 'Diga como quer ser chamado (2 letras ou mais).'
  const email = emailError(values.email)
  if (email) errors.email = email
  if (values.password.length < MIN_PASSWORD) errors.password = `A senha precisa de ${MIN_PASSWORD} caracteres ou mais.`
  if (values.confirm !== values.password) errors.confirm = 'As senhas não são iguais.'
  return errors
}

export const hasErrors = (errors: object) => Object.keys(errors).length > 0
