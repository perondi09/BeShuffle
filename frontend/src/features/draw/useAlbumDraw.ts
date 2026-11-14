import { useCallback, useEffect, useState } from 'react'
import { type Album, fetchDailyAlbum } from '../../api/albums'
import { drawAlbum, fetchMyDraws } from '../../api/draws'
import { ApiError } from '../../api/http'
import { useAllowance } from './allowance'
import { albumFromMyDraw, readLastDraw, saveLastDraw } from './lastDraw'

export type DailyState = { status: 'loading' } | { status: 'error' } | { status: 'ready'; album: Album }

/** Último sorteio de quem está na página. `fresh` quando acabou de sair, nesta visita. */
export interface LastDraw {
  album: Album
  fresh: boolean
  owner: number | null
}

/** O último sorteio que não deu certo; `null` quando está tudo bem. */
export type DrawProblem = { kind: 'limit' | 'failed'; message: string } | null

/** Tempo da animação de troca do disco; zero com movimento reduzido (ou sem matchMedia, como nos testes). */
function swapDelay(): number {
  const animate = window.matchMedia?.('(prefers-reduced-motion: no-preference)').matches ?? false
  return animate ? 650 : 0
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * Álbum do Dia (fixo) e último sorteio (guardado até a meia-noite). O sorteio volta ao recarregar a página: vem do
 * navegador e, para conta logada em outro aparelho, de GET /api/draws/mine.
 *
 * @param owner id da conta logada, ou null para visitante
 * @param sessionReady se já se sabe quem está logado
 */
export function useAlbumDraw(owner: number | null, sessionReady: boolean) {
  const { update, refresh } = useAllowance()
  const [daily, setDaily] = useState<DailyState>({ status: 'loading' })
  const [attempt, setAttempt] = useState(0)
  const [last, setLast] = useState<LastDraw | null>(null)
  const [drawing, setDrawing] = useState(false)
  const [problem, setProblem] = useState<DrawProblem>(null)

  useEffect(() => {
    let active = true
    fetchDailyAlbum()
      .then((album) => active && setDaily({ status: 'ready', album }))
      .catch(() => active && setDaily({ status: 'error' }))
    return () => {
      active = false
    }
  }, [attempt])

  const retry = useCallback(() => {
    setDaily({ status: 'loading' })
    setAttempt((n) => n + 1)
  }, [])

  useEffect(() => {
    if (!sessionReady) return
    let active = true
    const saved = readLastDraw(owner)
    const restore: Promise<Album | null> =
      saved || owner === null
        ? Promise.resolve(saved)
        : fetchMyDraws().then((list) => (list[0] ? albumFromMyDraw(list[0]) : null))
    restore
      .catch(() => null)
      .then((album) => {
        if (!active) return
        // Um sorteio feito agora vale mais que o restaurado; um de outra conta não vale.
        setLast((current) =>
          current?.fresh && current.owner === owner ? current : album ? { album, fresh: false, owner } : null,
        )
      })
    return () => {
      active = false
    }
  }, [owner, sessionReady])

  const draw = useCallback(
    async (genres: string[] = []) => {
      setDrawing(true)
      setProblem(null)
      try {
        const [result] = await Promise.all([drawAlbum(genres), wait(swapDelay())])
        setLast({ album: result.album, fresh: true, owner })
        saveLastDraw(owner, result.album, result.allowance.resetsAt)
        update(result.allowance)
      } catch (error) {
        if (error instanceof ApiError && error.status === 429) {
          setProblem({ kind: 'limit', message: error.message })
          void refresh()
        } else {
          setProblem({ kind: 'failed', message: 'O sorteio falhou e sua ficha continua com você. Tente de novo.' })
        }
      } finally {
        setDrawing(false)
      }
    },
    [update, refresh, owner],
  )

  return { daily, retry, last, drawing, problem, draw }
}
