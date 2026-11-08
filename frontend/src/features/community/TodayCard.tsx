import { Button, Card, LockedOverlay } from '../../components/ui'
import styles from './community.module.css'

const PLACEHOLDERS = ['Disco 1', 'Disco 2', 'Disco 3', 'Disco 4']

/**
 * Sorteados do dia. Só usuário logado vê; enquanto não existe login,
 * mostra a grade vazia atrás do cadeado.
 */
export function TodayCard() {
  return (
    <Card id="hoje" aria-labelledby="hoje-titulo">
      <div inert>
        <h2 id="hoje-titulo" className={`display ${styles.title}`}>
          Sorteados de hoje
        </h2>
        <p>O que todo mundo tirou na jukebox desde a meia-noite.</p>
        <div className={styles.grid}>
          {PLACEHOLDERS.map((name) => (
            <div key={name}>
              <div className={styles.mini} />
              <p className={styles.miniName}>
                {name}
                <span>Artista</span>
              </p>
            </div>
          ))}
        </div>
      </div>
      <LockedOverlay
        message="Crie uma conta grátis para ver o que a galera sorteou hoje."
        action={
          <Button variant="rosa" to="/cadastro">
            Criar conta
          </Button>
        }
      />
    </Card>
  )
}
