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

## Onde está cada coisa

| Caminho | Papel |
| --- | --- |
| `spotify/SpotifyClient` | Única porta para a Web API do Spotify |
| `spotify/SpotifyTokenProvider` | Token `client_credentials` em cache; renovado depois de um 401 |
| `services/AlbumPicker` | Estratégia de sorteio (implementação atual: `RandomQueryAlbumPicker`) |
| `services/DailyAlbumService` e `config/DailyAlbumScheduler` | Álbum do Dia |
| `config/TimeConfig` | `Clock` no fuso `beshuffle.timezone` (padrão `America/Sao_Paulo`) |
| `src/main/resources/db/migration` | Migrations do Flyway; o Hibernate só valida o schema |
