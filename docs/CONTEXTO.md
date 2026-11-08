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

### Stack real (pom.xml)
- Spring Boot **4.0.3** (`spring-boot-starter-webmvc`), Spring Cloud **2025.1.0** (OpenFeign)
- Spring Data JPA + PostgreSQL (H2 nos testes), Actuator, Lombok, `dotenv-java`
- `jackson-databind` 2 declarado explicitamente (Boot 4 usa Jackson 3 → mistura, ver §5)
- Docker multi-stage (Maven → `eclipse-temurin:21-jre`, usuário não-root) e `infra/docker-compose.yml` (Postgres 16 + API)

### Estrutura

```
src/main/java/perondi/BeShuffle/
├── BeShuffleApplication.java      carrega .env em System properties; @EnableFeignClients @EnableScheduling
├── client/
│   ├── AuthSpotifyClient          Feign → POST accounts.spotify.com/api/token (client_credentials)
│   └── AlbumSpotifyClient         Feign → GET api.spotify.com/v1/albums/{id}
├── config/
│   ├── ApplicationConfig          ObjectMapper (Jackson 2), RestTemplate, DataSource Hikari manual c/ fallback de DNS
│   └── DailyAlbumScheduler        cron 00:00 (horário do servidor) → gera Álbum do Dia
├── controller/AlbumController     GET /api/albums/random, GET /api/albums/daily
├── dtos/login/                    LoginRequest (form), LoginResponse (access_token)
├── entity/
│   ├── Album, AlbumImage, Artist  na prática são DTOs do Spotify (não são @Entity)
│   └── DailyAlbum                 @Entity daily_albums (album id único, display_date único, JSON completo)
├── exceptions/                    AlbumException + Spotify*/Validation + GlobalExceptionHandler
├── repository/DailyAlbumRepository findByDisplayDate
└── services/
    ├── AuthService                busca token do Spotify (a cada chamada)
    ├── AlbumService               busca álbum por id via Feign
    ├── SpotifyRandomAlbumService  ALGORITMO DE SORTEIO (ver §4)
    └── DailyAlbumService          lê/gera/persiste o álbum do dia
src/main/resources/static/          frontend vanilla (index.html, Script.js, Styles.css)
src/test/...                        AuthServiceTest, DailyAlbumServiceTest, ServiceUnitTests, AlbumIntegrationTest (MockMvc standalone)
```

### Endpoints
| Método | Rota | O que faz |
| --- | --- | --- |
| GET | `/api/albums/random` | sorteia id → busca álbum completo → retorna JSON do Spotify |
| GET | `/api/albums/daily` | retorna álbum do dia do banco; se não houver, gera na hora |
| GET | `/actuator/health` | health check |

### Frontend atual
- Vanilla JS servido pelo próprio Spring (`/static`). Card escuro único, toggle "Aleatório / Do dia",
  capa, artista, título, botão "Ouvir no Spotify" e "Novo aleatório". Fonte Playfair + Poppins, verde Spotify.
- Layout "sem scroll" (`overflow: hidden` no body) — no celular pode cortar conteúdo.
- Havia um frontend React (CRA) em `beshuffle-frontend/`, removido em `b66f16f`
  ("migrado para repositório dedicado"). Esse repositório **não está nesta máquina**.

---

## 3. Histórico (commits e branches)

Remoto: `github.com/perondi09/BeShuffle`. Branches remotas: `create-ramdomalbum`, `create-repositories`,
`security`, `visual-changes` — **todas já mergeadas** (0 commits à frente de `master`). Não há stash.

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

Como funciona hoje:
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
> Ficou para depois (levantado na revisão):
> - o token é renovado dentro de um lock que espera a chamada HTTP;
> - os `RestClient` não usam o `RestClient.Builder` do Boot (sem métricas);
> - `baseline-on-migrate` deve sair depois do primeiro deploy;
> - a porta 5435 do Postgres está exposta em todas as interfaces no compose.

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
