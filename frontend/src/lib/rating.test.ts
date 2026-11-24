import { describe, expect, it } from 'vitest'
import { formatRating, splitRating } from './rating'

describe('formatRating', () => {
  it('inteiro sem casa decimal, meia nota com vírgula', () => {
    expect(formatRating(4)).toBe('4')
    expect(formatRating(3.5)).toBe('3,5')
    expect(formatRating(0.5)).toBe('0,5')
  })
})

describe('splitRating', () => {
  it('separa discos cheios e meio disco', () => {
    expect(splitRating(4)).toEqual({ full: 4, half: false })
    expect(splitRating(2.5)).toEqual({ full: 2, half: true })
    expect(splitRating(0.5)).toEqual({ full: 0, half: true })
    expect(splitRating(5)).toEqual({ full: 5, half: false })
  })
})
