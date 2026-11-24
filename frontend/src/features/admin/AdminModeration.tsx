import { useEffect, useState } from 'react'
import { type ComentarioAdmin, type Pagina, fetchComentarios, removerComentario } from '../../api/admin'
import { Button, Card } from '../../components/ui'
import { cx } from '../../lib/cx'
import { formatRating } from '../../lib/rating'
import styles from './admin.module.css'
import { dataCurta, mensagemDeErro, nomeDaArea, quando } from './format'
import { Paginacao } from './Paginacao'

const TAMANHO = 20

/** Aba "Moderação": comentários de nota do mais novo para o mais antigo; remover apaga só o texto. */
export function AdminModeration() {
  const [pagina, setPagina] = useState(0)
  const [dados, setDados] = useState<Pagina<ComentarioAdmin> | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [recarregar, setRecarregar] = useState(0)
  const [confirmando, setConfirmando] = useState<number | null>(null)
  const [removendo, setRemovendo] = useState<number | null>(null)

  useEffect(() => {
    let ativo = true
    fetchComentarios(pagina, TAMANHO)
      .then((resultado) => {
        if (!ativo) return
        setDados(resultado)
        setErro(null)
      })
      .catch((e: unknown) => ativo && setErro(mensagemDeErro(e, 'Não deu para carregar os comentários agora.')))
    return () => {
      ativo = false
    }
  }, [pagina, recarregar])

  async function remover(comentario: ComentarioAdmin) {
    setRemovendo(comentario.reviewId)
    setAviso(null)
    try {
      await removerComentario(comentario.reviewId)
      setAviso(`Comentário de ${comentario.displayName} removido. A nota continua valendo.`)
      setConfirmando(null)
      setRecarregar((n) => n + 1)
    } catch (e) {
      setErro(mensagemDeErro(e, 'Não deu para remover o comentário agora.'))
    } finally {
      setRemovendo(null)
    }
  }

  return (
    <div className={styles.secao}>
      <Card aria-labelledby="admin-moderacao">
        <h2 id="admin-moderacao" className={`display ${styles.tituloCard}`}>
          Comentários
        </h2>
        <p>Do mais novo para o mais antigo. Remover apaga só o texto; a nota continua valendo.</p>
      </Card>

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
      {!dados && !erro ? <p role="status">Carregando os comentários…</p> : null}
      {dados?.items.length === 0 ? <p className={styles.vazio}>Nenhum comentário por enquanto.</p> : null}

      {dados && dados.items.length > 0 ? (
        <>
          <ul className={styles.comentarios}>
            {dados.items.map((comentario) => (
              <li key={comentario.reviewId} className={styles.comentario}>
                <div className={styles.comentarioCorpo}>
                  <span>
                    <span className={cx(styles.etiqueta, comentario.area === 'musica' ? styles.etiquetaMusica : styles.etiquetaFilmes)}>
                      {nomeDaArea(comentario.area)}
                    </span>{' '}
                    <strong>{comentario.displayName}</strong> · nota {formatRating(comentario.rating)} de 5
                  </span>
                  <p className={styles.comentarioTexto}>{comentario.comment}</p>
                  <small className={styles.comentarioMeta}>
                    {comentario.email} · {comentario.albumName} ({dataCurta(comentario.albumDate)}) · {quando(comentario.updatedAt)}
                  </small>
                </div>
                <div className={styles.acoes}>
                  {confirmando === comentario.reviewId ? (
                    <>
                      <span>Remover esse comentário?</span>
                      <Button
                        variant="tomate"
                        aria-label={`Confirmar a remoção do comentário de ${comentario.displayName}`}
                        disabled={removendo === comentario.reviewId}
                        onClick={() => remover(comentario)}
                      >
                        {removendo === comentario.reviewId ? 'Removendo…' : 'Remover'}
                      </Button>
                      <Button onClick={() => setConfirmando(null)}>Cancelar</Button>
                    </>
                  ) : (
                    <Button
                      aria-label={`Remover o comentário de ${comentario.displayName}`}
                      onClick={() => setConfirmando(comentario.reviewId)}
                    >
                      Remover comentário
                    </Button>
                  )}
                </div>
              </li>
            ))}
          </ul>
          <Paginacao page={dados.page} size={dados.size} total={dados.total} onChange={setPagina} />
        </>
      ) : null}
    </div>
  )
}
