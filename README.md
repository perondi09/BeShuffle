# BeShuffle

Aplicação web full-stack para descobrir álbuns aleatórios do Spotify e acompanhar o **Álbum do Dia** gerado automaticamente.

## 📋 O que você precisa

- Docker Desktop (ou Docker Engine + Docker Compose)
- Git

---

## 🚀 1. Clonar o projeto

```bash
git clone https://github.com/perondi09/beshuffle.git
cd beshuffle
```

---

## 🔑 2. Configurar credenciais do Spotify e Banco de Dados

Crie um arquivo `.env` dentro da pasta `infra/` contendo as suas credenciais:

```env
SPOTIFY_CLIENT_ID=seu_client_id
SPOTIFY_CLIENT_SECRET=seu_client_secret
DB_NAME=beshuffle_db
DB_USER=postgres
DB_PASSWORD=sua_senha
```

As credenciais do Spotify podem ser obtidas em:

👉 https://developer.spotify.com/dashboard

---

## 🐳 3. Rodar a aplicação com Docker Compose

Acesse a pasta de infraestrutura e inicie os containers (PostgreSQL, Back-end Spring Boot e Front-end React com Nginx):

```bash
cd infra
docker compose up -d --build
```

> **Observação:** Se estiver utilizando a versão antiga do Docker Compose, execute:

```bash
docker-compose up -d --build
```

---

## 🌐 4. Acessar a aplicação

Após a inicialização dos containers, acesse:

- **Front-end:** http://localhost:3000
- **API (Swagger/Endpoints):** http://localhost:8080

### Funcionalidades

Ao abrir a aplicação:

- O **Álbum do Dia** é carregado automaticamente pelo sistema.
- Na aba **Aleatório**, é possível gerar novos álbuns ilimitadamente clicando no botão **Novo Álbum**.

---

## 🛑 5. Parar a aplicação

Para encerrar todos os containers:

```bash
docker compose down
```

---

# 📡 Endpoints da API

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| `GET` | `/api/albums/daily` | Retorna o Álbum do Dia atual. |
| `GET` | `/api/albums/random` | Retorna um álbum totalmente aleatório do Spotify. |

---

# 🛠️ Tecnologias Utilizadas

### Front-end

- React
- React Router
- CSS Moderno

### Back-end

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Scheduling

### Banco de Dados

- PostgreSQL 18

### DevOps & Infraestrutura

- Docker
- Docker Compose
- Nginx

---

# ❗ Erros comuns

### `invalid_client`

Verifique se as variáveis:

- `SPOTIFY_CLIENT_ID`
- `SPOTIFY_CLIENT_SECRET`

estão configuradas corretamente no arquivo `.env`.

### Porta em uso

Certifique-se de que as seguintes portas estão disponíveis:

- `3000` (Front-end)
- `8080` (Back-end)
- `5435` (PostgreSQL)

---

# 👨‍💻 Autor

Desenvolvido por **Guilherme Perondi**

LinkedIn: https://www.linkedin.com/in/guilherme-perondi/