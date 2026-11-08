# BeShuffle

Um álbum novo por dia, sorteado do Spotify. Visitantes sorteiam álbuns; no futuro, quem tiver conta vai ver os
sorteados do dia, dar nota e comentar.

## Estrutura

| Pasta | O que tem | Documentação |
| --- | --- | --- |
| `backend/` | API Spring Boot 4 / Java 21 (Spotify, Álbum do Dia, PostgreSQL + Flyway) | [backend/README.md](backend/README.md) |
| `frontend/` | Site em React 19 + TypeScript + Vite | [frontend/README.md](frontend/README.md) |
| `infra/` | Docker Compose, Dockerfiles, nginx e `.env.example` | [infra/README.md](infra/README.md) |
| `.github/` | CI (GitHub Actions): testes do backend e do frontend e build das imagens | [infra/README.md](infra/README.md#ci) |
| `docs/` | Contexto do projeto, roadmap e proposta visual | [docs/CONTEXTO.md](docs/CONTEXTO.md) |

## Começar

1. **Crie o arquivo de segredos.** Chaves e senhas ficam só nesse arquivo, que o git ignora:

   ```bash
   cp infra/.env.example infra/.env
   ```

   Preencha `DB_PASSWORD`, `SPOTIFY_CLIENT_ID` e `SPOTIFY_CLIENT_SECRET`. As credenciais do Spotify saem do
   [dashboard de desenvolvedor](https://developer.spotify.com/dashboard).

2. **Suba tudo com Docker:**

   ```bash
   cd infra
   docker compose up --build -d
   ```

   O site abre em http://localhost:3000 e a API responde em http://localhost:8080.

Para desenvolver uma parte só, siga o README da pasta correspondente.

## Requisitos

- Docker com Docker Compose
- JDK 21 para rodar o backend fora do Docker (o JDK 27 não compila o Lombok do Spring Boot 4.0.3)
- Node.js 22 para rodar o frontend fora do Docker

## Fluxo de branches

```
backend ─┐
frontend ┼─► PR ─► test ─► (CI verde + testes) ─► PR ─► master
devops ──┤
docs ────┘
```

- **Branch por área:** `backend`, `frontend`, `devops` (infra e CI) e `docs`.
- **Tudo passa pela `test`:** cada mudança entra na `test` por pull request.
- **`master` só depois dos testes:** o PR `test → master` só abre com o CI verde e a `test` testada.
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/pt-br/), por exemplo `feat(frontend): ...`, `fix(backend): ...`, `ci: ...` ou `docs: ...`.

## Endpoints da API

| Método | Rota | O que faz |
| --- | --- | --- |
| `GET` | `/api/albums/random` | Sorteia um álbum do Spotify |
| `GET` | `/api/albums/daily` | Álbum do Dia (vira à meia-noite de São Paulo) |
| `GET` | `/actuator/health` | Saúde da API |
