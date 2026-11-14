"""Compõe a trilha de fundo do vídeo: pop leve e animado, original (sem direitos de terceiros).

O andamento é calculado para que 16 compassos durem exatamente o vídeo, e as notas que ainda soam no fim são
somadas ao começo: a música emenda sem corte quando o vídeo recomeça (loop).

Uso: python scripts/compor-trilha.py <segundos do vídeo>   (gera public/musica/trilha.wav)
"""
import sys
import wave
from pathlib import Path

import numpy as np

SR = 44100
DURACAO = float(sys.argv[1]) if len(sys.argv) > 1 else 34.4
COMPASSOS = 16
BATIDA = DURACAO / (COMPASSOS * 4)  # segundos por tempo
N = int(round(DURACAO * SR))
mix = np.zeros(N)
rng = np.random.default_rng(7)


def hz(nota: int) -> float:
    """Nota MIDI para frequência."""
    return 440.0 * 2 ** ((nota - 69) / 12)


def somar(som: np.ndarray, inicio_s: float, ganho: float) -> None:
    """Soma um som na mixagem; o que passa do fim volta para o começo (loop sem corte)."""
    i = int(round(inicio_s * SR)) % N
    idx = (np.arange(len(som)) + i) % N
    np.add.at(mix, idx, som * ganho)


def dedilhado(nota: int, dur: float) -> np.ndarray:
    """Corda dedilhada: harmônicos com decaimento, os agudos morrem mais rápido."""
    t = np.arange(int(dur * SR)) / SR
    f = hz(nota)
    som = sum((0.6 ** k) * np.sin(2 * np.pi * f * (k + 1) * t) * np.exp(-t * (3.5 + 2.5 * k)) for k in range(6))
    ataque = np.minimum(1, t / 0.004)
    return som * ataque


def pad(notas: list[int], dur: float) -> np.ndarray:
    """Acorde macio de fundo, com entrada e saída suaves."""
    t = np.arange(int(dur * SR)) / SR
    som = sum(np.sin(2 * np.pi * hz(n) * t) + 0.3 * np.sin(2 * np.pi * hz(n) * 2.003 * t) for n in notas)
    envelope = np.minimum(1, t / 0.25) * np.minimum(1, (dur - t) / 0.3)
    return som * envelope / len(notas)


def baixo(nota: int, dur: float) -> np.ndarray:
    t = np.arange(int(dur * SR)) / SR
    f = hz(nota)
    som = np.sin(2 * np.pi * f * t) + 0.35 * np.sin(2 * np.pi * 2 * f * t)
    return som * np.exp(-t * 3.0) * np.minimum(1, t / 0.006)


def bumbo() -> np.ndarray:
    t = np.arange(int(0.35 * SR)) / SR
    freq = 50 + 110 * np.exp(-t * 28)
    return np.sin(2 * np.pi * np.cumsum(freq) / SR) * np.exp(-t * 9)


def palma() -> np.ndarray:
    t = np.arange(int(0.2 * SR)) / SR
    ruido = rng.standard_normal(len(t))
    ruido = ruido - np.convolve(ruido, np.ones(8) / 8, mode='same')  # tira o grave
    return ruido * (np.exp(-t * 28) + 0.4 * np.exp(-((t - 0.012) * 300) ** 2))


def chimbal() -> np.ndarray:
    t = np.arange(int(0.06 * SR)) / SR
    ruido = rng.standard_normal(len(t))
    ruido = np.diff(ruido, prepend=0)  # bem agudo
    return ruido * np.exp(-t * 70)


# C - G - Am - F (notas MIDI: fundamental, terça, quinta)
ACORDES = [(48, [60, 64, 67]), (43, [59, 62, 67]), (45, [60, 64, 69]), (41, [60, 65, 69])]
ARPEJO = [0, 2, 1, 2, 0, 2, 1, 2]  # índices no acorde, em colcheias (mais a oitava em cima)

for compasso in range(COMPASSOS):
    raiz, notas = ACORDES[compasso % 4]
    t0 = compasso * 4 * BATIDA
    somar(pad([n - 12 for n in notas], 4 * BATIDA + 0.3), t0, 0.10)
    for passo, i in enumerate(ARPEJO):
        nota = notas[i] + (12 if passo in (3, 6) else 0)
        somar(dedilhado(nota, 1.2), t0 + passo * BATIDA / 2, 0.16 if passo % 2 == 0 else 0.12)
    for tempo, dur in ((0, 1.0), (1.5, 0.5), (2, 1.0), (3.5, 0.5)):
        somar(baixo(raiz, dur * BATIDA + 0.1), t0 + tempo * BATIDA, 0.30)
    for tempo in (0, 2, 2.5):
        somar(bumbo(), t0 + tempo * BATIDA, 0.55)
    for tempo in (1, 3):
        somar(palma(), t0 + tempo * BATIDA, 0.16)
    for colcheia in range(8):
        somar(chimbal(), t0 + colcheia * BATIDA / 2, 0.05 if colcheia % 2 == 0 else 0.08)

mix = np.tanh(mix * 1.2)
mix = mix / np.max(np.abs(mix)) * 10 ** (-3 / 20)  # pico em -3 dBFS

saida = Path(__file__).resolve().parent.parent / 'public' / 'musica' / 'trilha.wav'
saida.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(saida), 'wb') as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes((mix * 32767).astype('<i2').tobytes())
print(f'{saida.name}: {DURACAO:.2f}s, {60 / BATIDA:.1f} BPM, {COMPASSOS} compassos')
