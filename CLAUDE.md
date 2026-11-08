# BeShuffle

Sorteia álbuns do Spotify e guarda o "Álbum do Dia". Monorepo com três partes:
`backend/` (Spring Boot 4 / Java 21), `frontend/` (React 19 + TypeScript + Vite) e `infra/` (Docker, nginx).
Contexto completo, problemas conhecidos e roadmap: [docs/CONTEXTO.md](docs/CONTEXTO.md).

- Rodar tudo: `cd infra && docker compose up --build -d` (precisa de `infra/.env`, ver `.env.example`). Site em :3000, API em :8080
- Segredos: só em `infra/.env` (gitignored), lido pelo compose e pela API via `spring.config.import=../infra/.env`. Nunca pôr valor padrão de chave/senha no código; testes usam chaves falsas em `backend/src/test/resources/application-test.properties`
- Backend: `cd backend && ./mvnw clean verify` com **JDK 21** (o Lombok do Boot 4.0.3 não compila com JDK 27). Sem JDK 21 local:
  `docker run --rm -v "$PWD/backend:/ws" -v beshuffle-m2:/root/.m2 -w /ws maven:3.9.9-eclipse-temurin-21 mvn verify`
- Frontend: `cd frontend && npm ci && npm run lint && npm test && npm run build`. Dev: `npm run dev` (proxy `/api` → :8080)
- CI: `.github/workflows/ci.yml` (jobs backend, frontend e build das imagens); Dockerfiles e nginx em `infra/docker/`
- Spotify: `spotify/SpotifyClient` (única porta para a Web API) + `spotify/SpotifyTokenProvider` (token em cache)
- Sorteio: interface `services/AlbumPicker`; implementação atual `services/RandomQueryAlbumPicker`
- Álbum do dia: `services/DailyAlbumService` + `config/DailyAlbumScheduler`; fuso em `beshuffle.timezone` (`Clock` de `config/TimeConfig`)
- Schema: migrations Flyway em `backend/src/main/resources/db/migration`; Hibernate só valida (`ddl-auto=validate`)
- Branches: trabalho em `backend`, `frontend`, `devops` (infra/CI) ou `docs` → PR para `test` → depois do CI verde, PR `test` → `master`. Nunca commitar direto em `test`/`master`
- Idioma do código/logs: português. Commits: Conventional Commits, descritivos, sem trailer de coautoria.
- Visitante sorteia 1x/dia; usuário logado vê sorteados do dia, dá nota e comenta (ainda não implementado).
