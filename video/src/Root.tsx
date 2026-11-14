import { Composition } from 'remotion'
import { BeShuffleVideo, DURACAO_TOTAL } from './BeShuffleVideo'
import { Banner } from './divulgacao/Banner'
import { Retrato } from './divulgacao/Retrato'
import { FPS } from './tema'

export const RemotionRoot = () => (
  <>
    <Composition id="BeShuffle" component={BeShuffleVideo} durationInFrames={DURACAO_TOTAL} fps={FPS} width={1080} height={1920} />
    {/* Imagens de divulgação (npm run imagens): o quadro 45 já tem o Radinho parado na pose. */}
    <Composition id="Banner" component={Banner} durationInFrames={60} fps={FPS} width={1400} height={940} />
    <Composition id="Retrato" component={Retrato} durationInFrames={60} fps={FPS} width={1080} height={1350} />
  </>
)
