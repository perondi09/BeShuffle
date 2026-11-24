import { getJson, putJson } from './http'

export interface Review {
  author: string
  /** Nick e avatar de quem avaliou, para linkar o perfil. */
  handle?: string
  avatar?: string
  /** Em estrelas, de 0,5 a 5 de meia em meia. */
  rating: number
  comment?: string | null
  updatedAt: string
}

export interface MyReview {
  rating: number
  comment?: string | null
  updatedAt: string
}

/** Notas do Álbum do Dia (GET/PUT /api/reviews/today). Só para conta logada. */
export interface TodayReviews {
  albumId: string
  albumName: string
  albumArtist?: string | null
  albumImageUrl?: string | null
  /** Dia do Álbum do Dia, no formato aaaa-mm-dd. */
  date?: string | null
  /** null enquanto ninguém deu nota. */
  average: number | null
  count: number
  mine: MyReview | null
  reviews: Review[]
}

export const fetchTodayReviews = () => getJson<TodayReviews>('/api/reviews/today')

/** Cria ou edita a nota de quem está logado. */
export const saveReview = (rating: number, comment: string) =>
  putJson<TodayReviews>('/api/reviews/today', { rating, comment: comment.trim() || null })
