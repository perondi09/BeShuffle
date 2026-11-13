# BeShuffle API (backend)

API Spring Boot 4 / Java 21 que sorteia álbuns do Spotify e guarda o "Álbum do Dia" no PostgreSQL.

## Requisitos

- **JDK 21.** O Lombok do Spring Boot 4.0.3 não compila com o JDK 27.
- **`infra/.env` preenchido** a partir de `infra/.env.example`. É ali que ficam as chaves do Spotify e a senha do banco.
- **Postgres rodando.** O jeito mais simples é subir o do compose: `cd ../infra && docker compose up -d db`.

## Rodar

Todos os comandos rodam dentro de `backend/`:

```bash
./mvnw spring-boot:run
```

A aplicação lê `../infra/.env` (`spring.config.import`). Variáveis de ambiente de verdade têm prioridade sobre o arquivo.
Sem `DB_PASSWORD`, `SPOTIFY_CLIENT_ID` ou `SPOTIFY_CLIENT_SECRET`, ela não sobe: nenhum segredo tem valor padrão.

## Testar

```bash
./mvnw clean verify
```

Os testes usam H2 e chaves falsas (`src/test/resources/application-test.properties`), então não precisam de `.env` nem de banco.

## Endpoints

| Método | Rota | O que faz |
| --- | --- | --- |
| `GET` | `/api/albums/daily` | Álbum do Dia (vira à meia-noite de São Paulo) |
| `GET` | `/actuator/health` | Saúde da aplicação |
| `POST` | `/api/auth/register` | Cria a conta `{displayName, email, password}` e já entra (201); e-mail repetido 409, dados ruins 400 |
| `POST` | `/api/auth/login` | Entra com `{email, password}` (204); senha ou e-mail errado 401 |
| `POST` | `/api/auth/logout` | Sai (204) |
| `GET` | `/api/me` | Quem está logado `{id, email, displayName}`; sem login 401 |
| `POST` | `/api/draws` | Sorteia gastando uma ficha do dia; devolve `{album, allowance}`. Corpo opcional `{"genres":["rock"]}` (só logado; visitante com estilos 401). Sem fichas 429 `DAILY_LIMIT_REACHED`; Spotify fora 503 e a ficha volta |
| `GET` | `/api/draws/allowance` | Fichas de hoje `{plan, remaining, dailyLimit, resetsAt}` |
| `GET` | `/api/draws/today` | Sorteados de hoje por todo mundo, com quantas vezes cada álbum saiu (login) |
| `GET` | `/api/genres` | Estilos que podem ser escolhidos no sorteio `[{id, name}]` |
| `GET` | `/api/reviews/today` | Notas do Álbum do Dia: média, total, a sua e as 50 mais recentes (login) |
| `PUT` | `/api/reviews/today` | Cria ou edita a sua nota `{rating 1-5, comment até 500}` (login) |

### Conta e sessão

- A sessão fica num cookie `JSESSIONID` HttpOnly (SameSite=Lax, 7 dias). Em produção com HTTPS, defina
  `COOKIE_SECURE=true`.
- Todo `POST` e `PUT` exige o token CSRF: a API manda o cookie `XSRF-TOKEN` em qualquer resposta e o site devolve o
  valor no header `X-XSRF-TOKEN`. No Bruno, a pasta "Auth" faz isso sozinha.
- Exigem login: `/api/me`, `/api/draws/today` e `/api/reviews/**`. O resto é público.
- A sessão vive na memória da API: reiniciar desloga todo mundo (Spring Session JDBC fica para o deploy).

## Fichas do dia (tudo grátis)

| Quem | Sorteios por dia | Estilos | Sorteados de hoje e notas |
| --- | --- | --- | --- |
| Visitante | 1 | não | não |
| Conta logada | 10 | sim | sim (notas só no Álbum do Dia) |

