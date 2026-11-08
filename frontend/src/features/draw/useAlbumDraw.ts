import { useCallback, useEffect, useState } from 'react'
import { type Album, fetchDailyAlbum, fetchRandomAlbum } from '../../api/albums'

export type DrawState =
  | { status: 'loading' }
  | { status: 'error' }
  | { status: 'ready'; album: Album; source: 'daily' | 'drawn' }

/** Tempo da animação de troca do disco; zero com movimento reduzido (ou sem matchMedia, como nos testes). */
function swapDelay(): number {
  const animate = window.matchMedia?.('(prefers-reduced-motion: no-preference)').matches ?? false
  return animate ? 650 : 0
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/** Carrega o Álbum do Dia e sorteia novos álbuns pela API. */
export function useAlbumDraw() {
  const [state, setState] = useState<DrawState>({ status: 'loading' })
  const [drawing, setDrawing] = useState(false)
  const [drawFailed, setDrawFailed] = useState(false)

  useEffect(() => {
    let active = true
    fetchDailyAlbum()
      .then((album) => active && setState({ status: 'ready', album, source: 'daily' }))
      .catch(() => active && setState({ status: 'error' }))
    return () => {
      active = false
    }
  }, [])

  /** Devolve `true` quando o sorteio deu certo. */
  const draw = useCallback(async () => {
    setDrawing(true)
    setDrawFailed(false)
    try {
      const [album] = await Promise.all([fetchRandomAlbum(), wait(swapDelay())])
      setState({ status: 'ready', album, source: 'drawn' })
      return true
    } catch {
      setDrawFailed(true)
      return false
    } finally {
      setDrawing(false)
    }
  }, [])

  return { state, drawing, drawFailed, draw }
}
