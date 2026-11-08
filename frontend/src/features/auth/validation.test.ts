import { describe, expect, it } from 'vitest'
import { validateLogin, validateRegister } from './validation'

describe('validateLogin', () => {
  it('aceita e-mail e senha preenchidos', () => {
    expect(validateLogin({ email: 'a@b.co', password: 'x' })).toEqual({})
  })

  it('recusa e-mail sem domínio', () => {
    expect(validateLogin({ email: 'pessoa@', password: 'x' }).email).toBe('Esse e-mail não parece válido.')
  })
})

describe('validateRegister', () => {
  const valid = { name: 'Gui', email: 'a@b.co', password: '12345678', confirm: '12345678' }

  it('aceita um cadastro completo com senha de exatamente 8 caracteres', () => {
    expect(validateRegister(valid)).toEqual({})
  })

  it('recusa nome com menos de 2 letras, ignorando espaços', () => {
    expect(validateRegister({ ...valid, name: ' a ' }).name).toBeDefined()
  })

  it('recusa senha com 7 caracteres', () => {
    expect(validateRegister({ ...valid, password: '1234567', confirm: '1234567' }).password).toBeDefined()
  })
})
