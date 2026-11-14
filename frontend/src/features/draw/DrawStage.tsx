import { type KeyboardEvent, type ReactNode, useState } from 'react'
import { Link } from 'react-router'
import { coverUrl, spotifyUrl } from '../../api/albums'
import { Button, Card, Sticker } from '../../components/ui'
import { useSession } from '../session/session'
import { AlbumDetails } from './AlbumDetails'
import { useAllowance } from './allowance'
import styles from './DrawStage.module.css'
import { readTab, saveTab, type StageTab } from './lastDraw'
import { ServiceDown } from './ServiceDown'
import { StylePicker } from './StylePicker'
import { readSavedStyles } from './styleStorage'
import { Turntable } from './Turntable'
import { type DailyState, type DrawProblem, type LastDraw, useAlbumDraw } from './useAlbumDraw'

const TABS: { id: StageTab; label: string }[] = [
  { id: 'daily', label: 'Álbum do dia' },
  { id: 'draw', label: 'Sorteio' },
]

/** Palco da home em duas abas: o Álbum do Dia, fixo, e o Sorteio, que guarda o último disco sorteado até a meia-noite. */
export function DrawStage() {
  const { user, loading } = useSession()
  const owner = user?.id ?? null
  const { daily, retry, last, drawing, problem, draw } = useAlbumDraw(owner, !loading)
  const { allowance } = useAllowance()
  const [styleIds, setStyleIds] = useState(readSavedStyles)
  const [tab, setTab] = useState<StageTab>(readTab)
  // Sem saldo carregado (API fora), o botão fica ativo e o próprio sorteio mostra o erro.
  const hasTokens = allowance ? allowance.remaining > 0 : true

  function choose(next: StageTab) {
    setTab(next)
    saveTab(next)
  }

  function handleDraw() {
    if (!hasTokens) return
    void draw(user ? styleIds : [])
  }

  // Setas do teclado trocam de aba, como pede o padrão de abas (WAI-ARIA).
  function handleKeys(event: KeyboardEvent<HTMLDivElement>) {
    if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return
    event.preventDefault()
    const next = tab === 'daily' ? 'draw' : 'daily'
    choose(next)
    document.getElementById(`aba-${next}`)?.focus()
  }

  return (
    <Card size="palco" aria-label="Álbum do dia e sorteio">
      <div role="tablist" aria-label="O que mostrar no palco" className={styles.tabs} onKeyDown={handleKeys}>
        {TABS.map(({ id, label }) => (
          <button
            key={id}
            id={`aba-${id}`}
            type="button"
            role="tab"
            aria-selected={tab === id}
            aria-controls="palco"
            tabIndex={tab === id ? 0 : -1}
            className={styles.tab}
            onClick={() => choose(id)}
          >
            {label}
          </button>
        ))}
      </div>

      <div role="tabpanel" id="palco" aria-labelledby={`aba-${tab}`} className={styles.panel}>
        {tab === 'daily' ? (
          <DailyPanel daily={daily} onRetry={retry} onGoDraw={() => choose('draw')} />
        ) : (
          <DrawPanel
            last={last}
            drawing={drawing}
            hasTokens={hasTokens}
            onDraw={handleDraw}
            picker={user ? <StylePicker selected={styleIds} onChange={setStyleIds} disabled={drawing} /> : null}
            notice={<Notice problem={problem} loggedIn={Boolean(user)} remaining={allowance?.remaining} />}
          />
        )}
      </div>
    </Card>
  )
}

interface DailyPanelProps {
  daily: DailyState
  onRetry: () => void
  onGoDraw: () => void
}

/** Aba "Álbum do dia": sempre o disco do dia, igual para todo mundo. */
function DailyPanel({ daily, onRetry, onGoDraw }: DailyPanelProps) {
  if (daily.status === 'error') return <ServiceDown onRetry={onRetry} />
  const album = daily.status === 'ready' ? daily.album : undefined

  return (
    <>
      <Turntable
        cover={album ? coverUrl(album) : undefined}
        title={album?.name}
        stashed={!album}
        showDailySticker={Boolean(album)}
      />
      <div className={styles.info}>
        {album ? (
          <AlbumDetails album={album} source="daily" />
        ) : (
          <p role="status" className={styles.message}>
            Colocando o disco do dia na vitrola…
          </p>
        )}
        <div className={styles.actions}>
          {album ? (
            <Button variant="menta" size="g" href={spotifyUrl(album)} target="_blank" rel="noreferrer">
              Ouvir no Spotify
            </Button>
          ) : null}
          <Button variant="papel" size="g" onClick={onGoDraw}>
            Sortear outro
          </Button>
        </div>
        <p className={styles.notice}>O mesmo disco para todo mundo hoje. Ele muda à meia-noite.</p>
      </div>
    </>
  )
}

interface DrawPanelProps {
  last: LastDraw | null
  drawing: boolean
  hasTokens: boolean
  onDraw: () => void
  /** Estilos (só conta logada). */
  picker: ReactNode
  /** Aviso abaixo dos botões. */
  notice: ReactNode
}

/** Aba "Sorteio": o último disco sorteado, os estilos e o botão de sortear. */
function DrawPanel({ last, drawing, hasTokens, onDraw, picker, notice }: DrawPanelProps) {
  const album = last?.album

  return (
    <>
      <Turntable
        cover={album ? coverUrl(album) : undefined}
        title={album?.name}
        stashed={drawing || !album}
        showDailySticker={false}
      />
      <div className={styles.info}>
        {album && last ? (
          <AlbumDetails album={album} source={last.fresh ? 'drawn' : 'last'} />
        ) : (
          <div>
            <Sticker color="rosa">Sua vez</Sticker>
            <h1 className={`display ${styles.bigTitle}`}>Sorteie um disco</h1>
            <p>A jukebox escolhe um álbum para você. O último sorteio fica guardado aqui até a meia-noite.</p>
          </div>
        )}

        {picker}

        <div className={styles.actions}>
          <Button variant="tomate" size="g" display onClick={onDraw} disabled={drawing || !hasTokens}>
            {drawing ? 'Sorteando…' : hasTokens ? 'Sortear álbum' : 'Volte amanhã'}
          </Button>
          {album ? (
            <Button variant="menta" size="g" href={spotifyUrl(album)} target="_blank" rel="noreferrer">
              Ouvir no Spotify
            </Button>
          ) : null}
        </div>

        {notice}
      </div>
    </>
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
