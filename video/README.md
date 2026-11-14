# Vídeo de apresentação

Vídeo vertical (1080×1920, 34 segundos, narrado) que apresenta o BeShuffle, feito em código com
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

## Narração

A voz é a **Lucas – Social Media Voice** do ElevenLabs (português do Brasil), no modelo `eleven_v4`, que aceita
marcações de emoção como `[excited]` e `[mischievously]`. "BeShuffle" é falado em inglês: no texto enviado à voz
ele vai como pronúncia fonética, `/biː ˈʃʌfəl/`, e a legenda continua escrevendo "BeShuffle".

- `narracao.json`: a voz usada e, por cena, a legenda e o texto exato enviado ao ElevenLabs.
- `public/voz/<cena>.mp3`: os áudios gerados.
- `src/narracao.json`: a duração de cada áudio. Cada cena dura pelo menos o tempo da sua fala, e as palavras
  da legenda acompanham o ritmo da voz.

Para trocar uma fala: gere o áudio de novo no ElevenLabs com o texto de `narracao.json`, salve por cima do MP3
da cena e atualize os `segundos` em `src/narracao.json`.

## Música

A trilha é original, composta em código por `scripts/compor-trilha.py`: pop leve com violão dedilhado, baixo,
bumbo, palmas e chimbal sobre C–G–Am–F. Não usa música de terceiros, então pode ir para qualquer rede social.

- O andamento é calculado para que 16 compassos durem exatamente o vídeo, e as notas que soam no fim continuam
  no começo: a música emenda sem corte no loop.
- No vídeo ela fica baixa (18%) e abaixa mais (7%) enquanto o narrador fala (ducking, em
  `src/BeShuffleVideo.tsx`).
- Se a duração do vídeo mudar, gere de novo com os segundos novos: `python scripts/compor-trilha.py 34.4`.

## Imagens de divulgação

O mesmo projeto gera duas imagens estáticas com o Radinho, em `docs/imagens/`:

- `beshuffle-banner.png` (1400×940, a proporção do diagrama da arquitetura): a placa do BeShuffle com o Radinho
  apoiado nela. É o banner do README.
- `beshuffle-radinho.png` (1080×1350, formato de feed): só a logo e o Radinho acenando, para divulgação.

Para gerar de novo: `npm run imagens` (composições `Banner` e `Retrato`, em `src/divulgacao/`).

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
