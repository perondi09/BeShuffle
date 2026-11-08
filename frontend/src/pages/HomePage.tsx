import { ReviewCard } from '../features/community/ReviewCard'
import { TodayCard } from '../features/community/TodayCard'
import { DrawStage } from '../features/draw/DrawStage'
import styles from './HomePage.module.css'

/** Home: palco do sorteio e, abaixo, os cartões da comunidade. */
export function HomePage() {
  return (
    <>
      <DrawStage />
      <div className={styles.duo}>
        <TodayCard />
        <ReviewCard />
      </div>
    </>
  )
}
