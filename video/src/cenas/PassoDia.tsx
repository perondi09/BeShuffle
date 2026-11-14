import { DISCOS } from '../componentes/Capa'
import { Cena, type PropsDaCena } from '../componentes/Cena'
import { Selo } from '../componentes/Selo'
import { Vitrola } from '../componentes/Vitrola'
import { cor } from '../tema'

/** Cena 4: o Álbum do Dia girando na vitrola. */
export const PassoDia = ({ duracao, fala }: PropsDaCena) => (
  <Cena
    etiqueta="PASSO 1: O ÁLBUM DO DIA"
    fala={fala}
    duracao={duracao}
    mascote={{ pose: 'acena', x: 830, tamanho: 330 }}
  >
    <Vitrola disco={DISCOS[0]} x={110} y={360} tamanho={560} atraso={0.25} />
    <Selo x={150} y={880} atraso={1.1} giro={-7}>
      Álbum do dia
    </Selo>
    <Selo x={150} y={1020} atraso={1.5} giro={3} fundo={cor.papel} tamanho={38}>
      muda à meia-noite
    </Selo>
  </Cena>
)
