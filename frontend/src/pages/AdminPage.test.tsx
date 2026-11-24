import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import { beforeAll, describe, expect, it } from 'vitest'
import { json, mockApi, renderAt } from '../test/render'

// A rota /admin carrega a página sob demanda (lazy). Aquecer o módulo aqui evita que o primeiro teste pague esse
// carregamento dentro do limite de 1 s do findBy quando vários arquivos de teste rodam juntos.
beforeAll(async () => {
  await import('./AdminPage')
})

const admin = { id: 1, email: 'guilherme@exemplo.com', displayName: 'Guilherme', admin: true }
const ana = { id: 2, email: 'ana@exemplo.com', displayName: 'Ana', admin: false }
const allowance = { plan: 'LOGADO', remaining: 10, dailyLimit: 10, resetsAt: '2026-11-25T03:00:00Z' }

const ultimos30Dias = Array.from({ length: 30 }, (_, i) => ({
  date: new Date(Date.UTC(2026, 10, 24 - (29 - i))).toISOString().slice(0, 10),
  visits: 100 + i * 10,
  newAccounts: i % 5,
}))

const painel = {
  period: '7d',
  area: 'tudo',
  from: '2026-11-18',
  to: '2026-11-24',
  newAccounts: 37,
  totalAccounts: 1284,
  visits: 2418,
  visitorVisits: 1906,
  loggedVisits: 512,
  active7Days: 412,
  draws: 1340,
  reviews: 301,
  bannedAccounts: 6,
  conversionPct: 1.9,
  mobilePct: 71,
  return7DaysPct: null,
  byArea: {
    musica: { visits: 2418, draws: 1340, reviews: 301 },
    filmes: { visits: 0, draws: 0, reviews: 0 },
  },
  last30Days: ultimos30Dias,
}

const atividade = [
  { type: 'BAN', description: 'Guilherme baniu Spam Bot: propaganda', area: null, at: '2026-11-24T12:00:00Z' },
  { type: 'REMOVE_COMMENT', description: 'Guilherme removeu um comentário de Rafa', area: 'musica', at: '2026-11-24T11:00:00Z' },
]

const usuario = (id: number, nome: string, extra: Record<string, unknown> = {}) => ({
  id,
  email: `${nome.toLowerCase().replace(' ', '')}@exemplo.com`,
  displayName: nome,
  role: 'USER',
  createdAt: '2026-11-10T15:00:00Z',
  bannedAt: null,
  banReason: null,
  draws: 4,
  reviews: 2,
  ...extra,
})

const pagina = <T,>(items: T[], total = items.length) => ({ items, page: 0, size: 20, total })

const base = {
  '/api/draws/allowance': () => json(allowance),
}

const corpo = (init?: RequestInit) => JSON.parse(String(init?.body)) as Record<string, unknown>

