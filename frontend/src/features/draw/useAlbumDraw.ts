import { useCallback, useEffect, useState } from 'react'
import { type Album, fetchDailyAlbum } from '../../api/albums'
import { drawAlbum } from '../../api/draws'
import { ApiError } from '../../api/http'
import { useAllowance } from './allowance'

export type DrawState =
  | { status: 'loading' }
  | { status: 'error' }
  | { status: 'ready'; album: Album; source: 'daily' | 'drawn' }

/** O último sorteio que não deu certo; `null` quando está tudo bem. */
export type DrawProblem = { kind: 'limit' | 'failed'; message: string } | null

/** Tempo da animação de troca do disco; zero com movimento reduzido (ou sem matchMedia, como nos testes). */
function swapDelay(): number {
  const animate = window.matchMedia?.('(prefers-reduced-motion: no-preference)').matches ?? false
  return animate ? 650 : 0
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/** Carrega o Álbum do Dia e sorteia novos álbuns pela API, que conta os sorteios do dia. */
export function useAlbumDraw() {
  const { update, refresh } = useAllowance()
  const [state, setState] = useState<DrawState>({ status: 'loading' })
  const [drawing, setDrawing] = useState(false)
  const [problem, setProblem] = useState<DrawProblem>(null)

  useEffect(() => {
    let active = true
    fetchDailyAlbum()
      .then((album) => active && setState({ status: 'ready', album, source: 'daily' }))
      .catch(() => active && setState({ status: 'error' }))
    return () => {
      active = false
    }
  }, [])

  const draw = useCallback(
    async (genres: string[] = []) => {
      setDrawing(true)
      setProblem(null)
      try {
        const [result] = await Promise.all([drawAlbum(genres), wait(swapDelay())])
        setState({ status: 'ready', album: result.album, source: 'drawn' })
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
    [update, refresh],
  )

  return { state, drawing, problem, draw }
}
