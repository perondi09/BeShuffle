import { spring, useCurrentFrame, useVideoConfig } from 'remotion'

/** Mola de 0 a 1 que começa `atraso` segundos depois do início da cena. */
export function useMola(atraso = 0, config: { damping?: number; stiffness?: number; mass?: number } = {}) {
  const frame = useCurrentFrame()
  const { fps } = useVideoConfig()
  return spring({ frame: frame - atraso * fps, fps, config: { damping: 12, stiffness: 120, ...config } })
}

/** Mola calculada só com números (dá para usar dentro de map): 0 antes do atraso, quica e assenta em 1. */
export function molaManual(frame: number, fps: number, atraso: number): number {
  if (frame < atraso * fps) return 0
  const t = (frame - atraso * fps) / fps
  return 1 - Math.exp(-7 * t) * Math.cos(10 * t)
}
