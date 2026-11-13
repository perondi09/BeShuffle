# BeShuffle (frontend)

Site do BeShuffle feito com React 19, TypeScript, Vite e React Router, no visual de `docs/design/index.html`.

Telas:

| Rota | Tela |
| --- | --- |
| `/` | Home: Álbum do Dia, sorteio (1 ficha por dia para visitante, 10 e escolha de estilos para conta logada), sorteados de hoje e nota do Álbum do Dia (trancados para visitante) |
| `/entrar` | Login |
| `/cadastro` | Cadastro |
| qualquer outra | Página 404 |

Login e cadastro validam os campos e chamam `POST /api/auth/login` e `POST /api/auth/register`. A sessão fica
num cookie HttpOnly da API; o `SessionProvider` (`src/features/session/`) lê `GET /api/me` ao abrir o site, e o
topo mostra "Oi, nome" e o botão Sair (`POST /api/auth/logout`) para quem está logado. Todo `POST` manda o
header `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN` (proteção CSRF, em `src/api/http.ts`); o `PUT` da nota também.

Sorteio e comunidade (tudo grátis):

- As fichas do dia vêm da API (`GET /api/draws/allowance`), que é quem conta: visitante tem 1, conta logada tem 10.
  O `AllowanceProvider` (`src/features/draw/`) lê o saldo ao abrir o site e de novo quando alguém entra ou sai.
- `POST /api/draws` sorteia e devolve o álbum junto com o saldo novo. Conta logada pode mandar estilos
  (`{"genres":["rock","jazz"]}`, lista em `GET /api/genres`); a escolha fica salva no `localStorage` só por conveniência.
- Logado, "Sorteados de hoje" lê `GET /api/draws/today` e "Sua nota" lê e grava `GET`/`PUT /api/reviews/today`.
  Notas e comentários são só para o Álbum do Dia.

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
| `src/features/draw/` | Sorteio: palco, vitrola, detalhes do álbum, fichas do dia (`AllowanceProvider`) e estilos (`StylePicker`) |
| `src/features/community/` | Cartões "Sorteados de hoje" e "Sua nota" (trancados para visitante) |
| `src/features/auth/` | Moldura, formulários e validação de login/cadastro |
| `src/pages/` | Uma página por rota, só compondo features |
| `src/api/` | `http.ts` (fetch + `ApiError` com a mensagem da API), `albums.ts`, `auth.ts`, `draws.ts`, `reviews.ts` |
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
