import { type FormEvent, useState } from 'react'
import { updateProfile } from '../../api/auth'
import { ApiError } from '../../api/http'
import { AVATAR_COLORS, Avatar, Button, Card, TextField, avatarColor } from '../../components/ui'
import { useSession } from '../session/session'
import styles from './profile.module.css'

/** As mesmas regras do backend (services/Nick e AuthService): o servidor decide, isto só avisa mais cedo. */
const NICK = /^[a-z0-9_]{3,20}$/
const MIN_NAME = 2
const MAX_NAME = 60

const COLOR_NAMES: Record<(typeof AVATAR_COLORS)[number], string> = {
  rosa: 'Rosa',
  menta: 'Menta',
  gema: 'Gema',
  lilas: 'Lilás',
}

/** Editar o próprio perfil: como quer ser chamado, o nick (@nick, único) e a cor do avatar. */
export function EditProfileCard() {
  const { user, refresh } = useSession()
  const [name, setName] = useState(user?.displayName ?? '')
  const [handle, setHandle] = useState(user?.handle ?? '')
  const [avatar, setAvatar] = useState(avatarColor(user?.avatar))
  const [errors, setErrors] = useState<{ name?: string; handle?: string }>({})
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const cleanName = name.trim()
    const nick = handle.trim().toLowerCase()
    const found: { name?: string; handle?: string } = {}
    if (cleanName.length < MIN_NAME || cleanName.length > MAX_NAME) {
      found.name = `O nome precisa ter de ${MIN_NAME} a ${MAX_NAME} caracteres.`
    }
    if (!NICK.test(nick)) {
      found.handle = 'O nick precisa ter de 3 a 20 letras minúsculas, números ou _.'
    }
    setErrors(found)
    setMessage(null)
    if (found.name || found.handle) return

    setSaving(true)
    try {
      await updateProfile({ displayName: cleanName, handle: nick, avatar })
      await refresh()
      setName(cleanName)
      setHandle(nick)
      setMessage({ ok: true, text: 'Perfil salvo.' })
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setErrors({ handle: 'Esse nick já está em uso.' })
      } else {
        const text =
          error instanceof ApiError && error.status === 400 ? error.message : 'Não deu para salvar. Tente de novo.'
        setMessage({ ok: false, text })
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card aria-labelledby="editar-perfil-titulo">
      <h2 id="editar-perfil-titulo" className={`display ${styles.title}`}>
        Editar perfil
      </h2>
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <TextField
          label="Como quer ser chamado"
          value={name}
          maxLength={MAX_NAME}
          error={errors.name}
          onChange={(event) => setName(event.target.value)}
        />
        <TextField
          label="Nick"
          value={handle}
          maxLength={20}
          hint="É o seu endereço no site: letras minúsculas, números ou _."
          error={errors.handle}
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          onChange={(event) => setHandle(event.target.value)}
        />
        <div className={styles.field}>
          <span className={styles.label} id="avatar-rotulo">
            Avatar
          </span>
          <div className={styles.avatars} role="radiogroup" aria-labelledby="avatar-rotulo">
            {AVATAR_COLORS.map((color) => (
              <button
                key={color}
                type="button"
                role="radio"
                aria-checked={color === avatar}
                aria-label={`Avatar ${COLOR_NAMES[color]}`}
                className={styles.avatarOption}
                onClick={() => setAvatar(color)}
              >
                <Avatar name={name || user?.displayName || '?'} color={color} />
              </button>
            ))}
          </div>
        </div>
        <div className={styles.actions}>
          <Button type="submit" variant="gema" disabled={saving}>
            {saving ? 'Salvando…' : 'Salvar perfil'}
          </Button>
        </div>
        {message ? (
          <p role={message.ok ? 'status' : 'alert'} className={message.ok ? styles.ok : styles.error}>
            {message.text}
          </p>
        ) : null}
      </form>
    </Card>
  )
}
