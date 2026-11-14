import { Button, Sticker } from '../../components/ui'
import styles from './DrawStage.module.css'
import { Turntable } from './Turntable'

/** Tela de "fora do ar": a API não respondeu (servidor parado, deploy no meio ou internet caída). */
export function ServiceDown({ onRetry }: { onRetry: () => void }) {
  return (
    <>
      <Turntable stashed showDailySticker={false} />
      <div className={styles.info}>
        <Sticker color="rosa">Fora do ar</Sticker>
        <h1 className={`display ${styles.bigTitle}`}>A jukebox parou de tocar</h1>
        <p role="alert" className={styles.message}>
          Não deu para carregar o álbum do dia agora.
        </p>
        <p className={styles.notice}>
          O BeShuffle pode estar fora do ar por alguns minutos, ou a sua internet caiu. Seus sorteios e notas ficam
          guardados. Tente de novo daqui a pouco.
        </p>
        <div className={styles.actions}>
          <Button variant="tomate" size="g" display onClick={onRetry}>
            Tentar de novo
          </Button>
        </div>
      </div>
    </>
  )
}
