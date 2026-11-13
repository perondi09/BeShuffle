import { getJson, putJson } from './http'

export interface Review {
  author: string
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