describe('AdminPage: quem entra', () => {
  it('visitante vai para a tela de entrar', async () => {
    mockApi({ ...base, '/api/me': () => json({}, 401) })

    renderAt('/admin')

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('conta comum vê o 404 e nem chega a pedir dados do admin', async () => {
    const fetchMock = mockApi({ ...base, '/api/me': () => json(ana) })

    renderAt('/admin')

    expect(await screen.findByRole('heading', { name: 'Essa faixa não existe' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Painel do admin' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('/api/admin'))).toBe(false)
  })

  it('conta admin tem o link no topo e no menu do celular, e o fundo neutro enquanto o painel está aberto', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/activity': () => json(atividade),
    })

    const { unmount } = renderAt('/admin')

    expect(await screen.findByRole('heading', { level: 1, name: 'Painel do admin' })).toBeInTheDocument()
    const links = screen.getAllByRole('link', { name: 'Admin' })
    expect(links).toHaveLength(2)
    links.forEach((link) => expect(link).toHaveAttribute('href', '/admin'))
    expect(document.body.dataset.tema).toBe('admin')

    unmount()
    expect(document.body.dataset.tema).toBeUndefined()
  })
})

describe('AdminPage: painel', () => {
  it('mostra os números do site, por área, a atividade e o gráfico', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/activity': () => json(atividade),
    })

    renderAt('/admin')

    const metricas = within(await screen.findByRole('list', { name: 'Números do período' }))
    expect(metricas.getByText('37')).toBeInTheDocument()
    expect(metricas.getByText('1.284 no total')).toBeInTheDocument()
    expect(metricas.getByText('2.418')).toBeInTheDocument()
    expect(metricas.getByText('1.906 visitantes · 512 logados')).toBeInTheDocument()
    expect(metricas.getByText('32,1% das contas')).toBeInTheDocument()
    expect(metricas.getByText('1.340')).toBeInTheDocument()
    expect(metricas.getByText('301')).toBeInTheDocument()
    expect(metricas.getByText('6')).toBeInTheDocument()

    expect(screen.getByText('1,9%')).toBeInTheDocument()
    expect(screen.getByText('71%')).toBeInTheDocument()
    // Sem base para calcular, o painel mostra um traço em vez de inventar um número.
    expect(screen.getByText('—')).toBeInTheDocument()

    const porArea = within(screen.getByRole('region', { name: 'Por área' }))
    expect(porArea.getByText('2.418 acessos')).toBeInTheDocument()
    expect(porArea.getByText('1.340 sorteios · 301 notas')).toBeInTheDocument()
    expect(porArea.getByText('Música 100%')).toBeInTheDocument()
    expect(porArea.getByText('Filmes 0%')).toBeInTheDocument()

    expect(await screen.findByText('Guilherme baniu Spam Bot: propaganda')).toBeInTheDocument()
    expect(screen.getByText('Guilherme removeu um comentário de Rafa')).toBeInTheDocument()

    const grafico = screen.getByRole('img', { name: /Acessos e contas novas por dia, de 26\/10\/2026 a 24\/11\/2026/ })
    expect(grafico).toHaveAccessibleName(/Pico de acessos: 390 em 24\/11\/2026/)
  })

  it('muda o período e a área e pede os números de novo', async () => {
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/stats?periodo=30d&area=tudo': () => json({ ...painel, period: '30d', newAccounts: 120 }),
      '/api/admin/stats?periodo=30d&area=filmes': () => json({ ...painel, period: '30d', area: 'filmes', newAccounts: 0 }),
      '/api/admin/activity': () => json([]),
    })

    renderAt('/admin')
    await screen.findByRole('list', { name: 'Números do período' })

    fireEvent.click(within(screen.getByRole('group', { name: 'Período' })).getByRole('button', { name: '30 dias' }))
    expect(await within(await screen.findByRole('list', { name: 'Números do período' })).findByText('120')).toBeInTheDocument()

    fireEvent.click(within(screen.getByRole('group', { name: 'Área' })).getByRole('button', { name: 'Filmes' }))
    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith('/api/admin/stats?periodo=30d&area=filmes', expect.anything()),
    )
    expect(await screen.findByText('Nada por aqui ainda.')).toBeInTheDocument()
  })

  it('avisa quando os números não carregam', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json({}, 500),
      '/api/admin/activity': () => json([]),
    })

    renderAt('/admin')

    expect(await screen.findByText('Não deu para carregar os números agora.')).toBeInTheDocument()
  })

  it('regerar o Álbum do Dia pede confirmação e mostra o novo disco', async () => {
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/activity': () => json([]),
      '/api/admin/daily-album/regenerate': () =>
        json({ albumName: 'Mylo Xyloto', artistName: 'Coldplay', date: '2026-11-24' }),
    })

    renderAt('/admin')
    fireEvent.click(await screen.findByRole('button', { name: 'Regerar Álbum do Dia' }))
    // Ainda não chamou nada: primeiro a confirmação.
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('regenerate'))).toBe(false)

    fireEvent.click(screen.getByRole('button', { name: 'Trocar o álbum' }))

    expect(await screen.findByText('Novo Álbum do Dia: Mylo Xyloto, de Coldplay.')).toBeInTheDocument()
    const chamada = fetchMock.mock.calls.find(([url]) => String(url).includes('regenerate'))
    expect(chamada?.[1]?.method).toBe('POST')
  })

  it('mostra o motivo quando o servidor recusa regerar o álbum que já tem notas', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/activity': () => json([]),
      '/api/admin/daily-album/regenerate': () =>
        json({ error: 'ACTION_NOT_ALLOWED', message: 'O Álbum do Dia de hoje já tem notas; regerar apagaria essas notas' }, 409),
    })

    renderAt('/admin')
    fireEvent.click(await screen.findByRole('button', { name: 'Regerar Álbum do Dia' }))
    fireEvent.click(screen.getByRole('button', { name: 'Trocar o álbum' }))

    expect(
      await screen.findByText('O Álbum do Dia de hoje já tem notas; regerar apagaria essas notas'),
    ).toBeInTheDocument()
  })
})

