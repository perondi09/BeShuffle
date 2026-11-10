# BeShuffle — Contexto do projeto

> Mapeamento feito em 08/11/2026 sobre `master` (`bc27953`). Nenhum código foi alterado.

## 1. Visão do produto

Site onde a pessoa **sorteia um álbum do Spotify** e descobre música nova.

| Recurso | Visitante (sem conta) | Usuário cadastrado |
| --- | --- | --- |
| Sortear álbum aleatório | **1 vez por dia** | Ilimitado (ou cota maior) |
| Ver o "Álbum do Dia" | Sim | Sim |
| Ver os sorteados do dia (histórico) | Não | Sim |
| Dar nota ao álbum | Não | Sim |
| Comentar o álbum | Não | Sim |

Problema de produto relatado: o sorteio traz álbuns que quase ninguém ouviria
(ex.: álbum infantil mexicano). Objetivo: sortear **álbuns que pessoas realmente escutam**, com filtros.

Stack a manter: **Java 21 + Spring Boot + PostgreSQL + Docker**, frontend separado.

---

## 2. Estado atual do repositório

> Atualizado depois da fase 1 e da separação em `backend/`, `frontend/` e `infra/`. O retrato original,
> de antes das mudanças, está no histórico do git (`git show 4c41e9d:docs/CONTEXTO.md`).

### Organização (monorepo)

```
backend/     API Spring Boot 4 / Java 21 (Maven Wrapper próprio)        → backend/README.md
frontend/    site React 19 + TypeScript + Vite                          → frontend/README.md
infra/       docker-compose, Dockerfiles, nginx, .env.example           → infra/README.md
.github/     workflow de CI (o GitHub só lê aqui)
docs/        este contexto e a proposta visual (docs/design/index.html)
```

Chaves e senhas ficam só em `infra/.env` (gitignored). O compose e a API (`spring.config.import`) leem esse
arquivo, e nenhum segredo tem valor padrão no código.

### Stack
- **Backend:**
  - Spring Boot **4.0.3** (webmvc), Java 21, Jackson 3.
  - `RestClient` para a Web API do Spotify.
  - Spring Data JPA + PostgreSQL 16, com Flyway (`ddl-auto=validate`).
  - Testes com H2 e `MockRestServiceServer`.
- **Frontend:** React 19, TypeScript, Vite 6, Vitest e Testing Library.
- **Infra:**
  - Postgres 16, com a porta publicada só em `127.0.0.1`.
  - Imagem da API: Maven → `eclipse-temurin:21-jre`, rodando sem root.
  - Imagem do site: Node 22 → nginx 1.27, que repassa `/api` para a API.
- **CI** (GitHub Actions), três jobs:
  - backend: `mvnw verify`;
  - frontend: lint, testes e build;
  - build das duas imagens Docker.

### Backend (`backend/src/main/java/perondi/BeShuffle/`)

```
├── config/        DailyAlbumScheduler (cron 00:00 em beshuffle.timezone), TimeConfig (Clock),
│                  SorteioProperties (beshuffle.sorteio.*) + SorteioConfig
├── controller/    AlbumController: GET /api/albums/random, GET /api/albums/daily
├── entity/        Album, AlbumImage, Artist (DTOs do Spotify) e DailyAlbum (@Entity daily_albums)
├── exceptions/    Spotify*/Validation + GlobalExceptionHandler (sem repassar detalhes do Spotify)
├── repository/    DailyAlbumRepository
├── services/      AlbumPicker (interface) + PopularPorAnoAlbumPicker, FiltroDeAlbum, AlbumService, DailyAlbumService
└── spotify/       SpotifyClient (única porta para a API), SpotifyTokenProvider (token em cache), SpotifyConfig
```

### Endpoints
| Método | Rota | O que faz |
| --- | --- | --- |
| GET | `/api/albums/random` | sorteia id → busca álbum completo → retorna JSON do Spotify |
| GET | `/api/albums/daily` | retorna álbum do dia do banco; se não houver, gera na hora |
| GET | `/actuator/health` | health check |