- O Álbum do Dia é à parte: é o mesmo para todo mundo e não gasta ficha.
- O visitante é contado pelo cookie `bs_anon` (UUID aleatório, HttpOnly, 1 ano). Apagar os cookies dá outra ficha;
  para 1 sorteio grátis isso é aceitável.
- A tabela `draw_quota` tem uma linha por dono (`u:<id>` ou `a:<uuid>`) e dia. A ficha é gasta com um `UPDATE`
  condicional (`used < limite`), então dois cliques ao mesmo tempo não passam do limite.
- Os limites ficam em `services/Plano`; o "dia" segue `beshuffle.timezone`.

## Como o sorteio escolhe o álbum

O app do Spotify em Development Mode não recebe `popularity`, gêneros, top tracks nem playlists editoriais
(testado em 08/11/2026). O que funciona é a busca, e ela vem ordenada por relevância. Então:

1. Sorteia um ano entre `ano-inicial` (1960) e o ano atual, e uma página entre os primeiros 50 resultados de
   `year:ANO` no mercado `BR`. Ali estão os álbuns mais ouvidos daquele ano.
2. Descarta singles, EPs, coletâneas, "Various Artists", álbuns com menos de 6 ou mais de 40 faixas e nomes
   (do álbum ou do artista) com termos bloqueados: ninar, infantil, karaokê, tributo, greatest hits etc.
   A comparação ignora acentos e maiúsculas e só pega a palavra inteira.
3. Sorteia um dos álbuns que sobraram. Se não sobrar nenhum, tenta outro ano (até 5 vezes).

Tudo é configurável em `beshuffle.sorteio.*` (ver `application.properties`).

### Sorteio por estilo (conta logada)

A busca de álbuns do Spotify ignora `genre:`, mas a de faixas aceita. O `EstiloAlbumPicker` sorteia um dos estilos
pedidos, busca faixas `genre:"..."` numa das 10 primeiras páginas e sorteia um dos álbuns dessas faixas que o
`FiltroDeAlbum` aceita. O catálogo (`services/Estilo`) só tem gêneros testados contra a API (12/11/2026) que trazem
álbuns de verdade: rock, indie, metal, punk, pop, dance, hip hop, jazz, blues, soul, reggae, folk, country, música
latina, MPB, samba, forró, gospel e clássica. Funk, sertanejo, pagode e piseiro quase não têm resultado e ficaram de fora.

## Onde está cada coisa

| Caminho | Papel |
| --- | --- |
| `spotify/SpotifyClient` | Única porta para a Web API do Spotify |
| `spotify/SpotifyTokenProvider` | Token `client_credentials` em cache; renovado depois de um 401 |
| `services/AlbumPicker` | Estratégia de sorteio (implementação atual: `PopularPorAnoAlbumPicker`) |
| `services/EstiloAlbumPicker` e `services/Estilo` | Sorteio por estilo musical e o catálogo de estilos |
| `controller/DrawController`, `services/DrawService` e `services/DrawAllowanceService` | Sorteios com fichas do dia e sorteados de hoje |
| `repository/DrawQuotaRepository` | Fichas gastas por dono e dia (SQL direto, gasto atômico) |
| `controller/ReviewController` e `services/ReviewService` | Notas e comentários do Álbum do Dia |
| `config/SecurityConfig` | Rotas públicas e protegidas, CSRF, logout e erros 401/403 em JSON |
| `controller/AuthController` e `services/AuthService` | Cadastro, login e `/api/me`; senha com BCrypt |
| `services/FiltroDeAlbum` e `config/SorteioProperties` | O que entra no sorteio (`beshuffle.sorteio.*`) |
| `services/DailyAlbumService` e `config/DailyAlbumScheduler` | Álbum do Dia |
| `config/TimeConfig` | `Clock` no fuso `beshuffle.timezone` (padrão `America/Sao_Paulo`) |
| `src/main/resources/db/migration` | Migrations do Flyway; o Hibernate só valida o schema |
