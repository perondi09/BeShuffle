import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { RatingDiscs } from './RatingDiscs'

describe('RatingDiscs', () => {
  it('cada disco tem duas metades: a esquerda dá meia nota, a direita a nota inteira', () => {
    const onChange = vi.fn()
    render(<RatingDiscs value={0} onChange={onChange} />)

    fireEvent.click(screen.getByRole('radio', { name: '2,5 de 5' }))
    fireEvent.click(screen.getByRole('radio', { name: '3 de 5' }))
    fireEvent.click(screen.getByRole('radio', { name: '0,5 de 5' }))

    expect(onChange.mock.calls.map(([value]) => value)).toEqual([2.5, 3, 0.5])
  })

  it('marca só a nota atual como escolhida', () => {
    render(<RatingDiscs value={3.5} onChange={() => {}} />)

    expect(screen.getByRole('radio', { name: '3,5 de 5' })).toHaveAttribute('aria-checked', 'true')
    expect(screen.getByRole('radio', { name: '4 de 5' })).toHaveAttribute('aria-checked', 'false')
    expect(screen.getAllByRole('radio')).toHaveLength(10)
  })
})