### Frontend
- Visual do guia `docs/design/index.html` (cartunizado, celular e desktop), com tokens e CSS Modules.
- Home com Álbum do Dia, sorteio (ficha diária do visitante, só na interface por enquanto), cards da comunidade
  trancados para visitante; telas `/entrar` e `/cadastro` prontas, esperando `/api/auth/*` (fase 4).
- O frontend vanilla antigo (`src/main/resources/static`) não existe mais.

### Fluxo de branches

```
backend ─┐
frontend ┼─► PR ─► test ─► (CI verde + testes) ─► PR ─► master
devops ──┤
docs ────┘
```

- Cada área tem sua branch: `backend`, `frontend`, `devops` (infra e CI) e `docs`. Trabalhos maiores podem usar
  branches curtas a partir delas (por exemplo `backend/filtros-sorteio`).
- Toda mudança entra por PR na `test`. Só depois do CI verde e dos testes da `test` abre-se o PR `test → master`.
- Commits seguem Conventional Commits (`feat`, `fix`, `refactor`, `ci`, `docs`, …), descritivos e sem trailer de coautoria.

## 3. Histórico (commits e branches)

Remoto: `github.com/perondi09/BeShuffle`. As branches antigas `create-ramdomalbum`, `create-repositories`,
`security`, `visual-changes` e `refactor/fase1-base` estavam todas mergeadas e foram apagadas em 08/11/2026;
hoje existem só `master`, `test` e as branches de área (`backend`, `frontend`, `devops`, `docs`).

| Fase | Commits | O que aconteceu |
| --- | --- | --- |
| 1. Base | `93bd18a` → `156c981` | Token Spotify, Feign de álbum, entidade, esconder credenciais |
| 2. PRs #1–#10 | `8fd4f62` → `825a76d` | Pastas, JPA/DB, DTOs, randomizer (`6639aac`, `a2e974e`), Dockerfile, visual, exceptions/testes |
| 3. Docker + front | `13b1c3c` → `cf194bc` | Dockerização, botões "novo álbum", front React |
| 4. Álbum do dia | `65c61c4` → `b266f16` | Conexão DB, scheduler, lógica do álbum do dia |
| 5. Hardening (14/08/2026) | `b66f16f` → `26f49bc` | Remove front React, endurece Spotify, testes, Docker p/ AWS, logs, Postgres 16, front vanilla com toggle |
| 6. Último | `bc27953` (23/09/2026) | Ajuste no `application.properties` |

### ⚠️ Sistema de cadastro
**Não existe nenhum código de cadastro/login de usuário em nenhum commit ou branch deste repositório**
(busca por user/register/password/security). Os problemas citados com o cadastro devem estar em código
não commitado, em outra pasta, ou no repositório do frontend. → Precisamos localizar esse código.

---

## 4. Análise do sorteio (`SpotifyRandomAlbumService`)

> **Fase 2 concluída (08/11/2026).** O sorteio agora é o `PopularPorAnoAlbumPicker` (ver `backend/README.md`):
> ano aleatório entre 1960 e o atual, página aleatória entre os 50 primeiros resultados de `year:ANO` no mercado
> BR, filtro `FiltroDeAlbum` (só `album`, 6–40 faixas, sem "Various Artists", sem termos bloqueados) e sorteio
> entre os aprovados. Configurável em `beshuffle.sorteio.*`.
>
> O que o app (Development Mode) recebe do Spotify, testado em 08/11/2026:
>
> | Recurso | Resultado |
> | --- | --- |
> | `popularity`, `genres`, `followers`, `label` (álbum e artista) | não vêm |
> | `GET /v1/albums?ids=`, top tracks, new-releases, categorias | 403 |
> | Playlists editoriais (Top 50) e faixas de playlists | 404 / 403 |
> | `recommendations` | 404 |
> | Busca de artista por `genre:` | 0 resultados |
> | Busca de álbum `year:ANO` + `market=BR` | **funciona**, ordenada por relevância; `limit` máximo 10 |
>
> Por isso as opções 1 (busca por faixas com `genre:`) e o filtro por `popularity` abaixo não são viáveis
> hoje. O pool curado (opção 2, Last.fm) continua sendo o próximo passo se quisermos filtrar por gênero.
> O texto abaixo é a análise original, de antes da fase 1.

