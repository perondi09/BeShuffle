import { linearTiming, TransitionSeries } from '@remotion/transitions'
import { slide } from '@remotion/transitions/slide'
import { wipe } from '@remotion/transitions/wipe'
import { Audio } from '@remotion/media'
import type { FC } from 'react'
import { interpolate, Sequence, staticFile } from 'remotion'
import { Chamada } from './cenas/Chamada'
import { Gancho } from './cenas/Gancho'
import { Ideia } from './cenas/Ideia'
import { ParaQuem } from './cenas/ParaQuem'
import { PassoDia } from './cenas/PassoDia'
import { PassoNota } from './cenas/PassoNota'
import { PassoSorteio } from './cenas/PassoSorteio'
import { Problema } from './cenas/Problema'
import type { PropsDaCena } from './componentes/Cena'
import narracao from './narracao.json'
import { FPS } from './tema'

const TRANSICAO = 12

/** A voz entra um pouco depois do começo da cena, e a cena ainda respira um pouco depois que a fala acaba. */
const ATRASO_DA_VOZ = 0.3
const RESPIRO = 18
/** Na última cena tudo some no fim (Chamada, saida={14}), deixando o palco vazio como no primeiro quadro. */
const SAIDA_DA_CHAMADA = 14

const ROTEIRO: { cena: string; Componente: FC<PropsDaCena>; minimo: number }[] = [
  { cena: 'gancho', Componente: Gancho, minimo: 105 },
  { cena: 'problema', Componente: Problema, minimo: 135 },
  { cena: 'ideia', Componente: Ideia, minimo: 125 },
  { cena: 'dia', Componente: PassoDia, minimo: 135 },
  { cena: 'sorteio', Componente: PassoSorteio, minimo: 165 },
  { cena: 'nota', Componente: PassoNota, minimo: 135 },
  { cena: 'paraquem', Componente: ParaQuem, minimo: 165 },
  { cena: 'chamada', Componente: Chamada, minimo: 140 },
]

/**
 * Roteiro em loop: a última cena termina com o palco vazio e a fala "Entra lá e me conta:", que emenda na
 * pergunta da primeira cena. Cada cena dura pelo menos o tempo da sua fala (src/narracao.json; a voz vem do
 * ElevenLabs, ver narracao.json e o README).
 */
export const CENAS = ROTEIRO.map(({ cena, Componente, minimo }) => {
  const falada = narracao.find((item) => item.cena === cena)
  if (!falada) throw new Error(`Falta a narração da cena ${cena} em src/narracao.json`)
  const quadros = Math.ceil(falada.segundos * FPS)
  const saida = cena === 'chamada' ? SAIDA_DA_CHAMADA : 0
  const duracao = Math.max(minimo, Math.ceil(ATRASO_DA_VOZ * FPS) + quadros + RESPIRO + saida)
  return { cena, Componente, duracao, arquivo: falada.arquivo, fala: { texto: falada.texto, quadros } }
})

/** As transições sobrepõem as cenas, então o vídeo é mais curto que a soma delas. */
export const DURACAO_TOTAL = CENAS.reduce((total, cena) => total + cena.duracao, 0) - TRANSICAO * (CENAS.length - 1)

/** Trechos (em quadros do vídeo) em que o narrador está falando. */
const FALAS = CENAS.map((cena, i) => {
  const comeco = CENAS.slice(0, i).reduce((total, c) => total + c.duracao, 0) - TRANSICAO * i
  const inicio = comeco + Math.round(ATRASO_DA_VOZ * FPS)
  return { inicio, fim: inicio + cena.fala.quadros }
})

const MUSICA_ALTA = 0.18
const MUSICA_BAIXA = 0.07
const RAMPA = 8

/** Volume da música: baixo enquanto o narrador fala (ducking), um pouco mais alto nos intervalos. */
function volumeDaMusica(quadro: number): number {
  let falando = 0
  for (const { inicio, fim } of FALAS) {
    const v = interpolate(quadro, [inicio - RAMPA, inicio, fim, fim + RAMPA], [0, 1, 1, 0], {
      extrapolateLeft: 'clamp',
      extrapolateRight: 'clamp',
    })
    falando = Math.max(falando, v)
  }
  return MUSICA_ALTA - (MUSICA_ALTA - MUSICA_BAIXA) * falando
}

export const BeShuffleVideo = () => (
  <>
    {/* Trilha original (scripts/compor-trilha.py), do tamanho exato do vídeo: emenda no loop. */}
    <Audio src={staticFile('musica/trilha.wav')} volume={volumeDaMusica} />
    <TransitionSeries>
      {CENAS.flatMap(({ Componente, duracao, arquivo, fala }, i) => {
        const sequencia = (
          <TransitionSeries.Sequence key={`cena-${i}`} durationInFrames={duracao}>
            <Componente duracao={duracao} fala={fala} />
            <Sequence from={Math.round(ATRASO_DA_VOZ * FPS)} layout="none">
              <Audio src={staticFile(arquivo)} />
            </Sequence>
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
  </>
)
