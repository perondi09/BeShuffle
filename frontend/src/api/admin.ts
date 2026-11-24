import { deleteJson, getJson, postJson } from './http'

/* Rotas do painel de admin (backend: controller/AdminController). Só contas admin passam; as outras recebem 403. */

export type Periodo = 'hoje' | '7d' | '30d'
export type Area = 'tudo' | 'musica' | 'filmes'
export type StatusConta = 'todos' | 'ativos' | 'banidos' | 'admins'

export interface ResumoArea {
  visits: number
  draws: number
  reviews: number
}

export interface PontoDoDia {
  /** aaaa-mm-dd */
  date: string
  visits: number
  newAccounts: number
}

/** GET /api/admin/stats. Percentuais vêm com uma casa decimal e null quando não há base para calcular. */
export interface PainelAdmin {
  period: Periodo
  area: Area
  from: string
  to: string
  newAccounts: number
  totalAccounts: number
  /** Visitantes únicos (conta, cookie ou rede) que abriram o site no período. */
  visits: number
  visitorVisits: number
  loggedVisits: number
  active7Days: number
  draws: number
  reviews: number
  bannedAccounts: number
  conversionPct: number | null
  mobilePct: number | null
  return7DaysPct: number | null
  byArea: Record<'musica' | 'filmes', ResumoArea>
  last30Days: PontoDoDia[]
}

export interface UsuarioAdmin {
  id: number
  email: string
  displayName: string
  role: 'USER' | 'ADMIN'
  createdAt: string
  bannedAt: string | null
  banReason: string | null
  draws: number
  reviews: number
}

export interface Pagina<T> {
  items: T[]
  /** Começa em 0. */
  page: number
  size: number
  total: number
}

export interface ComentarioAdmin {
  reviewId: number
  userId: number
  displayName: string
  email: string
  rating: number
  comment: string
  updatedAt: string
  albumName: string
  albumDate: string
  area: 'musica' | 'filmes'
}

export interface EventoAdmin {
  type: string
  description: string
  area: 'musica' | 'filmes' | null
  at: string
}

export interface AlbumRegerado {
  albumName: string
  artistName: string | null
  date: string
}

export const fetchPainel = (periodo: Periodo, area: Area) =>
  getJson<PainelAdmin>(`/api/admin/stats?periodo=${periodo}&area=${area}`)

export interface BuscaUsuarios {
  q: string
  status: StatusConta
  page: number
  size?: number
}

export function fetchUsuarios({ q, status, page, size = 20 }: BuscaUsuarios) {
  const params = new URLSearchParams({ status, page: String(page), size: String(size) })
  if (q.trim()) params.set('q', q.trim())
  return getJson<Pagina<UsuarioAdmin>>(`/api/admin/users?${params.toString()}`)
}

export const banirUsuario = (id: number, reason: string) =>
  postJson<UsuarioAdmin>(`/api/admin/users/${id}/ban`, { reason })

export const desbanirUsuario = (id: number) => postJson<UsuarioAdmin>(`/api/admin/users/${id}/unban`, {})

export const fetchComentarios = (page: number, size = 20) =>
  getJson<Pagina<ComentarioAdmin>>(`/api/admin/comments?page=${page}&size=${size}`)

/** Apaga só o texto do comentário; a nota continua valendo. */
export const removerComentario = (reviewId: number) => deleteJson(`/api/admin/comments/${reviewId}`)

export const fetchAtividade = () => getJson<EventoAdmin[]>('/api/admin/activity')

export const regerarAlbumDoDia = () => postJson<AlbumRegerado>('/api/admin/daily-album/regenerate', {})
