import { describe, expect, it } from 'vitest'
import { diaPorExtenso } from './date'

describe('diaPorExtenso', () => {
  it('escreve o dia da semana, o dia e o mês, sem mudar o dia por causa do fuso', () => {
    expect(diaPorExtenso('2026-11-14')).toBe('sábado, 14 de novembro')
    expect(diaPorExtenso('2026-01-01')).toBe('quinta-feira, 1 de janeiro')
  })

  it('devolve o texto como veio quando não é uma data', () => {
    expect(diaPorExtenso('hoje')).toBe('hoje')
  })
})
