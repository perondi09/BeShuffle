# Imagem do site. O contexto de build é a raiz do repositório (ver infra/docker-compose.yml);
# as exclusões ficam em frontend.Dockerfile.dockerignore, ao lado deste arquivo.
FROM node:22.17.0-alpine AS build
WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build

# O nginx serve o build estático e repassa /api para a API.
FROM nginx:1.27.5-alpine
COPY infra/docker/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# Endereço da API dentro da rede do compose; o compose pode sobrescrever.
ENV API_UPSTREAM=http://beshuffle:8080

EXPOSE 80
