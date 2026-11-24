import { cx } from '../../lib/cx'
import styles from './admin.module.css'

interface SegmentadoProps<T extends string> {
  /** Nome do grupo para leitores de tela ("Período", "Área"). */
  label: string
  value: T
  options: ReadonlyArray<{ id: T; label: string }>
  onChange: (value: T) => void
}

/** Filtro em pílula: uma opção ligada por vez (período, área, status da conta). */
export function Segmentado<T extends string>({ label, value, options, onChange }: SegmentadoProps<T>) {
  return (
    <div role="group" aria-label={label} className={styles.segmentado}>
      {options.map((option) => (
        <button
          key={option.id}
          type="button"
          aria-pressed={option.id === value}
          className={cx(styles.segmento, option.id === value && styles.segmentoOn)}
          onClick={() => onChange(option.id)}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}
