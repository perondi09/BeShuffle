import { loadFont as carregarBagel } from '@remotion/google-fonts/BagelFatOne'
import { loadFont as carregarFredoka } from '@remotion/google-fonts/Fredoka'

/** As mesmas fontes do site: Bagel Fat One nos títulos e Fredoka no texto. */
export const fonteDisplay = carregarBagel('normal', { weights: ['400'], subsets: ['latin', 'latin-ext'] }).fontFamily
export const fonteTexto = carregarFredoka('normal', {
  weights: ['400', '500', '600'],
  subsets: ['latin', 'latin-ext'],
}).fontFamily

/** Paleta do site (frontend/src/styles/tokens.css). */
export const cor = {
  lilas: '#c9b8ff',
  lilasEscuro: '#b3a0f5',
  lilasClaro: '#f3eeff',
  tinta: '#1c1530',
  tomate: '#ff5a36',
  gema: '#ffc93c',
  menta: '#3ddc97',
  rosa: '#ff8fc7',
  papel: '#ffffff',
  vinil1: '#2a2140',
  vinil2: '#3b3060',
}

export const borda = (espessura = 6) => `${espessura}px solid ${cor.tinta}`
export const sombra = (n = 12) => `${n}px ${n}px 0 ${cor.tinta}`

export const FPS = 30
