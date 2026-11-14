import { Composition } from 'remotion'
import { BeShuffleVideo, DURACAO_TOTAL } from './BeShuffleVideo'
import { FPS } from './tema'

export const RemotionRoot = () => (
  <>
    <Composition id="BeShuffle" component={BeShuffleVideo} durationInFrames={DURACAO_TOTAL} fps={FPS} width={1080} height={1920} />
  </>
)
