import { type FormEvent, type KeyboardEvent, useEffect, useId, useRef, useState } from 'react'
import type { UsuarioAdmin } from '../../api/admin'
import { Button, Sticker, TextField } from '../../components/ui'
import styles from './admin.module.css'
import { mensagemDeErro } from './format'

const MIN_MOTIVO = 3
const MAX_MOTIVO = 300

interface BanDialogProps {
  usuario: UsuarioAdmin
  onCancelar: () => void
  /** Quem chama banir de verdade; se lançar erro, a janela mostra a mensagem e continua aberta. */
  onConfirmar: (motivo: string) => Promise<void>
}

/** Janela de confirmação do banimento: pede o motivo (que fica no registro) e prende o foco enquanto está aberta. */
export function BanDialog({ usuario, onCancelar, onConfirmar }: BanDialogProps) {
  const [motivo, setMotivo] = useState('')
  const [erroCampo, setErroCampo] = useState<string | undefined>()
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const titulo = useId()
  const caixa = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const anterior = document.activeElement as HTMLElement | null
    caixa.current?.querySelector('input')?.focus()
    return () => anterior?.focus()
  }, [])

  function aoTeclar(evento: KeyboardEvent<HTMLDivElement>) {
    if (evento.key === 'Escape') {
      onCancelar()
      return
    }
    if (evento.key !== 'Tab') return
    const focaveis = caixa.current?.querySelectorAll<HTMLElement>('input, button:not(:disabled)')
    if (!focaveis || focaveis.length === 0) return
    const primeiro = focaveis[0]
    const ultimo = focaveis[focaveis.length - 1]
    if (evento.shiftKey && document.activeElement === primeiro) {
      evento.preventDefault()
      ultimo.focus()
    } else if (!evento.shiftKey && document.activeElement === ultimo) {
      evento.preventDefault()
      primeiro.focus()
    }
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const texto = motivo.trim()
    if (texto.length < MIN_MOTIVO) {
      setErroCampo(`Conte o motivo em pelo menos ${MIN_MOTIVO} letras.`)
      return
    }
    setErroCampo(undefined)
    setErro(null)
    setEnviando(true)
    try {
      await onConfirmar(texto)
    } catch (e) {
      setErro(mensagemDeErro(e, 'Não deu para banir agora. Tente de novo em instantes.'))
      setEnviando(false)
    }
  }

  return (
    <div className={styles.fundoJanela}>
      <div
        ref={caixa}
        className={styles.janela}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titulo}
        onKeyDown={aoTeclar}
      >
        <Sticker color="rosa">Ação de admin</Sticker>
        <h2 id={titulo} className="display">
          Banir {usuario.displayName}?
        </h2>
        <p>
          A conta sai na hora de todos os aparelhos e não consegue mais entrar. Dá para desbanir depois.
        </p>
        <form onSubmit={enviar} noValidate className={styles.secao}>
          <TextField
            label="Motivo (fica no registro)"
            value={motivo}
            maxLength={MAX_MOTIVO}
            error={erroCampo}
            onChange={(e) => setMotivo(e.target.value)}
          />
          {erro ? (
            <p role="alert" className={styles.erro}>
              {erro}
            </p>
          ) : null}
          <div className={styles.acoes}>
            <Button onClick={onCancelar}>Cancelar</Button>
            <Button type="submit" variant="tomate" disabled={enviando}>
              {enviando ? 'Banindo…' : 'Banir conta'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
