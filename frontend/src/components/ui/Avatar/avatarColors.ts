/** As cores de avatar do design system; o backend só aceita estas (AppUser.AVATARES). */
export const AVATAR_COLORS = ['rosa', 'menta', 'gema', 'lilas'] as const
export type AvatarColor = (typeof AVATAR_COLORS)[number]

/** Cor vinda da API; valor desconhecido ou ausente (conta antiga) cai no rosa. */
export function avatarColor(value?: string | null): AvatarColor {
  return AVATAR_COLORS.find((color) => color === value) ?? 'rosa'
}
