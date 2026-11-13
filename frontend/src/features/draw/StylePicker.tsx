import { useEffect, useState } from 'react'
import { fetchGenres, type Genre } from '../../api/draws'
import { cx } from '../../lib/cx'
import styles from './StylePicker.module.css'
import { saveStyles } from './styleStorage'

interface StylePickerProps {
  selected: string[]
  onChange: (ids: string[]) => void
  disabled?: boolean
}

/** Estilos musicais do sorteio (conta logada). Nenhum marcado = sorteio de todos os estilos. */
export function StylePicker({ selected, onChange, disabled }: StylePickerProps) {
  const [genres, setGenres] = useState<Genre[]>([])

  useEffect(() => {
    let active = true
    fetchGenres()
      .then((list) => active && setGenres(list))
      .catch(() => active && setGenres([]))
    return () => {
      active = false
    }
  }, [])

  // Descarta estilos guardados que a API não oferece mais.
  useEffect(() => {
    if (genres.length === 0) return
    const valid = selected.filter((id) => genres.some((g) => g.id === id))
    if (valid.length !== selected.length) onChange(valid)
  }, [genres, selected, onChange])

  if (genres.length === 0) return null

  function toggle(id: string) {
    const next = selected.includes(id) ? selected.filter((s) => s !== id) : [...selected, id]
    saveStyles(next)
    onChange(next)
  }

  function clear() {
    saveStyles([])
    onChange([])
  }

  const none = selected.length === 0

  return (
    <fieldset className={styles.picker} disabled={disabled}>
      <legend className={styles.legend}>
        Estilos <span>{none ? 'todos' : `${selected.length} escolhido${selected.length > 1 ? 's' : ''}`}</span>
      </legend>
      <div className={styles.chips}>
        <button type="button" className={cx(styles.chip, none && styles.on)} aria-pressed={none} onClick={clear}>
          Todos
        </button>
        {genres.map((genre) => (
          <button
            key={genre.id}
            type="button"
            className={cx(styles.chip, selected.includes(genre.id) && styles.on)}
            aria-pressed={selected.includes(genre.id)}
            onClick={() => toggle(genre.id)}
          >
            {genre.name}
          </button>
        ))}
      </div>
    </fieldset>
  )
}
