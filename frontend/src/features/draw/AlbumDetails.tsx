import { type Album, artistNames, releaseYear } from '../../api/albums'
import { Sticker, Tag, TagList } from '../../components/ui'
import styles from './AlbumDetails.module.css'

interface AlbumDetailsProps {
  album: Album
  source: 'daily' | 'drawn'
}

/** Título, artistas e etiquetas do álbum em destaque. */
export function AlbumDetails({ album, source }: AlbumDetailsProps) {
  const artists = artistNames(album)
  const year = releaseYear(album)
  const artistCount = album.artists?.length ?? 0

  return (
    <div>
      <Sticker color="menta" className={styles.when}>
        {source === 'daily' ? 'Hoje na jukebox' : 'Sorteado agora'}
      </Sticker>
      <h1 className={`display ${styles.title}`}>{album.name}</h1>
      {artists ? <p className={styles.artists}>{artists}</p> : null}
      {year || artistCount > 1 ? (
        <TagList>
          {year ? <Tag>{year}</Tag> : null}
          {artistCount > 1 ? <Tag>{artistCount} artistas</Tag> : null}
        </TagList>
      ) : null}
    </div>
  )
}
