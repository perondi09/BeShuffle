import { Button, Card, Sticker } from '../components/ui'
import styles from './NotFoundPage.module.css'

export function NotFoundPage() {
  return (
    <Card size="palco" className={styles.page}>
      <Sticker color="gema" tilt="forte">
        Erro 404
      </Sticker>
      <h1 className={`display ${styles.title}`}>Essa faixa não existe</h1>
      <p>A página que você procurou não está nesse disco.</p>
      <Button variant="tomate" size="g" display to="/">
        Voltar para a jukebox
      </Button>
    </Card>
  )
}
