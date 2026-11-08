import { Sticker, VinylDisc } from '../../components/ui'
import { cx } from '../../lib/cx'
import styles from './Turntable.module.css'

interface TurntableProps {
  cover?: string
  title?: string
  /** Disco recolhido atrás da capa enquanto sorteia. */
  stashed: boolean
  showDailySticker: boolean
}

/** Capa do álbum com o vinil saindo de trás. */
export function Turntable({ cover, title, stashed, showDailySticker }: TurntableProps) {
  return (
    <div className={cx(styles.turntable, stashed && styles.stashed)}>
      <VinylDisc className={styles.vinyl} labelUrl={cover} spinning={!stashed} />
      <div className={styles.cover}>
        {cover ? <img src={cover} alt={title ? `Capa de ${title}` : 'Capa do álbum'} /> : <div className={styles.blank} />}
      </div>
      {showDailySticker ? (
        <Sticker color="gema" tilt="forte" className={styles.sticker}>
          Álbum do dia
        </Sticker>
      ) : null}
    </div>
  )
}
