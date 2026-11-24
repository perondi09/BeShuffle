import { ApiError } from '../../api/http'

const inteiro = new Intl.NumberFormat('pt-BR')

/** 1284 → "1.284". */
export const numero = (valor: number) => inteiro.format(valor)

/** 4.6 → "4,6%"; null (sem base para calcular) → "—". */
export function percentual(valor: number | null): string {
  if (valor === null) return '—'
  return `${valor.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`
}

/** Parte sobre o total, em %, com uma casa; null quando o total é zero. */
export function parteDoTotal(parte: number, total: number): number | null {
  if (total <= 0) return null
  return Math.round((parte * 1000) / total) / 10
}

/** "2026-11-14" ou "2026-11-14T15:30:00Z" → "14/11/2026" (a data como veio, sem mexer no fuso). */
export function dataCurta(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}

/** "2026-11-14" → "14/11" (rótulo do eixo do gráfico). */
export function diaMes(iso: string): string {
  const [, mes, dia] = iso.slice(0, 10).split('-')
  return mes && dia ? `${dia}/${mes}` : iso
}

/** Há quanto tempo aconteceu: "agora", "há 12 min", "há 3 h", "há 2 dias" ou a data, passada uma semana. */
export function quando(iso: string, agora = Date.now()): string {
  const minutos = Math.floor((agora - new Date(iso).getTime()) / 60_000)
  if (Number.isNaN(minutos)) return iso
  if (minutos < 1) return 'agora'
  if (minutos < 60) return `há ${minutos} min`
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return `há ${horas} h`
  const dias = Math.floor(horas / 24)
  if (dias < 7) return dias === 1 ? 'há 1 dia' : `há ${dias} dias`
  return dataCurta(iso)
}

export const nomeDaArea = (area: 'musica' | 'filmes') => (area === 'musica' ? 'Música' : 'Filmes')

/** Mensagem para a pessoa: o texto do backend em erros de regra (400, 404, 409), um aviso próprio nos demais. */
export function mensagemDeErro(error: unknown, padrao: string): string {
  if (error instanceof ApiError) {
    if (error.status === 401) return 'Sua sessão acabou. Entre de novo.'
    if (error.status === 403) return 'Esta conta não tem acesso ao painel de admin.'
    if (error.status >= 400 && error.status < 500) return error.message
  }
  return padrao
}
