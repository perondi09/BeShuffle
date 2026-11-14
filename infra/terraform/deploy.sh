#!/bin/bash
# Atualiza a API no EC2: lê os segredos, baixa a imagem do ECR e troca o container.
# Uso: /opt/beshuffle/deploy.sh <tag da imagem>   (o workflow "Deploy AWS" chama via SSM)
set -euo pipefail

TAG="${1:-latest}"
# shellcheck disable=SC1091
source /opt/beshuffle/deploy.conf

param() {
  aws ssm get-parameter --region "$REGION" --name "$SSM_PREFIX/$1" --with-decryption \
    --query Parameter.Value --output text
}

SEGREDO_DB=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id "$DB_SECRET_ARN" \
  --query SecretString --output text)

umask 077
{
  echo "SERVER_PORT=8080"
  echo "DB_HOST=$DB_HOST"
  echo "DB_PORT=5432"
  echo "DB_NAME=$DB_NAME"
  echo "DB_USER=$(echo "$SEGREDO_DB" | jq -r .username)"
  echo "DB_PASSWORD=$(echo "$SEGREDO_DB" | jq -r .password)"
  echo "SPOTIFY_CLIENT_ID=$(param spotify-client-id)"
  echo "SPOTIFY_CLIENT_SECRET=$(param spotify-client-secret)"
  echo "COOKIE_SECURE=true"
  echo "SESSION_TIMEOUT=7d"
  echo "JAVA_OPTS=-XX:MaxRAMPercentage=70"
} > /opt/beshuffle/app.env

REGISTRO="${ECR_REPOSITORY%%/*}"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRO"
docker pull "$ECR_REPOSITORY:$TAG"

docker rm -f beshuffle-api 2>/dev/null || true
docker run -d --name beshuffle-api --restart unless-stopped \
  -p 8080:8080 --env-file /opt/beshuffle/app.env \
  --log-driver awslogs --log-opt awslogs-region="$REGION" \
  --log-opt awslogs-group="$LOG_GROUP" --log-opt awslogs-stream=api \
  "$ECR_REPOSITORY:$TAG"

for _ in $(seq 1 36); do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null; then
    echo "API no ar com a imagem $TAG"
    docker image prune -f >/dev/null
    exit 0
  fi
  sleep 5
done

echo "A API não respondeu em 3 minutos:" >&2
docker logs --tail 80 beshuffle-api >&2
exit 1
