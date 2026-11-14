# Vídeo de apresentação

Vídeo vertical (1080×1920, cerca de 37 segundos) que apresenta o BeShuffle, feito em código com
[Remotion](https://www.remotion.dev/). Usa as cores, as fontes e o vinil do site, e um mascote próprio: o
Radinho, um rádio retrô de desenho animado.

## Roteiro

| Cena | O que mostra |
| --- | --- |
| Gancho | "Qual disco você vai ouvir hoje?" |
| O problema | A mesma playlist em repetição, com o carimbo "DE NOVO?" |
| A ideia | O BeShuffle entra em cena |
| Passo 1 | O Álbum do Dia girando na vitrola |
| Passo 2 | Escolher estilos e sortear, com o carimbo "SORTEADO!" |
| Passo 3 | Dar nota com os vinis e comentar |
| Por trás do palco | Spring Boot, React, AWS, Terraform e CI/CD |
| Chamada | beshuffle.com.br |

As capas de disco são inventadas (formas e cores), sem capas reais com direitos autorais.

## Comandos

```bash
cd video
npm install
npm run studio      # edita e assiste no navegador
npm run render      # gera out/beshuffle.mp4
npm run quadro -- --frame=95   # um quadro em out/quadro.png
```

## Onde mexer

- `src/BeShuffleVideo.tsx`: ordem e duração das cenas.
- `src/cenas/`: uma cena por arquivo, com o texto da legenda.
- `src/componentes/`: mascote, etiqueta, carimbo, legenda e os cartões.
- `src/tema.ts`: cores e fontes (as mesmas de `frontend/src/styles/tokens.css`).
