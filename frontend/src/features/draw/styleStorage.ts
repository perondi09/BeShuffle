const STORAGE_KEY = 'beshuffle:estilos'

/** Estilos escolhidos da última vez (só conveniência deste navegador). */
export function readSavedStyles(): string[] {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as unknown
    return Array.isArray(saved) ? saved.filter((id): id is string => typeof id === 'string') : []
  } catch {
    return []
  }
}

export function saveStyles(ids: string[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(ids))
  } catch {
    // Sem armazenamento: a escolha vale só até recarregar a página.
  }
}
