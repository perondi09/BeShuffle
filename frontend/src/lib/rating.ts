/** A nota em estrelas, de 0,5 a 5 de meia em meia: 4 → "4", 3.5 → "3,5". */
export function formatRating(value: number): string {
  return value.toLocaleString('pt-BR', { maximumFractionDigits: 1 })
}

/** Quantos discos ficam cheios (inteiros) e se há meio disco, para desenhar a nota. */
export function splitRating(value: number): { full: number; half: boolean } {
  const doubled = Math.round(value * 2)
  return { full: Math.floor(doubled / 2), half: doubled % 2 === 1 }
}
