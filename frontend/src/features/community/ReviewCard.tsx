import { useState } from 'react'
import { Button, Card, LockedOverlay } from '../../components/ui'
import styles from './community.module.css'
import { RatingDiscs } from './RatingDiscs'

/** Nota e comentário do álbum. Bloqueado para visitante até existir login na API. */
export function ReviewCard() {
  const [rating, setRating] = useState(0)

  return (
    <Card aria-labelledby="nota-titulo">
      <div inert>
        <h2 id="nota-titulo" className={`display ${styles.title}`}>
          Sua nota
        </h2>
        <RatingDiscs value={rating} onChange={setRating} />
        <textarea className={styles.comment} placeholder="O que achou desse disco?" aria-label="Comentário" />
        <div className={styles.row}>
          <small>Seja a primeira pessoa a dar nota.</small>
          <Button variant="gema">Publicar</Button>
        </div>
      </div>
      <LockedOverlay
        message="Entre para dar nota e comentar os discos que você sorteia."
        action={
          <Button variant="rosa" to="/entrar">
            Entrar
          </Button>
        }
      />
    </Card>
  )
}
