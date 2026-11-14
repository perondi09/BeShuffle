import { useEffect, useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { fetchMyDraws, type MyDraw } from '../api/draws'
import { fetchTodayReviews, type TodayReviews } from '../api/reviews'
import { Button, Card, Sticker } from '../components/ui'
import { useAllowance } from '../features/draw/allowance'
import { useSession } from '../features/session/session'
import styles from './ProfilePage.module.css'

const hora = (iso: string) =>
  new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })

/** Perfil da conta logada: quem é, fichas de hoje, o que sorteou hoje e a nota do Álbum do Dia. */
export function ProfilePage() {
  const { user, loading, signOut } = useSession()
  const navigate = useNavigate()

  if (loading) {
    return (
      <p role="status" className={styles.loading}>
        Carregando seu perfil…
      </p>
    )
  }
  if (!user) return <Navigate to="/entrar" replace />

  async function handleSignOut() {
    // Vai para a home antes: sem conta, esta página mandaria para /entrar.
    navigate('/', { replace: true })
    await signOut()
  }

  return (
    <div className={styles.page}>
      <Card size="palco" className={styles.hero} aria-labelledby="perfil-titulo">
        <Sticker color="rosa">Seu perfil</Sticker>
        <h1 id="perfil-titulo" className={`display ${styles.name}`}>
          {user.displayName}
        </h1>
        <p className={styles.email}>{user.email}</p>
        <Tokens />
        <div className={styles.actions}>
          <Button variant="tomate" display to="/">
            Ir sortear
          </Button>
          <Button variant="papel" onClick={handleSignOut}>
            Sair da conta
          </Button>
        </div>
      </Card>
      <div className={styles.duo}>
        <MyDrawsCard />
        <MyReviewCard />
      </div>
    </div>
  )
}

function Tokens() {
  const { allowance } = useAllowance()
  if (!allowance) return null
  return (
    <p className={styles.tokens}>
      {allowance.remaining === 0
        ? 'Suas fichas de hoje acabaram. Elas voltam à meia-noite.'
        : `Você ainda tem ${allowance.remaining} de ${allowance.dailyLimit} fichas hoje.`}
    </p>
  )
}

function MyDrawsCard() {
  const [draws, setDraws] = useState<MyDraw[] | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    fetchMyDraws()
      .then((list) => active && setDraws(list))
      .catch(() => active && setFailed(true))
    return () => {
      active = false
    }
  }, [])

  return (
    <Card aria-labelledby="meus-sorteios-titulo">
      <h2 id="meus-sorteios-titulo" className={`display ${styles.cardTitle}`}>
        Seus sorteios de hoje
      </h2>
      {failed ? (
        <p role="alert">Não deu para carregar seus sorteios agora.</p>
      ) : draws === null ? (
        <p role="status">Carregando…</p>
      ) : draws.length === 0 ? (
        <p>Você ainda não sorteou nada hoje.</p>
      ) : (
        <ul className={styles.list}>
          {draws.map((draw) => (
            <li key={`${draw.id}-${draw.drawnAt}`} className={styles.item}>
              <span className={styles.cover}>
                {draw.imageUrl ? <img src={draw.imageUrl} alt="" loading="lazy" /> : null}
              </span>
              <span className={styles.itemText}>
                <a href={`https://open.spotify.com/album/${draw.id}`} target="_blank" rel="noreferrer">
                  {draw.name}
                </a>
                <small>
                  {draw.artist ? `${draw.artist}, ` : ''}às {hora(draw.drawnAt)}
                </small>
              </span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}

function MyReviewCard() {
  const [data, setData] = useState<TodayReviews | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    fetchTodayReviews()
      .then((loaded) => active && setData(loaded))
      .catch(() => active && setFailed(true))
    return () => {
      active = false
    }
  }, [])

  return (
    <Card aria-labelledby="minha-nota-titulo">
      <h2 id="minha-nota-titulo" className={`display ${styles.cardTitle}`}>
        Sua nota de hoje
      </h2>
      {failed ? (
        <p role="alert">Não deu para carregar sua nota agora.</p>
      ) : data === null ? (
        <p role="status">Carregando…</p>
      ) : data.mine ? (
        <>
          <p>
            Você deu <strong>{data.mine.rating} de 5</strong> para <strong>{data.albumName}</strong>.
          </p>
          {data.mine.comment ? <blockquote className={styles.quote}>{data.mine.comment}</blockquote> : null}
          <Button variant="gema" to="/">
            Mudar a nota
          </Button>
        </>
      ) : (
        <>
          <p>
            Você ainda não deu nota para o Álbum do Dia, <strong>{data.albumName}</strong>.
          </p>
          <Button variant="gema" to="/">
            Dar nota
          </Button>
        </>
      )}
    </Card>
  )
}
