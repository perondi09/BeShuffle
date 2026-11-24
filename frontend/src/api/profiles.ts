import { getJson } from './http'

/* Perfis e diários de notas (backend: controller/PerfilController). Só para conta logada. */

/** GET /api/users/{handle}. Sem e-mail; a média é em estrelas e vem null enquanto não há nota. */
export interface PublicProfile {
  handle: string
  displayName: string
  avatar: string
  joinedAt: string
  reviewCount: number
  average: number | null
}

/** Uma entrada do diário: o Álbum do Dia avaliado, a nota em estrelas (0,5 a 5) e o comentário. */
export interface DiaryEntry {
  albumId: string
  albumName: string
  albumArtist: string | null
  albumImageUrl: string | null
  /** aaaa-mm-dd */
  date: string
  rating: number
  comment: string | null
  updatedAt: string
}

export interface Page<T> {
  items: T[]
  /** Começa em 0. */
  page: number
  size: number
  total: number
}

export const fetchProfile = (handle: string) => getJson<PublicProfile>(`/api/users/${encodeURIComponent(handle)}`)

/** Uma página do diário, do Álbum do Dia mais recente para o mais antigo. */
export const fetchDiary = (handle: string, page: number, size = 12) =>
  getJson<Page<DiaryEntry>>(`/api/users/${encodeURIComponent(handle)}/reviews?page=${page}&size=${size}`)
