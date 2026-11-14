import { linearTiming, TransitionSeries } from '@remotion/transitions'
import { slide } from '@remotion/transitions/slide'
import { wipe } from '@remotion/transitions/wipe'
import type { FC } from 'react'
import { Chamada } from './cenas/Chamada'
import { Gancho } from './cenas/Gancho'
import { Ideia } from './cenas/Ideia'
import { ParaQuem } from './cenas/ParaQuem'
import { PassoDia } from './cenas/PassoDia'
import { PassoNota } from './cenas/PassoNota'
import { PassoSorteio } from './cenas/PassoSorteio'
import { Problema } from './cenas/Problema'

const TRANSICAO = 12

/**
 * Roteiro em loop: a última cena termina com o palco vazio e a frase "Entra lá e me conta:", que emenda na
 * pergunta da primeira cena. Cada cena tem a sua duração em quadros (30 por segundo).
 */
export const CENAS: { Componente: FC<{ duracao: number }>; duracao: number }[] = [
  { Componente: Gancho, duracao: 105 },
  { Componente: Problema, duracao: 135 },
  { Componente: Ideia, duracao: 125 },
  { Componente: PassoDia, duracao: 135 },
  { Componente: PassoSorteio, duracao: 165 },
  { Componente: PassoNota, duracao: 135 },
  { Componente: ParaQuem, duracao: 165 },
  { Componente: Chamada, duracao: 140 },
]

/** As transições sobrepõem as cenas, então o vídeo é mais curto que a soma delas. */
export const DURACAO_TOTAL = CENAS.reduce((total, cena) => total + cena.duracao, 0) - TRANSICAO * (CENAS.length - 1)

export const BeShuffleVideo = () => (
  <TransitionSeries>
    {CENAS.flatMap(({ Componente, duracao }, i) => {
      const sequencia = (
        <TransitionSeries.Sequence key={`cena-${i}`} durationInFrames={duracao}>
          <Componente duracao={duracao} />
        </TransitionSeries.Sequence>
      )
      if (i === 0) return [sequencia]
      const tempo = linearTiming({ durationInFrames: TRANSICAO })
      // Alterna deslizar e cortina entre as cenas.
      const transicao =
        i % 2 ? (
          <TransitionSeries.Transition key={`transicao-${i}`} presentation={slide({ direction: 'from-right' })} timing={tempo} />
        ) : (
          <TransitionSeries.Transition key={`transicao-${i}`} presentation={wipe({ direction: 'from-left' })} timing={tempo} />
        )
      return [transicao, sequencia]
    })}
  </TransitionSeries>
)
