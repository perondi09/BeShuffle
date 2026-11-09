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
| `GET` | `/api/albums/random` | Sorteia um álbum do Spotify |
| `GET` | `/api/albums/daily` | Álbum do Dia (vira à meia-noite de São Paulo) |
| `GET` | `/actuator/health` | Saúde da aplicação |

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

## Onde está cada coisa

| Caminho | Papel |
| --- | --- |
| `spotify/SpotifyClient` | Única porta para a Web API do Spotify |
| `spotify/SpotifyTokenProvider` | Token `client_credentials` em cache; renovado depois de um 401 |
| `services/AlbumPicker` | Estratégia de sorteio (implementação atual: `PopularPorAnoAlbumPicker`) |
| `services/FiltroDeAlbum` e `config/SorteioProperties` | O que entra no sorteio (`beshuffle.sorteio.*`) |
| `services/DailyAlbumService` e `config/DailyAlbumScheduler` | Álbum do Dia |
| `config/TimeConfig` | `Clock` no fuso `beshuffle.timezone` (padrão `America/Sao_Paulo`) |
| `src/main/resources/db/migration` | Migrations do Flyway; o Hibernate só valida o schema |
