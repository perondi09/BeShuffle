import { type InputHTMLAttributes, useId } from 'react'
import styles from './TextField.module.css'

interface TextFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> {
  label: string
  /** Mensagem de erro do campo; quando existe, o campo fica marcado como inválido. */
  error?: string
  hint?: string
}

/** Campo de formulário com rótulo, dica e erro ligados por aria. */
export function TextField({ label, error, hint, ...input }: TextFieldProps) {
  const id = useId()
  const hintId = hint ? `${id}-dica` : undefined
  const errorId = error ? `${id}-erro` : undefined
  const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined

  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      <input
        id={id}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        {...input}
      />
      {hint ? (
        <small id={hintId} className={styles.hint}>
          {hint}
        </small>
      ) : null}
      {error ? (
        <small id={errorId} className={styles.error}>
          {error}
        </small>
      ) : null}
    </div>
  )
}
