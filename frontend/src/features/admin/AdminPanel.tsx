import { type ReactNode, useEffect, useState } from 'react'
import {
  type AlbumRegerado,
  type Area,
  type EventoAdmin,
  type PainelAdmin,
  type Periodo,
  fetchAtividade,
  fetchPainel,
  regerarAlbumDoDia,
} from '../../api/admin'
import { Button, Card } from '../../components/ui'
import { cx } from '../../lib/cx'
import { AcessosChart } from './AcessosChart'
import styles from './admin.module.css'
import { mensagemDeErro, nomeDaArea, numero, parteDoTotal, percentual, quando } from './format'
import { Segmentado } from './Segmentado'

const PERIODOS = [
  { id: 'hoje', label: 'Hoje' },
  { id: '7d', label: '7 dias' },
  { id: '30d', label: '30 dias' },
] as const

const AREAS = [
  { id: 'tudo', label: 'Tudo' },
  { id: 'musica', label: 'Música' },
  { id: 'filmes', label: 'Filmes' },
] as const

function Metrica({ titulo, valor, detalhe, rosa }: { titulo: string; valor: string; detalhe?: ReactNode; rosa?: boolean }) {
  return (
    <li className={cx(styles.metrica, rosa && styles.metricaRosa)}>
      <span className={styles.metricaTitulo}>{titulo}</span>
      <strong className={`display ${styles.metricaValor}`}>{valor}</strong>
      {detalhe ? <small className={styles.metricaDetalhe}>{detalhe}</small> : null}
    </li>
  )
}

/** Etiqueta da área do evento: lilás para Música, vermelho cortina para Filmes. */
function EtiquetaDeArea({ area }: { area: 'musica' | 'filmes' }) {
  return <span className={cx(styles.etiqueta, area === 'musica' ? styles.etiquetaMusica : styles.etiquetaFilmes)}>{nomeDaArea(area)}</span>
}

/** Aba "Painel": números do site todo, filtráveis por período e por área. */
export function AdminPanel() {
  const [periodo, setPeriodo] = useState<Periodo>('7d')
  const [area, setArea] = useState<Area>('tudo')
  const [painel, setPainel] = useState<PainelAdmin | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [eventos, setEventos] = useState<EventoAdmin[] | null>(null)
  const [erroEventos, setErroEventos] = useState(false)

  useEffect(() => {
    let ativo = true
    fetchPainel(periodo, area)
      .then((dados) => {
        if (!ativo) return
        setPainel(dados)
        setErro(null)
      })
      .catch((e: unknown) => ativo && setErro(mensagemDeErro(e, 'Não deu para carregar os números agora.')))
    return () => {
      ativo = false
    }
  }, [periodo, area])

  useEffect(() => {
    let ativo = true
    fetchAtividade()
      .then((lista) => ativo && setEventos(lista))
      .catch(() => ativo && setErroEventos(true))
    return () => {
      ativo = false
    }
  }, [])

  const atualizando = painel !== null && (painel.period !== periodo || painel.area !== area)

  return (
    <div className={styles.secao}>
      <div className={styles.barra}>
        <Segmentado label="Período" value={periodo} options={PERIODOS} onChange={setPeriodo} />
        <Segmentado label="Área" value={area} options={AREAS} onChange={setArea} />
        {atualizando ? (
          <span role="status" className={styles.vazio}>
            Atualizando…
          </span>
        ) : null}
      </div>

      {erro ? (
        <p role="alert" className={styles.erro}>
          {erro}
        </p>
      ) : null}
      {!painel && !erro ? <p role="status">Carregando os números…</p> : null}

      {painel ? (
        <>
          <ul className={styles.metricas} aria-label="Números do período">
            <Metrica titulo="Contas novas" valor={numero(painel.newAccounts)} detalhe={`${numero(painel.totalAccounts)} no total`} />
            <Metrica
              titulo="Acessos"
              valor={numero(painel.visits)}
              detalhe={`${numero(painel.visitorVisits)} visitantes · ${numero(painel.loggedVisits)} logados`}
            />
            <Metrica
              titulo="Ativos em 7 dias"
              valor={numero(painel.active7Days)}
              detalhe={`${percentual(parteDoTotal(painel.active7Days, painel.totalAccounts))} das contas`}
            />
            <Metrica titulo="Sorteios" valor={numero(painel.draws)} />
            <Metrica titulo="Notas" valor={numero(painel.reviews)} />
            <Metrica titulo="Banidas" valor={numero(painel.bannedAccounts)} rosa />
          </ul>

          <p className={styles.metricasPequenas}>
            <span>
              Visitante que virou conta: <strong>{percentual(painel.conversionPct)}</strong>
            </span>
            <span>
              Acessos pelo celular: <strong>{percentual(painel.mobilePct)}</strong>
            </span>
            <span>
              Voltaram em 7 dias: <strong>{percentual(painel.return7DaysPct)}</strong>
            </span>
          </p>

          <div className={styles.duo}>
            <Card aria-labelledby="admin-grafico">
              <h2 id="admin-grafico" className={`display ${styles.tituloCard}`}>
                Acessos e contas novas · 30 dias
              </h2>
              <p className={styles.legenda}>
                <span className={styles.legendaItem}>
                  <i className={styles.amostra} aria-hidden="true" /> acessos
                </span>
                <span className={styles.legendaItem}>
                  <i className={cx(styles.amostra, styles.amostraConta)} aria-hidden="true" /> contas novas
                </span>
              </p>
              <AcessosChart pontos={painel.last30Days} />
            </Card>

            <Card aria-labelledby="admin-areas">
              <h2 id="admin-areas" className={`display ${styles.tituloCard}`}>
                Por área
              </h2>
              <p className={styles.legendaItem}>Sorteios no período</p>
              <PorArea painel={painel} />
            </Card>
          </div>
        </>
      ) : null}

      <Card aria-labelledby="admin-atividade">
        <h2 id="admin-atividade" className={`display ${styles.tituloCard}`}>
          Atividade recente
        </h2>
        {erroEventos ? <p role="alert">Não deu para carregar a atividade agora.</p> : null}
        {eventos === null && !erroEventos ? <p role="status">Carregando…</p> : null}
        {eventos?.length === 0 ? <p>Nada por aqui ainda.</p> : null}
        {eventos && eventos.length > 0 ? (
          <ul className={styles.eventos}>
            {eventos.map((evento, i) => (
              <li key={`${evento.type}-${evento.at}-${i}`} className={styles.evento}>
                {evento.area ? <EtiquetaDeArea area={evento.area} /> : null}
                <span>{evento.description}</span>
                <span className={styles.eventoQuando}>{quando(evento.at)}</span>
              </li>
            ))}
          </ul>
        ) : null}
      </Card>

      <RegerarAlbum />
    </div>
  )
}