Como funcionava antes:
1. Pega um token novo (`client_credentials`).
2. Monta a busca `q=<1 caractere aleatório a-z0-9>&type=album&limit=5&offset=<0..949>`.
3. Pega o primeiro item com `album_type == "album"`; até 5 tentativas.
4. `AlbumService` pede **outro** token e busca o álbum completo.

Por que vem "álbum infantil mexicano":
- Busca por um único caractere em offset alto = cauda longa do catálogo. A busca do Spotify ordena por
  relevância/popularidade; em offset 500–950 sobra o que quase ninguém ouve.
- Sem `market` → aparecem lançamentos de qualquer país.
- Sem filtro de gênero, ano, popularidade, nem lista de exclusão (infantil, karaokê, ninar, tributo, white noise…).

Outros pontos:
- 2 requisições de token por sorteio (sem cache; token vale 1h) → risco de 429.
- Dois clientes HTTP diferentes para o Spotify (Feign e RestTemplate).
- Exceções viram `null` silenciosamente → difícil diagnosticar.

### Proposta de filtros (do mais rápido ao mais robusto)
1. **Ajuste rápido**: `market=BR`, offset baixo (0–200), buscar por **faixas** com `genre:"rock" year:2000-2025`
   (o filtro `genre:` funciona em artistas/faixas, não em álbuns) e usar `track.album`;
   descartar `compilation`, álbuns com < 6 faixas e nomes/artistas numa blocklist.
2. **Recomendado — pool curado no banco**: tabela `album_candidates` alimentada por job semanal a partir de
   fontes de escuta real (Last.fm `tag.getTopAlbums` / `chart.getTopArtists` por gênero) e resolvida para IDs do Spotify.
   O sorteio escolhe dessa tabela (rápido, sem 429, sem repetição recente, filtrável por gênero/década).
3. **Personalizado** (futuro): usuário marca gêneros favoritos no cadastro e o sorteio pondera por eles.

> Atenção: o Spotify restringiu a Web API para apps novos (ex.: `recommendations`, `related-artists`,
> `audio-features` e playlists editoriais deixaram de funcionar em nov/2024) e mudou regras do modo de
> desenvolvimento depois disso. Confirme na documentação atual quais campos (ex.: `popularity`) seu app recebe
> antes de depender deles.

---

## 5. Problemas técnicos encontrados

| # | Gravidade | Onde | Problema |
| --- | --- | --- | --- |
| 1 | Alta | README, compose, `.env.example` | Senha padrão do Postgres commitada (`Senhalol.123`) e repetida como default. Trocar e nunca usar em produção. |
| 2 | Alta | `DailyAlbum.spotifyAlbumId unique` | Se o sorteio repetir um álbum já usado, o `save` lança violação de unicidade → 500 e dia sem álbum. |
| 3 | Alta | `DailyAlbumService` + scheduler | `LocalDate.now()` no fuso do container (UTC): o "dia" vira às 21h no Brasil. Fallback e scheduler podem gerar o mesmo dia em paralelo. |
| 4 | Média | `ApplicationConfig.dataSource` | DataSource manual com fallback de DNS substitui a autoconfiguração do Boot; esconde erros de config em produção. Usar profiles (`local`, `prod`). |
| 5 | Média | Jackson | Boot 4 usa Jackson 3 (`tools.jackson`); o projeto cria um `ObjectMapper` Jackson 2 e `LoginResponse` usa anotações do Jackson 3. Unificar. |
| 6 | Média | `ddl-auto=update` | Sem migrations. Adotar Flyway antes de criar tabelas de usuário/nota/comentário. |
| 7 | Média | Segurança/API | Sem Spring Security, sem CORS (front está em outro repo), sem rate limit — o "1 sorteio por dia" precisa ser garantido no backend. |
| 8 | Baixa | `entity/Album` etc. | DTOs do Spotify no pacote `entity`; o JSON do Spotify sai direto para o front (acoplamento). |
| 9 | Baixa | Logs | `DEBUG` fixo para o pacote; arquivo em `/tmp` no container. |
| 10 | Baixa | README | Diz Postgres 18; compose usa 16. Diz "frontend separado" mas há front em `/static`. |

