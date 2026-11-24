import { Button } from '../../components/ui'
import styles from './admin.module.css'
import { numero } from './format'

interface PaginacaoProps {
  /** Começa em 0. */
  page: number
  size: number
  total: number
  onChange: (page: number) => void
}

/** "Mostrando 1–20 de 1.284" com Anterior e Próxima. */
export function Paginacao({ page, size, total, onChange }: PaginacaoProps) {
  if (total === 0) return null
  const primeiro = page * size + 1
  const ultimo = Math.min((page + 1) * size, total)
  const ultimaPagina = Math.ceil(total / size) - 1

  return (
    <nav className={styles.paginacao} aria-label="Páginas">
      <span>
        Mostrando {numero(primeiro)}–{numero(ultimo)} de {numero(total)}
      </span>
      <div className={styles.paginacaoBotoes}>
        <Button disabled={page <= 0} onClick={() => onChange(page - 1)}>
          Anterior
        </Button>
        <Button disabled={page >= ultimaPagina} onClick={() => onChange(page + 1)}>
          Próxima
        </Button>
      </div>
    </nav>
  )
}
