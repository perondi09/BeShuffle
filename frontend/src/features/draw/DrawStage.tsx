import { useState } from 'react'
import { Link } from 'react-router'
import { coverUrl, spotifyUrl } from '../../api/albums'
import { Button, Card } from '../../components/ui'
import { useSession } from '../session/session'
import { AlbumDetails } from './AlbumDetails'
import { useAllowance } from './allowance'
import styles from './DrawStage.module.css'
import { StylePicker } from './StylePicker'
import { readSavedStyles } from './styleStorage'
import { Turntable } from './Turntable'
import { type DrawProblem, useAlbumDraw } from './useAlbumDraw'

/** Palco da home: o álbum em destaque, os estilos (conta logada), o botão de sortear e o link do Spotify. */
export function DrawStage() {
  const { state, drawing, problem, draw } = useAlbumDraw()
  const { allowance } = useAllowance()
  const { user } = useSession()
  const [styleIds, setStyleIds] = useState(readSavedStyles)
  const album = state.status === 'ready' ? state.album : undefined
  // Sem saldo carregado (API fora), o botão fica ativo e o próprio sorteio mostra o erro.
  const hasTokens = allowance ? allowance.remaining > 0 : true

  function handleDraw() {
    if (!hasTokens) return
    void draw(user ? styleIds : [])
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

        {user ? <StylePicker selected={styleIds} onChange={setStyleIds} disabled={drawing} /> : null}

        <div className={styles.actions}>
          <Button variant="tomate" size="g" display onClick={handleDraw} disabled={drawing || !hasTokens}>
            {drawing ? 'Sorteando…' : hasTokens ? 'Sortear álbum' : 'Volte amanhã'}
          </Button>
          {album ? (
            <Button variant="menta" size="g" href={spotifyUrl(album)} target="_blank" rel="noreferrer">
              Ouvir no Spotify
            </Button>
          ) : null}
        </div>

        <Notice problem={problem} loggedIn={Boolean(user)} remaining={allowance?.remaining} />
      </div>
    </Card>
  )
}

interface NoticeProps {
  problem: DrawProblem
  loggedIn: boolean
  remaining?: number
}

/** Aviso abaixo dos botões: erro do sorteio, quantas fichas faltam ou o convite para criar conta. */
function Notice({ problem, loggedIn, remaining }: NoticeProps) {
  if (problem) {
    return (
      <p role="alert" className={styles.notice}>
        {problem.message}
      </p>
    )
  }
  if (loggedIn) {
    return (
      <p className={styles.notice}>
        {remaining === 0
          ? 'Suas 10 fichas de hoje acabaram. Elas voltam à meia-noite.'
          : 'Você tem 10 fichas por dia e pode escolher os estilos. Elas renovam à meia-noite.'}
      </p>
    )
  }
  if (remaining === 0) {
    return (
      <p className={styles.notice}>
        Sua ficha de hoje já foi. <Link to="/cadastro">Crie uma conta grátis</Link> para sortear até 10 álbuns por
        dia, escolher estilos, ver os sorteados do dia e dar nota.
      </p>
    )
  }
  return <p className={styles.notice}>Visitantes têm 1 ficha por dia. Ela renova à meia-noite.</p>
}