describe('AdminPage: usuários', () => {
  const lista = '/api/admin/users?status=todos&page=0&size=20'

  it('lista as contas com a situação de cada uma e sem botão de banir para admin', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () =>
        json(
          pagina([
            usuario(1, 'Guilherme', { role: 'ADMIN' }),
            usuario(2, 'Ana'),
            usuario(3, 'Spam Bot', { bannedAt: '2026-11-20T10:00:00Z', banReason: 'propaganda' }),
          ]),
        ),
    })

    renderAt('/admin?aba=usuarios')

    const tabela = within(await screen.findByRole('table', { name: 'Contas do BeShuffle' }))
    expect(tabela.getByText('ana@exemplo.com')).toBeInTheDocument()
    expect(tabela.getByText('Admin')).toBeInTheDocument()
    expect(tabela.getByText('Ativa')).toBeInTheDocument()
    expect(tabela.getByText('Banida')).toBeInTheDocument()
    expect(tabela.getByText('propaganda')).toBeInTheDocument()
    expect(tabela.getAllByText('10/11/2026')).toHaveLength(3)
    expect(tabela.getByRole('button', { name: 'Banir Ana' })).toBeInTheDocument()
    expect(tabela.getByRole('button', { name: 'Desbanir Spam Bot' })).toBeInTheDocument()
    expect(tabela.queryByRole('button', { name: 'Banir Guilherme' })).not.toBeInTheDocument()
    expect(screen.getByText('Mostrando 1–3 de 3')).toBeInTheDocument()
  })

  it('bane com motivo: a janela exige o motivo, manda o POST e a lista passa a mostrar a conta banida', async () => {
    let banida = false
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () =>
        json(pagina([usuario(2, 'Ana', banida ? { bannedAt: '2026-11-24T12:00:00Z', banReason: 'ofensas repetidas' } : {})])),
      '/api/admin/users/2/ban': () => {
        banida = true
        return json(usuario(2, 'Ana', { bannedAt: '2026-11-24T12:00:00Z', banReason: 'ofensas repetidas' }))
      },
    })

    renderAt('/admin?aba=usuarios')
    fireEvent.click(await screen.findByRole('button', { name: 'Banir Ana' }))

    const janela = within(screen.getByRole('dialog', { name: 'Banir Ana?' }))
    // Sem motivo não manda nada.
    fireEvent.click(janela.getByRole('button', { name: 'Banir conta' }))
    expect(await janela.findByText('Conte o motivo em pelo menos 3 letras.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/ban'))).toBe(false)

    fireEvent.change(janela.getByLabelText('Motivo (fica no registro)'), { target: { value: '  ofensas repetidas ' } })
    fireEvent.click(janela.getByRole('button', { name: 'Banir conta' }))

    expect(await screen.findByText('Ana foi banida e perdeu o acesso.')).toBeInTheDocument()
    const chamada = fetchMock.mock.calls.find(([url]) => String(url).endsWith('/ban'))
    expect(chamada?.[1]?.method).toBe('POST')
    expect(corpo(chamada?.[1])).toEqual({ reason: 'ofensas repetidas' })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(await screen.findByText('Banida')).toBeInTheDocument()
  })

  it('Escape e Cancelar fecham a janela sem banir', async () => {
    const fetchMock = mockApi({ ...base, '/api/me': () => json(admin), [lista]: () => json(pagina([usuario(2, 'Ana')])) })

    renderAt('/admin?aba=usuarios')
    fireEvent.click(await screen.findByRole('button', { name: 'Banir Ana' }))
    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'Escape' })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Banir Ana' }))
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Cancelar' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/ban'))).toBe(false)
  })

  it('mostra a recusa do servidor dentro da janela e continua aberta', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () => json(pagina([usuario(2, 'Ana')])),
      '/api/admin/users/2/ban': () => json({ error: 'ACTION_NOT_ALLOWED', message: 'Não dá para banir uma conta de admin' }, 409),
    })

    renderAt('/admin?aba=usuarios')
    fireEvent.click(await screen.findByRole('button', { name: 'Banir Ana' }))
    const janela = within(screen.getByRole('dialog'))
    fireEvent.change(janela.getByLabelText('Motivo (fica no registro)'), { target: { value: 'teste' } })
    fireEvent.click(janela.getByRole('button', { name: 'Banir conta' }))

    expect(await janela.findByRole('alert')).toHaveTextContent('Não dá para banir uma conta de admin')
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('desbane e recarrega a lista', async () => {
    let banida = true
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () => json(pagina([usuario(3, 'Spam Bot', banida ? { bannedAt: '2026-11-20T10:00:00Z', banReason: 'x' } : {})])),
      '/api/admin/users/3/unban': () => {
        banida = false
        return json(usuario(3, 'Spam Bot'))
      },
    })

    renderAt('/admin?aba=usuarios')
    fireEvent.click(await screen.findByRole('button', { name: 'Desbanir Spam Bot' }))

    expect(await screen.findByText('Spam Bot voltou a ter acesso.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.find(([url]) => String(url).endsWith('/unban'))?.[1]?.method).toBe('POST')
    expect(await screen.findByRole('button', { name: 'Banir Spam Bot' })).toBeInTheDocument()
  })

  it('busca por nome ou e-mail e filtra pela situação, voltando para a primeira página', async () => {
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () => json(pagina([usuario(2, 'Ana'), usuario(4, 'Bruno')])),
      [`${lista}&q=ana`]: () => json(pagina([usuario(2, 'Ana')])),
      // Mudar a situação mantém a busca que já estava valendo.
      '/api/admin/users?status=banidos&page=0&size=20&q=ana': () => json(pagina([])),
    })

    renderAt('/admin?aba=usuarios')
    await screen.findByRole('table', { name: 'Contas do BeShuffle' })

    fireEvent.change(screen.getByLabelText('Buscar'), { target: { value: ' ana ' } })
    fireEvent.submit(screen.getByRole('search'))
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith(`${lista}&q=ana`, expect.anything()))
    await waitFor(() => expect(screen.queryByText('bruno@exemplo.com')).not.toBeInTheDocument())

    fireEvent.click(within(screen.getByRole('group', { name: 'Situação' })).getByRole('button', { name: 'Banidos' }))
    expect(await screen.findByText('Nenhuma conta com esses filtros.')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/admin/users?status=banidos&page=0&size=20&q=ana', expect.anything())
  })

  it('pagina: Próxima pede a página seguinte', async () => {
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () => json({ items: [usuario(2, 'Ana')], page: 0, size: 20, total: 21 }),
      '/api/admin/users?status=todos&page=1&size=20': () => json({ items: [usuario(9, 'Zeca')], page: 1, size: 20, total: 21 }),
    })

    renderAt('/admin?aba=usuarios')
    expect(await screen.findByText('Mostrando 1–20 de 21')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()

    fireEvent.click(screen.getByRole('button', { name: 'Próxima' }))

    expect(await screen.findByText('Mostrando 21–21 de 21')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/admin/users?status=todos&page=1&size=20', expect.anything())
    expect(screen.getByRole('button', { name: 'Próxima' })).toBeDisabled()
  })

  it('avisa quando a lista não carrega', async () => {
    mockApi({ ...base, '/api/me': () => json(admin), [lista]: () => json({}, 500) })

    renderAt('/admin?aba=usuarios')

    expect(await screen.findByText('Não deu para carregar as contas agora.')).toBeInTheDocument()
  })
})

