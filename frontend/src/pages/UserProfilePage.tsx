import { useEffect, useState } from 'react'
import { Navigate, useParams } from 'react-router'
import { ApiError } from '../api/http'
import { type DiaryEntry, type PublicProfile, fetchDiary, fetchProfile } from '../api/profiles'
import { Avatar, Button, Card, Sticker } from '../components/ui'
import { RatingValue } from '../features/community/RatingValue'
import { useSession } from '../features/session/session'
import { diaPorExtenso } from '../lib/date'
import { formatRating } from '../lib/rating'
import { NotFoundPage } from './NotFoundPage'
import styles from './UserProfilePage.module.css'

const PAGE_SIZE = 12

/**
 * Perfil de uma conta com o diário das notas que ela deu aos Álbuns do Dia, do mais recente para o mais antigo.
 * Só para conta logada, como o resto da comunidade: visitante vai para o login.
 */
export function UserProfilePage() {
  const { handle = '' } = useParams()
  const { user, loading } = useSession()

  if (loading) {
    return (
      <p role="status" className={styles.loading}>
        Carregando…
      </p>
    )
  }
  if (!user) return <Navigate to="/entrar" replace />

  return <Profile key={handle} handle={handle} mine={user.handle === handle.toLowerCase()} />
}

function Profile({ handle, mine }: { handle: string; mine: boolean }) {
  const [profile, setProfile] = useState<PublicProfile | null>(null)
  const [entries, setEntries] = useState<DiaryEntry[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [failure, setFailure] = useState<'notFound' | 'failed' | null>(null)
  const [loadingMore, setLoadingMore] = useState(false)
  const [moreFailed, setMoreFailed] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([fetchProfile(handle), fetchDiary(handle, 0, PAGE_SIZE)])
      .then(([loadedProfile, diary]) => {
        if (!active) return
        setProfile(loadedProfile)
        setEntries(diary.items)
        setTotal(diary.total)
      })
      .catch((error: unknown) => active && setFailure(error instanceof ApiError && error.status === 404 ? 'notFound' : 'failed'))
    return () => {
      active = false
    }
  }, [handle])

  async function loadMore() {
    setLoadingMore(true)
    setMoreFailed(false)
    try {
      const diary = await fetchDiary(handle, page + 1, PAGE_SIZE)
      setEntries((current) => [...current, ...diary.items])
      setTotal(diary.total)
      setPage(page + 1)
    } catch {
      setMoreFailed(true)
    } finally {
      setLoadingMore(false)
    }
  }

  if (failure === 'notFound') return <NotFoundPage />
  if (failure === 'failed') return <p role="alert">Não deu para carregar esse perfil agora.</p>
  if (!profile) {
    return (
      <p role="status" className={styles.loading}>
        Carregando o perfil…
      </p>
    )
  }

  const since = new Date(profile.joinedAt).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })

  return (
    <div className={styles.page}>
      <Card size="palco" className={styles.hero} aria-labelledby="perfil-publico-titulo">
        <Avatar name={profile.displayName} color={profile.avatar} size="g" />
        <Sticker color="rosa">{mine ? 'Seu perfil' : 'Perfil'}</Sticker>
        <h1 id="perfil-publico-titulo" className={`display ${styles.name}`}>
          {profile.displayName}
        </h1>
        <p className={styles.handle}>@{profile.handle}</p>
        <ul className={styles.stats} aria-label="Resumo do perfil">
          <li>
            <strong>{profile.reviewCount}</strong> {profile.reviewCount === 1 ? 'nota' : 'notas'}
          </li>
          {profile.average !== null ? (
            <li>
              média <strong>{formatRating(profile.average)}</strong> de 5
            </li>
          ) : null}
          <li>no BeShuffle desde {since}</li>
        </ul>
        {mine ? (
          <Button variant="gema" to="/perfil">
            Editar perfil
          </Button>
        ) : null}
      </Card>

      <Card aria-labelledby="diario-titulo">
        <h2 id="diario-titulo" className={`display ${styles.cardTitle}`}>
          Diário de notas
        </h2>
        {entries.length === 0 ? (
          <>
            <p>
              {mine
                ? 'Você ainda não avaliou nenhum Álbum do Dia.'
                : `${profile.displayName} ainda não avaliou nenhum Álbum do Dia.`}
            </p>
            {mine ? (
              <Button variant="tomate" to="/">
                Avaliar o Álbum do Dia
              </Button>
            ) : null}
          </>
        ) : (
          <ul className={styles.grid}>
            {entries.map((entry) => (
              <li key={`${entry.albumId}-${entry.date}`} className={styles.entry}>
                <div className={styles.cover}>
                  {entry.albumImageUrl ? (
                    <img src={entry.albumImageUrl} alt={`Capa de ${entry.albumName}`} loading="lazy" />
                  ) : null}
                </div>
                <div className={styles.entryText}>
                  <strong className={styles.album}>{entry.albumName}</strong>
                  {entry.albumArtist ? <span>{entry.albumArtist}</span> : null}
                  <time dateTime={entry.date} className={styles.date}>
                    {diaPorExtenso(entry.date)}
                  </time>
                  <RatingValue value={entry.rating} />
                  {entry.comment ? <p className={styles.comment}>{entry.comment}</p> : null}
                </div>
              </li>
            ))}
          </ul>
        )}
        {entries.length < total ? (
          <div className={styles.more}>
            <Button onClick={loadMore} disabled={loadingMore}>
              {loadingMore ? 'Carregando…' : 'Ver mais notas'}
            </Button>
            {moreFailed ? <p role="alert">Não deu para carregar mais notas agora.</p> : null}
          </div>
        ) : null}
      </Card>
    </div>
  )
}
