# Infra e DevOps

Tudo o que sobe, empacota e verifica o BeShuffle fica aqui. A exceção é o workflow de CI: o GitHub só o
lê em `.github/workflows/ci.yml`, na raiz do repositório.

| Arquivo | Papel |
| --- | --- |
| `docker-compose.yml` | Sobe o Postgres, a API e o site |
| `.env.example` | Modelo do `.env`, o único lugar com chaves e senhas (o `.env` é ignorado pelo git) |
| `docker/backend.Dockerfile` | Imagem da API. Contexto: `backend/` |
| `docker/frontend.Dockerfile` | Imagem do site (build do Vite servido pelo nginx). Contexto: a raiz do repositório |
| `docker/nginx.conf.template` | nginx do site: arquivos estáticos e repasse de `/api` para a API |
| `docker/*.dockerignore` | O que cada build deixa de fora |

## Subir tudo

```bash
cp .env.example .env   # preencha DB_PASSWORD e as chaves do Spotify
docker compose up --build -d
```

| Serviço | Endereço |
| --- | --- |
| Site | http://localhost:3000 (`FRONTEND_PORT`) |
| API | http://localhost:8080 (`SERVER_PORT`) |
| Postgres | `127.0.0.1:5435` (só esta máquina acessa) |

Sem `DB_PASSWORD`, `SPOTIFY_CLIENT_ID` ou `SPOTIFY_CLIENT_SECRET` no `.env`, o compose para com uma mensagem
dizendo qual variável falta.

Para desenvolver só uma parte, suba só o banco (`docker compose up -d db`) e rode a API e o site fora do Docker
(veja `backend/README.md` e `frontend/README.md`).

## Login

A API guarda a sessão num cookie HttpOnly (`JSESSIONID`), e o nginx do site repassa esse cookie junto com
`/api`, então nada muda no proxy. Em produção, com HTTPS, use `COOKIE_SECURE=true` no `.env`; em localhost
deixe `false`, ou o navegador não envia o cookie. `SESSION_TIMEOUT` (padrão `7d`) é quanto tempo o login dura.
A sessão fica na memória da API: reiniciar o container desloga todo mundo.

Os sorteios do dia são contados no banco. O visitante é identificado pelo cookie `bs_anon` (HttpOnly, 1 ano), que
também segue o `COOKIE_SECURE`; o nginx repassa os cookies sem configuração extra.

## CI

O workflow roda a cada PR e a cada push em `master` e `test`:

1. **Backend:** `./mvnw clean verify` com Java 21.
2. **Frontend:** `npm ci`, lint, testes e build com Node 22.
3. **Imagens Docker:** build das duas imagens. Só roda se os dois jobs anteriores passarem.
