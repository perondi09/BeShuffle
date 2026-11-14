# Vídeo de apresentação

Vídeo vertical (1080×1920, 34 segundos) que apresenta o BeShuffle, feito em código com
[Remotion](https://www.remotion.dev/). Usa as cores, as fontes e o vinil do site, e um mascote próprio: o
Radinho, um rádio retrô de desenho animado.

## Roteiro (em loop)

O último quadro é igual ao primeiro e a última fala ("Entra lá e me conta:") emenda na pergunta do começo. Ao
recomeçar, o vídeo não tem corte, então quem assiste tende a ver de novo.

| Cena | O que mostra |
| --- | --- |
| Gancho | "Qual disco você vai ouvir hoje?" |
| Sempre o mesmo? | A mesma playlist em repetição, com o carimbo "DE NOVO?" |
| Conheça | O BeShuffle: uma jukebox que escolhe por você |
| Passo 1 | O Álbum do Dia girando na vitrola |
| Passo 2 | Escolher estilos e sortear, com o carimbo "SORTEADO!" |
| Passo 3 | Dar nota com os vinis e ver o que a galera achou |
| Pra quem é? | Quem quer sair da bolha, descobrir artista, ter trilha para estudar ou nunca sabe o que ouvir |
| Chamada | beshuffle.com.br, e tudo sai de cena até o palco ficar vazio |

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