function PorArea({ painel }: { painel: PainelAdmin }) {
  const { musica, filmes } = painel.byArea
  const totalSorteios = musica.draws + filmes.draws

  return (
    <>
      <div className={styles.divisao} role="img" aria-label={`Sorteios: ${numero(musica.draws)} de música e ${numero(filmes.draws)} de filmes`}>
        {totalSorteios > 0 ? (
          <>
            <div className={styles.divisaoMusica} style={{ width: `${(musica.draws / totalSorteios) * 100}%` }} />
            <div className={styles.divisaoFilmes} style={{ flex: 1 }} />
          </>
        ) : null}
      </div>
      <div className={styles.divisaoLegenda}>
        <span>Música {percentual(parteDoTotal(musica.draws, totalSorteios))}</span>
        <span>Filmes {percentual(parteDoTotal(filmes.draws, totalSorteios))}</span>
      </div>
      <ul className={styles.areas}>
        {(['musica', 'filmes'] as const).map((id) => (
          <li key={id} className={styles.areaLinha}>
            <EtiquetaDeArea area={id} />
            <span>
              {numero(painel.byArea[id].visits)} acessos
            </span>
            <small>
              {numero(painel.byArea[id].draws)} sorteios · {numero(painel.byArea[id].reviews)} notas
            </small>
          </li>
        ))}
      </ul>
    </>
  )
}

type Regeracao = { fase: 'parado' } | { fase: 'confirmar' } | { fase: 'enviando' } | { fase: 'ok'; album: AlbumRegerado } | { fase: 'erro'; mensagem: string }

/** Troca o Álbum do Dia de hoje (disco quebrado). Pede confirmação; o servidor recusa se o disco já tem notas. */
function RegerarAlbum() {
  const [estado, setEstado] = useState<Regeracao>({ fase: 'parado' })

  async function regerar() {
    setEstado({ fase: 'enviando' })
    try {
      setEstado({ fase: 'ok', album: await regerarAlbumDoDia() })
    } catch (e) {
      setEstado({ fase: 'erro', mensagem: mensagemDeErro(e, 'Não deu para regerar o Álbum do Dia agora.') })
    }
  }

  return (
    <section className={styles.regerar} aria-label="Álbum do Dia">
      {estado.fase === 'confirmar' || estado.fase === 'enviando' ? (
        <>
          <span>Trocar o Álbum do Dia de hoje por outro sorteado?</span>
          <Button variant="tomate" disabled={estado.fase === 'enviando'} onClick={regerar}>
            {estado.fase === 'enviando' ? 'Regerando…' : 'Trocar o álbum'}
          </Button>
          <Button onClick={() => setEstado({ fase: 'parado' })}>Cancelar</Button>
        </>
      ) : (
        <>
          <Button onClick={() => setEstado({ fase: 'confirmar' })}>Regerar Álbum do Dia</Button>
          <small>Use só se o disco de hoje veio quebrado (sem capa ou fora do Spotify).</small>
        </>
      )}
      {estado.fase === 'ok' ? (
        <p role="status" className={styles.aviso}>
          Novo Álbum do Dia: {estado.album.albumName}
          {estado.album.artistName ? `, de ${estado.album.artistName}` : ''}.
        </p>
      ) : null}
      {estado.fase === 'erro' ? (
        <p role="alert" className={styles.erro}>
          {estado.mensagem}
        </p>
      ) : null}
    </section>
  )
}
