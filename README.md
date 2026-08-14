# BeShuffle API

Backend Java/Spring Boot para selecionar álbuns aleatórios do Spotify e manter o Álbum do Dia em um banco PostgreSQL.

This repository contains only the API layer. The frontend was moved to a separate repository.

## Requirements

- Docker Desktop or Docker Engine
- Docker Compose
- Git
- Java 21 (for local development)

## Local environment

The default local setup uses a PostgreSQL container started by Docker Compose.

### 1. Configure environment variables

Inside `infra/`, create a `.env` file based on `.env.example`:

```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=default

DB_NAME=beshuffle_db
DB_USER=postgres
DB_PASSWORD=Senhalol.123

SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
```

The Spotify credentials can be obtained here:
https://developer.spotify.com/dashboard

### 2. Start the application with Docker Compose

```bash
cd infra
docker compose up --build -d
```

This starts:
- PostgreSQL 18 on port `5435`
- The Spring Boot application on port `8080`

### 3. Check application health

```bash
curl http://localhost:8080/actuator/health
```

### 4. Stop the stack

```bash
docker compose down
```

## Database connection

The app is configured to connect to the local PostgreSQL container using these defaults:

- Host: `db` (from inside Docker)
- Port: `5432`
- Database: `beshuffle_db`
- User: `postgres`
- Password: `Senhalol.123`

For local host execution outside Docker, the application defaults to:

- Host: `localhost`
- Port: `5435`

## Dockerfile

The `Dockerfile` is a multi-stage build that compiles the project and runs the application in a lightweight Java image.

## API endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/albums/random` | Returns a random Spotify album |
| `GET` | `/api/albums/daily` | Returns the Album of the Day |

## Tech stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL 18
- OpenFeign
- Docker / Docker Compose
- Actuator

## Troubleshooting

### Database connection refused

Check if the PostgreSQL container is running:

```bash
docker compose ps
docker logs beshuffle-db
```

### Spotify invalid_client

Verify that the variables `SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET` are set correctly in `infra/.env`.

### Local database port already in use

If port `5435` is already occupied, change the mapping in `docker-compose.yml`:

```yaml
ports:
  - "5436:5432"
```

Then align your local datasource settings accordingly.