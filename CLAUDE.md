# BeShuffle

API Spring Boot 4 / Java 21 que sorteia álbuns do Spotify e guarda o "Álbum do Dia" no PostgreSQL.
Contexto completo, problemas conhecidos e roadmap: [docs/CONTEXTO.md](docs/CONTEXTO.md).

- Rodar: `cd infra && docker compose up --build -d` (precisa de `infra/.env`, ver `.env.example`)
- Segredos: só em `infra/.env` (gitignored), lido pelo compose e pela app via `spring.config.import`. Nunca pôr valor padrão de chave/senha no código; testes usam chaves falsas em `application-test.properties`
- CI: `.github/workflows/ci.yml` roda `./mvnw -B clean verify` com Java 21
- Testes: `./mvnw test` com **JDK 21** (o Lombok do Boot 4.0.3 não compila com JDK 27). Sem JDK 21 local:
  `docker run --rm -v "$PWD:/ws" -v beshuffle-m2:/root/.m2 -w /ws maven:3.9.9-eclipse-temurin-21 mvn test`
- Spotify: `spotify/SpotifyClient` (única porta para a Web API) + `spotify/SpotifyTokenProvider` (token em cache)
- Sorteio: interface `services/AlbumPicker`; implementação atual `services/RandomQueryAlbumPicker`
- Álbum do dia: `services/DailyAlbumService` + `config/DailyAlbumScheduler`; fuso em `beshuffle.timezone` (`Clock` de `config/TimeConfig`)
- Schema: migrations Flyway em `src/main/resources/db/migration`; Hibernate só valida (`ddl-auto=validate`)
- Idioma do código/logs: português. Commits: Conventional Commits, descritivos, sem trailer de coautoria.
- Visitante sorteia 1x/dia; usuário logado vê sorteados do dia, dá nota e comenta (ainda não implementado).
