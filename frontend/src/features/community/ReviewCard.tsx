import { type FormEvent, useEffect, useState } from 'react'
import { ApiError } from '../../api/http'
import { fetchTodayReviews, saveReview, type TodayReviews } from '../../api/reviews'
import { Button, Card, LockedOverlay } from '../../components/ui'
import { useSession } from '../session/session'
import styles from './community.module.css'
import { RatingDiscs } from './RatingDiscs'

const MAX_COMMENT = 500
const SHOWN_REVIEWS = 5

/** Nota e comentário do Álbum do Dia. Conta logada avalia; visitante vê o cartão trancado. */
export function ReviewCard() {
  const { user } = useSession()

  return <Card aria-labelledby="nota-titulo">{user ? <ReviewForm /> : <LockedReview />}</Card>
}

function ReviewForm() {
  const [data, setData] = useState<TodayReviews | null>(null)
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null)

  useEffect(() => {
    let active = true
    fetchTodayReviews()
      .then((loaded) => {
        if (!active) return
        setData(loaded)
        setRating(loaded.mine?.rating ?? 0)
        setComment(loaded.mine?.comment ?? '')
      })
      .catch(() => active && setMessage({ ok: false, text: 'Não deu para carregar as notas agora.' }))
    return () => {
      active = false
    }
  }, [])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (rating === 0) {
      setMessage({ ok: false, text: 'Escolha uma nota de 1 a 5.' })
      return
    }
    setSaving(true)
    setMessage(null)
    try {
      setData(await saveReview(rating, comment))
      setMessage({ ok: true, text: 'Nota publicada.' })
    } catch (error) {
      const text =
        error instanceof ApiError && error.status === 400 ? error.message : 'Não deu para publicar. Tente de novo.'
      setMessage({ ok: false, text })
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2 id="nota-titulo" className={`display ${styles.title}`}>
        Sua nota
      </h2>
      <p>
        {data ? (
          <>
            Para o Álbum do Dia, <strong>{data.albumName}</strong>.
          </>
        ) : (
          'Para o Álbum do Dia.'
        )}
      </p>
      <RatingDiscs value={rating} onChange={setRating} />
      <textarea
        className={styles.comment}
        placeholder="O que achou desse disco?"
        aria-label="Comentário"
        maxLength={MAX_COMMENT}
        value={comment}
        onChange={(event) => setComment(event.target.value)}
      />
      <div className={styles.row}>
        <small>{summary(data)}</small>
        <Button type="submit" variant="gema" disabled={saving || data === null}>
          {saving ? 'Publicando…' : data?.mine ? 'Atualizar nota' : 'Publicar'}
        </Button>
      </div>
      {message ? (
        <p role={message.ok ? 'status' : 'alert'} className={message.ok ? styles.ok : styles.error}>
          {message.text}
        </p>
      ) : null}
      {data && data.reviews.length > 0 ? (
        <ul className={styles.reviews} aria-label="Notas da comunidade">
          {data.reviews.slice(0, SHOWN_REVIEWS).map((review) => (
            <li key={`${review.author}-${review.updatedAt}`}>
              <strong>{review.author}</strong> <span className={styles.score}>{review.rating}/5</span>
              {review.comment ? <p>{review.comment}</p> : null}
            </li>
          ))}
        </ul>
      ) : null}
    </form>
  )
}

function summary(data: TodayReviews | null): string {
  if (!data) return 'Carregando notas…'
  if (data.count === 0 || data.average === null) return 'Seja a primeira pessoa a dar nota.'
  const notas = data.count === 1 ? '1 nota' : `${data.count} notas`
  return `Média ${data.average.toLocaleString('pt-BR')} de 5, ${notas}`
}

function LockedReview() {
  return (
    <>
      <div inert>
        <h2 id="nota-titulo" className={`display ${styles.title}`}>
          Sua nota
        </h2>
        <RatingDiscs value={0} onChange={() => {}} />
        <textarea className={styles.comment} placeholder="O que achou desse disco?" aria-label="Comentário" />
        <div className={styles.row}>
          <small>Seja a primeira pessoa a dar nota.</small>
          <Button variant="gema">Publicar</Button>
        </div>
      </div>
      <LockedOverlay
        message="Entre para dar nota e comentar o Álbum do Dia."
        action={
          <Button variant="rosa" to="/entrar">
            Entrar
          </Button>
        }
      />
    </>
  )
}
