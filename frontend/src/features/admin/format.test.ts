import { describe, expect, it } from 'vitest'
import { ApiError } from '../../api/http'
import { dataCurta, diaMes, mensagemDeErro, nomeDaArea, numero, parteDoTotal, percentual, quando } from './format'

describe('formatação do admin', () => {
  it('escreve números e percentuais em português', () => {
    expect(numero(1284)).toBe('1.284')
    expect(percentual(4.6)).toBe('4,6%')
    expect(percentual(71)).toBe('71%')
    expect(percentual(null)).toBe('—')
  })

  it('calcula a parte do total com uma casa e devolve null sem base', () => {
    expect(parteDoTotal(412, 1284)).toBe(32.1)
    expect(parteDoTotal(1, 3)).toBe(33.3)
    expect(parteDoTotal(5, 0)).toBeNull()
  })

  it('mostra datas como vieram, sem mexer no fuso', () => {
    expect(dataCurta('2026-11-14')).toBe('14/11/2026')
    expect(dataCurta('2026-11-14T23:59:00Z')).toBe('14/11/2026')
    expect(diaMes('2026-11-14')).toBe('14/11')
    expect(dataCurta('sem data')).toBe('sem data')
  })

  it('diz há quanto tempo algo aconteceu', () => {
    const base = '2026-11-24T12:00:00Z'
    const depois = (ms: number) => Date.parse(base) + ms
    const minuto = 60_000
    expect(quando(base, depois(30_000))).toBe('agora')
    expect(quando(base, depois(12 * minuto))).toBe('há 12 min')
    expect(quando(base, depois(3 * 60 * minuto))).toBe('há 3 h')
    expect(quando(base, depois(24 * 60 * minuto))).toBe('há 1 dia')
    expect(quando(base, depois(2 * 24 * 60 * minuto))).toBe('há 2 dias')
    expect(quando(base, depois(10 * 24 * 60 * minuto))).toBe('24/11/2026')
    expect(quando('lixo')).toBe('lixo')
  })

  it('dá nome às áreas', () => {
    expect(nomeDaArea('musica')).toBe('Música')
    expect(nomeDaArea('filmes')).toBe('Filmes')
  })

  it('usa o texto do backend nos erros de regra e um aviso próprio nos outros', () => {
    expect(mensagemDeErro(new ApiError(409, 'Você não pode banir a própria conta'), 'padrão')).toBe(
      'Você não pode banir a própria conta',
    )
    expect(mensagemDeErro(new ApiError(404, 'Conta não encontrada'), 'padrão')).toBe('Conta não encontrada')
    expect(mensagemDeErro(new ApiError(401, 'x'), 'padrão')).toBe('Sua sessão acabou. Entre de novo.')
    expect(mensagemDeErro(new ApiError(403, 'x'), 'padrão')).toBe('Esta conta não tem acesso ao painel de admin.')
    expect(mensagemDeErro(new ApiError(500, 'x'), 'padrão')).toBe('padrão')
    expect(mensagemDeErro(new Error('rede'), 'padrão')).toBe('padrão')
  })
})
