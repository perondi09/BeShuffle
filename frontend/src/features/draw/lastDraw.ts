import type { Album } from '../../api/albums'
import type { MyDraw } from '../../api/draws'

const DRAW_KEY = 'beshuffle:ultimo-sorteio'
const TAB_KEY = 'beshuffle:aba'

/** Aba do palco da home. */
export type StageTab = 'daily' | 'draw'

interface SavedDraw {
  /** Id da conta que sorteou; null para visitante. */
  owner: number | null
  /** Quando as fichas renovam (meia-noite da API): depois disso o sorteio é de ontem. */
  expiresAt: string
  album: Album
}

/** Último álbum sorteado neste navegador, se for da mesma conta (ou visitante) e ainda for o mesmo dia. */
export function readLastDraw(owner: number | null, now = Date.now()): Album | null {
  try {
    const saved = JSON.parse(localStorage.getItem(DRAW_KEY) ?? 'null') as SavedDraw | null
    if (!saved?.album?.id || saved.owner !== owner) return null
    return Date.parse(saved.expiresAt) > now ? saved.album : null
  } catch {
    return null
  }
}

export function saveLastDraw(owner: number | null, album: Album, expiresAt: string): void {
  try {
    localStorage.setItem(DRAW_KEY, JSON.stringify({ owner, expiresAt, album } satisfies SavedDraw))
  } catch {
    // Sem armazenamento: o sorteio aparece até recarregar a página.
  }
}

export function readTab(): StageTab {
  try {
    return localStorage.getItem(TAB_KEY) === 'draw' ? 'draw' : 'daily'
  } catch {
    return 'daily'
  }
}

export function saveTab(tab: StageTab): void {
  try {
    localStorage.setItem(TAB_KEY, tab)
  } catch {
    // Sem armazenamento: a aba volta para o Álbum do Dia ao recarregar.
  }
}

/** Um item de GET /api/draws/mine no formato de álbum que o palco mostra. */
export function albumFromMyDraw(draw: MyDraw): Album {
  return {
    id: draw.id,
    name: draw.name,
    artists: draw.artist ? [{ name: draw.artist }] : [],
    images: draw.imageUrl ? [{ url: draw.imageUrl }] : [],
  }
}
