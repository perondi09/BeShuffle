import { getJson } from './http'

/** Formato devolvido pela API (backend/entity/Album). */
export interface Album {
  id: string
  name: string
  release_date?: string
  uri?: string
  images?: { url: string; width?: number; height?: number }[]
  artists?: { id?: string; name: string }[]
}

export const fetchDailyAlbum = () => getJson<Album>('/api/albums/daily')
export const fetchRandomAlbum = () => getJson<Album>('/api/albums/random')

/** Link público do álbum no Spotify (open.spotify.com), montado a partir do id. */
export function spotifyUrl(album: Album): string {
  return `https://open.spotify.com/album/${album.id}`
}

/** Maior imagem da capa, se houver (o Spotify manda da maior para a menor). */
export const coverUrl = (album: Album) => album.images?.[0]?.url

export const artistNames = (album: Album) => album.artists?.map((artist) => artist.name).join(', ') ?? ''

export const releaseYear = (album: Album) => album.release_date?.slice(0, 4)
