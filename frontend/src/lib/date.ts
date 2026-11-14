/** "sábado, 14 de novembro" a partir de "2026-11-14" (a data do Álbum do Dia, sem hora nem fuso). */
export function diaPorExtenso(data: string): string {
  const [ano, mes, dia] = data.split('-').map(Number)
  if (!ano || !mes || !dia) return data
  return new Date(Date.UTC(ano, mes - 1, dia)).toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    timeZone: 'UTC',
  })
}
