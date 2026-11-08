import { Link } from 'react-router'
import { coverUrl, spotifyUrl } from '../../api/albums'
import { Button, Card } from '../../components/ui'
import { AlbumDetails } from './AlbumDetails'
import { useDailyToken } from './dailyToken'
import styles from './DrawStage.module.css'
import { Turntable } from './Turntable'
import { useAlbumDraw } from './useAlbumDraw'

/** Palco da home: o álbum em destaque, o botão de sortear e o link para o Spotify. */
export function DrawStage() {
  const { state, drawing, drawFailed, draw } = useAlbumDraw()
  const token = useDailyToken()
  const album = state.status === 'ready' ? state.album : undefined

  async function handleDraw() {
    if (!token.available) return
    if (await draw()) token.spend()
  }

  return (
    <Card size="palco" className={styles.stage} aria-label="Sorteio">
      <Turntable
        cover={album ? coverUrl(album) : undefined}
        title={album?.name}
        stashed={drawing || state.status === 'loading'}
        showDailySticker={state.status === 'ready' && state.source === 'daily'}
      />

      <div className={styles.info}>
        {state.status === 'loading' && (
          <p role="status" className={styles.message}>
            Colocando o disco do dia na vitrola…
          </p>
        )}
        {state.status === 'error' && (
          <p role="alert" className={styles.message}>
            Não deu para carregar o álbum do dia. Confira se a API está rodando e recarregue a página.
          </p>
        )}
        {state.status === 'ready' && <AlbumDetails album={state.album} source={state.source} />}

        <div className={styles.actions}>
          <Button
            variant="tomate"
            size="g"
            display
            onClick={handleDraw}
            disabled={drawing || !token.available}
          >
            {drawing ? 'Sorteando…' : token.available ? 'Sortear álbum' : 'Volte amanhã'}
          </Button>
          {album ? (
            <Button variant="menta" size="g" href={spotifyUrl(album)} target="_blank" rel="noreferrer">
              Ouvir no Spotify
            </Button>
          ) : null}
        </div>

        {drawFailed ? (
          <p role="alert" className={styles.notice}>
            O sorteio falhou e sua ficha continua com você. Tente de novo.
          </p>
        ) : token.available ? (
          <p className={styles.notice}>Visitantes têm 1 ficha por dia. Ela renova à meia-noite.</p>
        ) : (
          <p className={styles.notice}>
            Sua ficha de hoje já foi. <Link to="/cadastro">Crie uma conta</Link> para sortear quantas vezes quiser,
            ver os sorteados do dia e dar nota.
          </p>
        )}
      </div>
    </Card>
  )
}
