import type { Album } from './albums'
import { getJson, postJson } from './http'

/** Sorteios do dia (GET /api/draws/allowance). Visitante tem 1; conta logada tem 10. */
export interface Allowance {
  plan: 'VISITANTE' | 'LOGADO'
  remaining: number
  dailyLimit: number
  /** Quando os sorteios voltam (meia-noite no fuso da API), em ISO-8601. */
  resetsAt: string
}

export interface DrawResult {
  album: Album
  allowance: Allowance
}

/** Um álbum dos "Sorteados de hoje". */
export interface DrawnToday {
  id: string
  name: string
  artist?: string | null
  imageUrl?: string | null
  count: number
  lastDrawnAt: string
}

/** Estilo musical que a conta logada pode escolher. */
export interface Genre {
  id: string
  name: string
}

export const fetchAllowance = () => getJson<Allowance>('/api/draws/allowance')

/** Gasta um sorteio do dia. `genres` só vale para conta logada. */
export const drawAlbum = (genres: string[] = []) =>
  postJson<DrawResult>('/api/draws', genres.length ? { genres } : {})

export const fetchDrawnToday = () => getJson<DrawnToday[]>('/api/draws/today')

export const fetchGenres = () => getJson<Genre[]>('/api/genres')
