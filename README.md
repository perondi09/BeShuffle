# BeShuffle

Aplicacao web para descobrir albuns aleatorios do Spotify.

## O que voce precisa

Para rodar localmente (sem Docker):
- Java 21+
- Git

Para rodar com Docker:
- Docker Desktop (ou Docker Engine + Compose)
- Git

## 1) Clonar o projeto

```bash
git clone https://github.com/perondi09/beshuffle.git
cd beshuffle/BeShuffle
```

## 2) Configurar credenciais do Spotify

Crie o arquivo `.env` na raiz do projeto (ou copie de `.env.example`) e preencha:

```bash
cp .env.example .env
```

```bash
SPOTIFY_CLIENT_ID=seu_client_id
SPOTIFY_CLIENT_SECRET=seu_client_secret
```

Voce consegue essas credenciais em:
- https://developer.spotify.com/dashboard

## 3) Rodar a aplicacao

Escolha **uma** das opcoes abaixo.

### Opcao A - Rodar localmente (Maven Wrapper)

```bash
./mvnw spring-boot:run
```

### Opcao B - Rodar com Docker Compose

```bash
docker compose up --build
```

Se seu ambiente usa o comando antigo, use:

```bash
docker-compose up --build
```

## 4) Acessar no navegador

- http://localhost:8080

Ao abrir a pagina:
- o album eh carregado automaticamente
- o botao `Novo album` busca outro album

## 5) Parar a aplicacao

Se estiver rodando localmente:
- `Ctrl + C`

Se estiver rodando com Docker Compose:

```bash
docker compose down
```

ou

```bash
docker-compose down
```

## Endpoint principal

```http
POST /api/albums/random
```

Retorna um album aleatorio do Spotify para exibicao na interface.

## Erros comuns

- `invalid_client`: confira `SPOTIFY_CLIENT_ID` e `SPOTIFY_CLIENT_SECRET` no `.env`
- Porta `8080` ocupada: pare outro processo na porta ou mude o mapeamento no `docker-compose.yml`

## Autor

Desenvolvido por Guilherme Perondi:
- https://www.linkedin.com/in/guilherme-perondi/
