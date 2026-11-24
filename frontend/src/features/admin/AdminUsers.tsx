import { type FormEvent, useEffect, useState } from 'react'
import {
  type Pagina,
  type StatusConta,
  type UsuarioAdmin,
  banirUsuario,
  desbanirUsuario,
  fetchUsuarios,
} from '../../api/admin'
import { Button, Card, TextField } from '../../components/ui'
import { cx } from '../../lib/cx'
import styles from './admin.module.css'
import { BanDialog } from './BanDialog'
import { dataCurta, mensagemDeErro, numero } from './format'
import { Paginacao } from './Paginacao'
import { Segmentado } from './Segmentado'

const STATUS = [
  { id: 'todos', label: 'Todos' },
  { id: 'ativos', label: 'Ativos' },
  { id: 'banidos', label: 'Banidos' },
  { id: 'admins', label: 'Admins' },
] as const

const TAMANHO = 20

function SituacaoDaConta({ usuario }: { usuario: UsuarioAdmin }) {
  if (usuario.bannedAt) {
    return (
      <>
        <span className={cx(styles.etiqueta, styles.statusBanida)}>Banida</span>
        {usuario.banReason ? <small className={styles.motivo}>{usuario.banReason}</small> : null}
      </>
    )
  }
  if (usuario.role === 'ADMIN') return <span className={cx(styles.etiqueta, styles.statusAdmin)}>Admin</span>
  return <span className={cx(styles.etiqueta, styles.statusAtiva)}>Ativa</span>
}

/** Aba "Usuários": busca por nome ou e-mail, filtro por situação, banir e desbanir. */
export function AdminUsers() {
  const [texto, setTexto] = useState('')
  const [busca, setBusca] = useState('')
  const [status, setStatus] = useState<StatusConta>('todos')
  const [pagina, setPagina] = useState(0)
  const [dados, setDados] = useState<Pagina<UsuarioAdmin> | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [recarregar, setRecarregar] = useState(0)
  const [banindo, setBanindo] = useState<UsuarioAdmin | null>(null)

  useEffect(() => {
    let ativo = true
    fetchUsuarios({ q: busca, status, page: pagina, size: TAMANHO })
      .then((resultado) => {
        if (!ativo) return
        setDados(resultado)
        setErro(null)
      })
      .catch((e: unknown) => ativo && setErro(mensagemDeErro(e, 'Não deu para carregar as contas agora.')))
    return () => {
      ativo = false
    }
  }, [busca, status, pagina, recarregar])

  function buscar(evento: FormEvent) {
    evento.preventDefault()
    setBusca(texto.trim())
    setPagina(0)
  }

  function mudarStatus(novo: StatusConta) {
    setStatus(novo)
    setPagina(0)
  }

  async function desbanir(usuario: UsuarioAdmin) {
    setAviso(null)
    try {
      await desbanirUsuario(usuario.id)
      setAviso(`${usuario.displayName} voltou a ter acesso.`)
      setRecarregar((n) => n + 1)
    } catch (e) {
      setErro(mensagemDeErro(e, 'Não deu para desbanir agora.'))
    }
  }

  async function banir(motivo: string) {
    if (!banindo) return
    await banirUsuario(banindo.id, motivo)
    setAviso(`${banindo.displayName} foi banida e perdeu o acesso.`)
    setBanindo(null)
    setRecarregar((n) => n + 1)
  }

  return (
    <div className={styles.secao}>
      <div className={styles.barra}>
        <form className={styles.busca} onSubmit={buscar} role="search">
          <div className={styles.buscaCampo}>
            <TextField label="Buscar" placeholder="nome ou e-mail" value={texto} onChange={(e) => setTexto(e.target.value)} />
          </div>
          <Button type="submit" variant="gema">
            Buscar
          </Button>
        </form>
        <div className={styles.barraFim}>
          <Segmentado label="Situação" value={status} options={STATUS} onChange={mudarStatus} />
        </div>
      </div>

      {aviso ? (
        <p role="status" className={styles.aviso}>
          {aviso}
        </p>
      ) : null}
      {erro ? (
        <p role="alert" className={styles.erro}>
          {erro}
        </p>
      ) : null}
      {!dados && !erro ? <p role="status">Carregando as contas…</p> : null}

      {dados ? (
        <Card aria-label="Contas">
          {dados.items.length === 0 ? (
            <p className={styles.vazio}>Nenhuma conta com esses filtros.</p>
          ) : (
            <table className={styles.tabela}>
              <caption className="visually-hidden">Contas do BeShuffle</caption>
              <thead>
                <tr>
                  <th scope="col">Conta</th>
                  <th scope="col">Desde</th>
                  <th scope="col">Sorteios</th>
                  <th scope="col">Notas</th>
                  <th scope="col">Situação</th>
                  <th scope="col">
                    <span className="visually-hidden">Ação</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {dados.items.map((usuario) => (
                  <tr key={usuario.id}>
                    <td>
                      <div className={styles.conta}>
                        <span className={styles.inicial} aria-hidden="true">
                          {usuario.displayName.charAt(0).toUpperCase()}
                        </span>
                        <span className={styles.contaTexto}>
                          <strong>{usuario.displayName}</strong>
                          <small>{usuario.email}</small>
                        </span>
                      </div>
                    </td>
                    <td data-rotulo="Desde">{dataCurta(usuario.createdAt)}</td>
                    <td data-rotulo="Sorteios">{numero(usuario.draws)}</td>
                    <td data-rotulo="Notas">{numero(usuario.reviews)}</td>
                    <td data-rotulo="Situação">
                      <SituacaoDaConta usuario={usuario} />
                    </td>
                    <td>
                      {usuario.role === 'ADMIN' ? (
                        <span aria-label="Conta de admin não pode ser banida">—</span>
                      ) : usuario.bannedAt ? (
                        <Button variant="menta" aria-label={`Desbanir ${usuario.displayName}`} onClick={() => desbanir(usuario)}>
                          Desbanir
                        </Button>
                      ) : (
                        <Button variant="tomate" aria-label={`Banir ${usuario.displayName}`} onClick={() => setBanindo(usuario)}>
                          Banir
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <Paginacao page={dados.page} size={dados.size} total={dados.total} onChange={setPagina} />
        </Card>
      ) : null}

      {banindo ? <BanDialog usuario={banindo} onCancelar={() => setBanindo(null)} onConfirmar={banir} /> : null}
    </div>
  )
}