---

## 6. Modelo de dados sugerido (para a visão)

```
users            id, email (único), password_hash, display_name, created_at
user_genres      user_id, genre                      -- preferências para o sorteio
album_candidates spotify_album_id (pk), name, artist, image_url, genres[], year, source, score, last_drawn_at
daily_album      date (pk, fuso America/Sao_Paulo), spotify_album_id
draws            id, user_id NULL, anon_id NULL, spotify_album_id, drawn_at   -- histórico + cota diária
reviews          id, user_id, spotify_album_id, rating (1–5, meio ponto), comment, created_at, updated_at
                 UNIQUE(user_id, spotify_album_id)
```
Visitante: `anon_id` em cookie HttpOnly + hash de IP; a regra "1 por dia" é verificada em `draws`.

---

## 7. Caminho até produção (sugestão)

> **Fase 1 concluída** na branch `refactor/fase1-base`. Resolvidos: problemas 1 a 6 da §5, mais a remoção
> do OpenFeign, o cache de token com renovação após 401 e as mensagens de erro que não expõem mais a resposta do Spotify.
> Também feitos na fase 1:
> - todas as chaves e senhas vêm só de `infra/.env`, lido pelo compose e pela aplicação
>   (`spring.config.import`), sem valor padrão no código;
> - o Postgres escuta só em `127.0.0.1:5435`;
> - CI no GitHub Actions (`.github/workflows/ci.yml`) roda `mvnw verify` com Java 21 em cada PR.
>
> **Reorganização concluída** antes da fase 2:
> - monorepo com `backend/`, `frontend/` (React + TypeScript + Vite, tela inicial provisória) e `infra/`
>   (compose com Postgres, API e site; Dockerfiles; nginx);
> - CI separado para backend, frontend e imagens Docker;
> - fluxo de branches `backend`/`frontend`/`devops`/`docs` → `test` → `master` (ver §2).
>
> **Fase 2 concluída** (sorteio de álbuns populares por ano, ver §4), mais limpezas:
> - `dotenv-java` removido (o `.env` é lido só pelo `spring.config.import`);
> - branches antigas apagadas;
> - frontend vanilla antigo (`backend/src/main/resources/static`) apagado; a API não serve mais página em `/`;
> - senha antiga do Postgres local trocada (ela continua no histórico do git, então nunca deve ser reusada).
>
> Ficou para a fase 3 (deploy):
> - o token é renovado dentro de um lock que espera a chamada HTTP;
> - os `RestClient` não usam o `RestClient.Builder` do Boot (sem métricas);
> - `baseline-on-migrate` deve sair depois do primeiro deploy.

1. Localizar/commitar o código de cadastro; criar branch `feature/auth`.
2. Refatoração sem mudar comportamento (`/ecc:orch-refine-code`): unificar cliente Spotify, cache de token,
   extrair estratégia de sorteio (`AlbumPicker`), separar DTOs, profiles no lugar do DataSource manual, fuso fixo.
3. Flyway + tabelas de §6.
4. Spring Security: e-mail/senha (BCrypt) com sessão em cookie HttpOnly ou JWT; CORS para o domínio do front.
5. Sorteio com pool curado + filtros; cota diária de visitante.
6. Endpoints: `POST /api/auth/register|login|logout`, `GET /api/me`, `GET /api/draws/today`,
   `POST/PUT /api/albums/{id}/review`, `GET /api/albums/{id}/reviews`.
7. Novo frontend (proposta visual em `docs/design/`).
8. Deploy: API em container (AWS App Runner/ECS, Render ou Fly), Postgres gerenciado (RDS/Neon), front em
   Vercel/Netlify, segredos no provedor, `SPRING_PROFILES_ACTIVE=prod`, CI no GitHub Actions rodando `mvn verify`.
