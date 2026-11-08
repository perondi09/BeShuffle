# BeShuffle (frontend)

Site do BeShuffle feito com React 19, TypeScript e Vite. Por enquanto ele mostra o Álbum do Dia e tem um botão
para sortear um álbum, usando a API em `backend/`. O visual definitivo (`docs/design/index.html`) entra na fase 5.

## Requisitos

- Node.js 22 ou mais recente
- A API rodando em `http://localhost:8080` (ver `backend/README.md`)

## Rodar

Todos os comandos rodam dentro de `frontend/`:

```bash
npm install
npm run dev
```

Em desenvolvimento, o Vite repassa `/api` para `http://localhost:8080`, então a API não precisa de CORS. Para usar
outra API, defina `VITE_API_PROXY` (exemplo: `VITE_API_PROXY=http://localhost:8099 npm run dev`).

O frontend não guarda chave nem senha: as chamadas ao Spotify são feitas pela API.

## Scripts

| Comando | O que faz |
| --- | --- |
| `npm run dev` | Servidor de desenvolvimento com recarga automática |
| `npm test` | Testes com Vitest e Testing Library |
| `npm run lint` | ESLint |
| `npm run build` | Checa os tipos e gera o build de produção em `dist/` |

## Estrutura

| Caminho | Papel |
| --- | --- |
| `src/api/albums.ts` | Chamadas à API e o tipo `Album` |
| `src/App.tsx` | Tela inicial: Álbum do Dia e sorteio |
| `src/App.test.tsx` | Testes da tela inicial |
| `vite.config.ts` | Proxy `/api` e configuração do Vitest |
