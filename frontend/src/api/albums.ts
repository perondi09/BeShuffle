/** Formato devolvido pela API (backend/entity/Album). */
export interface Album {
  id: string
  name: string
  release_date?: string
  uri?: string
  images?: { url: string; width?: number; height?: number }[]
  artists?: { id?: string; name: string }[]
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function getAlbum(path: string): Promise<Album> {
  const response = await fetch(path, { headers: { Accept: 'application/json' } })
  if (!response.ok) {
    throw new ApiError(response.status, `A API respondeu ${response.status}`)
  }
  return (await response.json()) as Album
}

export const fetchDailyAlbum = () => getAlbum('/api/albums/daily')
export const fetchRandomAlbum = () => getAlbum('/api/albums/random')

/** Link público do álbum no Spotify (open.spotify.com), montado a partir do id. */
export function spotifyUrl(album: Album): string {
  return `https://open.spotify.com/album/${album.id}`
}
