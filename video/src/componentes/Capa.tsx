import { borda, cor, fonteDisplay } from '../tema'

export interface DiscoFicticio {
  titulo: string
  artista: string
  fundo: string
  forma: string
  texto?: string
}

/** Discos inventados: capas desenhadas com formas e cores (nada de capa real com direitos autorais). */
export const DISCOS: DiscoFicticio[] = [
  { titulo: 'Ondas de Neon', artista: 'Maré Alta', fundo: cor.tomate, forma: cor.gema },
  { titulo: 'Samba de Varanda', artista: 'Trio Quintal', fundo: cor.menta, forma: cor.papel, texto: cor.tinta },
  { titulo: 'Fita Cassete', artista: 'Os Rebobinados', fundo: cor.rosa, forma: cor.vinil1 },
  { titulo: 'Jazz na Garoa', artista: 'Quarteto Sereno', fundo: cor.vinil2, forma: cor.rosa },
  { titulo: 'Pagode do Bem', artista: 'Grupo Domingo', fundo: cor.gema, forma: cor.tomate, texto: cor.tinta },
  { titulo: 'Riff Elétrico', artista: 'Garagem 77', fundo: cor.tinta, forma: cor.menta },
]

export const Capa = ({ disco, tamanho }: { disco: DiscoFicticio; tamanho: number }) => (
  <div
    style={{
      width: tamanho,
      height: tamanho,
      position: 'relative',
      overflow: 'hidden',
      background: disco.fundo,
      border: borda(6),
      borderRadius: 24,
      flex: 'none',
    }}
  >
    <div
      style={{
        position: 'absolute',
        width: tamanho * 0.9,
        height: tamanho * 0.9,
        right: -tamanho * 0.25,
        top: -tamanho * 0.2,
        borderRadius: '50%',
        background: disco.forma,
        border: borda(5),
      }}
    />
    <div
      style={{
        position: 'absolute',
        left: tamanho * 0.08,
        bottom: tamanho * 0.08,
        right: tamanho * 0.08,
        fontFamily: fonteDisplay,
        fontSize: tamanho * 0.13,
        lineHeight: 1.02,
        color: disco.texto ?? cor.papel,
        textShadow: disco.texto ? 'none' : `3px 3px 0 ${cor.tinta}`,
      }}
    >
      {disco.titulo}
    </div>
  </div>
)
