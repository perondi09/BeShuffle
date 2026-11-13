import { useEffect, useState } from 'react'
import { type DrawnToday, fetchDrawnToday } from '../../api/draws'
import { Button, Card, LockedOverlay } from '../../components/ui'
import { useAllowance } from '../draw/allowance'
import { useSession } from '../session/session'
import styles from './community.module.css'

const PLACEHOLDERS = ['Disco 1', 'Disco 2', 'Disco 3', 'Disco 4']

/** Sorteados do dia por toda a comunidade. Conta logada vê; visitante vê a grade vazia atrás do cadeado. */
export function TodayCard() {
  const { user } = useSession()

  return (
    <Card id="hoje" aria-labelledby="hoje-titulo">
      {user ? <DrawnTodayList /> : <LockedToday />}
    </Card>
  )
}

function DrawnTodayList() {
  const { allowance } = useAllowance()
  const [albums, setAlbums] = useState<DrawnToday[] | null>(null)
  const [failed, setFailed] = useState(false)
  // Lê de novo depois de cada sorteio (o saldo muda), para o álbum que acabou de sair aparecer.
  const remaining = allowance?.remaining

  useEffect(() => {
    let active = true
    fetchDrawnToday()
      .then((list) => {
        if (!active) return
        setAlbums(list)
        setFailed(false)
      })
      .catch(() => active && setFailed(true))
    return () => {
      active = false
    }
  }, [remaining])

  return (
    <>
      <h2 id="hoje-titulo" className={`display ${styles.title}`}>
        Sorteados de hoje
      </h2>
      <p>O que todo mundo tirou na jukebox desde a meia-noite.</p>
      {failed ? (
        <p role="alert" className={styles.empty}>
          Não deu para carregar os sorteados agora.
        </p>
      ) : albums === null ? (
        <p role="status" className={styles.empty}>
          Carregando…
        </p>
      ) : albums.length === 0 ? (
        <p className={styles.empty}>Ninguém sorteou nada hoje ainda. Seja a primeira pessoa!</p>
      ) : (
        <ul className={styles.grid}>
          {albums.map((album) => (
            <li key={album.id}>
              <a
                className={styles.mini}
                href={`https://open.spotify.com/album/${album.id}`}
                target="_blank"
                rel="noreferrer"
                aria-label={`${album.name} no Spotify`}
              >
                {album.imageUrl ? <img src={album.imageUrl} alt="" loading="lazy" /> : null}
              </a>
              <p className={styles.miniName}>
                {album.name}
                <span>
                  {album.artist}
                  {album.count > 1 ? ` (${album.count}x)` : ''}
                </span>
              </p>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

function LockedToday() {
  return (
    <>
      <div inert>
        <h2 id="hoje-titulo" className={`display ${styles.title}`}>
          Sorteados de hoje
        </h2>
        <p>O que todo mundo tirou na jukebox desde a meia-noite.</p>
        <div className={styles.grid}>
          {PLACEHOLDERS.map((name) => (
            <div key={name}>
              <div className={styles.mini} />
              <p className={styles.miniName}>
                {name}
                <span>Artista</span>
              </p>
            </div>
          ))}
        </div>
      </div>
      <LockedOverlay
        message="Crie uma conta grátis para ver o que a galera sorteou hoje."
        action={
          <Button variant="rosa" to="/cadastro">
            Criar conta
          </Button>
        }
      />
    </>
  )
}
