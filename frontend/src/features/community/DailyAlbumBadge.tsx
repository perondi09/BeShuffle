import type { TodayReviews } from '../../api/reviews'
import { diaPorExtenso } from '../../lib/date'
import styles from './DailyAlbumBadge.module.css'

/** Capa, nome, artista e dia do Álbum do Dia que está recebendo as notas. */
export function DailyAlbumBadge({ data }: { data: TodayReviews }) {
  return (
    <div className={styles.badge}>
      <span className={styles.cover}>
        {data.albumImageUrl ? <img src={data.albumImageUrl} alt={`Capa de ${data.albumName}`} /> : null}
      </span>
      <span className={styles.text}>
        <small>{data.date ? `Álbum do Dia de ${diaPorExtenso(data.date)}` : 'Álbum do Dia'}</small>
        <strong>{data.albumName}</strong>
        {data.albumArtist ? <span>{data.albumArtist}</span> : null}
      </span>
    </div>
  )
}
