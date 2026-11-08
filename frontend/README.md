# BeShuffle (frontend)

Site do BeShuffle feito com React 19, TypeScript, Vite e React Router, no visual de `docs/design/index.html`.

Telas:

| Rota | Tela |
| --- | --- |
| `/` | Home: Álbum do Dia, sorteio (visitante tem 1 ficha por dia), sorteados de hoje e nota (trancados para visitante) |
| `/entrar` | Login |
| `/cadastro` | Cadastro |
| qualquer outra | Página 404 |

Login e cadastro já validam os campos e chamam `POST /api/auth/login` e `POST /api/auth/register`, mas a API
ainda não tem essas rotas: até lá a tela avisa que o recurso não está disponível. A ficha diária do visitante fica
no `localStorage` só para a interface; a API ainda precisa garantir a regra de 1 sorteio por dia.

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
| `src/styles/tokens.css` | **Única fonte de estilo**: cores, fontes, bordas, sombras, raios, espaços e fundo |
| `src/styles/global.css` | Reset, fundo de bolinhas, foco e a classe `.display` (fonte de títulos) |
| `src/components/ui/` | Componentes base: `Button`, `Card`, `Sticker`, `Tag`, `TextField`, `LockedOverlay`, `VinylDisc` |
| `src/components/layout/` | `AppLayout`, `Header`, `Brand`, `MobileNav` |
| `src/features/draw/` | Sorteio: palco, vitrola, detalhes do álbum, ficha diária |
| `src/features/community/` | Cartões "Sorteados de hoje" e "Sua nota" |
| `src/features/auth/` | Moldura, formulários e validação de login/cadastro |
| `src/pages/` | Uma página por rota, só compondo features |
| `src/api/` | `http.ts` (fetch + `ApiError`), `albums.ts`, `auth.ts` |
| `src/AppRoutes.tsx` | Rotas (o `App.tsx` só põe o `BrowserRouter`) |
| `src/test/` | Setup do Vitest e `renderAt`/`mockApi` para os testes |
| `vite.config.ts` | Proxy `/api` e configuração do Vitest |

## Padrões de estilo

- Cada componente tem uma pasta (ou arquivo) com `Componente.tsx` + `Componente.module.css` (CSS Modules).
- CSS de componente só usa `var(--…)` de `tokens.css`. Cor, borda ou fonte nova entra primeiro nos tokens.
- Variações viram props (`<Button variant="tomate" size="g">`), não classes soltas.
- Todas as telas usam o mesmo fundo (`--fundo-pagina`), as fontes Bagel Fat One (títulos, classe `.display`) e
  Fredoka (texto), borda `--borda` e sombra chapada `--sombra*`.
- Textos da interface e comentários em português.