describe('AdminPage: moderação', () => {
  const lista = '/api/admin/comments?page=0&size=20'
  const comentario = {
    reviewId: 9,
    userId: 7,
    displayName: 'Rafa',
    email: 'rafa@exemplo.com',
    rating: 2,
    comment: 'compra seguidor aqui',
    updatedAt: '2026-11-24T11:00:00Z',
    albumName: 'Clube da Esquina',
    albumDate: '2026-11-24',
    area: 'musica',
  }

  it('lista os comentários e remove só o texto depois de confirmar', async () => {
    let removido = false
    const fetchMock = mockApi({
      ...base,
      '/api/me': () => json(admin),
      [lista]: () => json(pagina(removido ? [] : [comentario])),
      '/api/admin/comments/9': () => {
        removido = true
        return new Response(null, { status: 204 })
      },
    })

    renderAt('/admin?aba=moderacao')

    expect(await screen.findByText('compra seguidor aqui')).toBeInTheDocument()
    expect(screen.getByText(/nota 2 de 5/)).toBeInTheDocument()
    expect(screen.getByText(/rafa@exemplo.com · Clube da Esquina \(24\/11\/2026\)/)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Remover o comentário de Rafa' }))
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('/comments/9'))).toBe(false)
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar a remoção do comentário de Rafa' }))

    expect(await screen.findByText('Comentário de Rafa removido. A nota continua valendo.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.find(([url]) => String(url).includes('/comments/9'))?.[1]?.method).toBe('DELETE')
    expect(await screen.findByText('Nenhum comentário por enquanto.')).toBeInTheDocument()
  })

  it('cancelar a confirmação não remove nada', async () => {
    const fetchMock = mockApi({ ...base, '/api/me': () => json(admin), [lista]: () => json(pagina([comentario])) })

    renderAt('/admin?aba=moderacao')
    fireEvent.click(await screen.findByRole('button', { name: 'Remover o comentário de Rafa' }))
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(screen.getByRole('button', { name: 'Remover o comentário de Rafa' })).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('/comments/9'))).toBe(false)
  })

  it('troca de aba pelo filtro do topo', async () => {
    mockApi({
      ...base,
      '/api/me': () => json(admin),
      '/api/admin/stats?periodo=7d&area=tudo': () => json(painel),
      '/api/admin/activity': () => json([]),
      [lista]: () => json(pagina([])),
    })

    renderAt('/admin')
    await screen.findByRole('list', { name: 'Números do período' })

    fireEvent.click(within(screen.getByRole('group', { name: 'Seções do admin' })).getByRole('button', { name: 'Moderação' }))

    expect(await screen.findByText('Nenhum comentário por enquanto.')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Números do período' })).not.toBeInTheDocument()
  })
})
