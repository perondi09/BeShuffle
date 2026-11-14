#!/usr/bin/env bash
# Copia as chaves do Spotify do infra/.env para o SSM Parameter Store (SecureString) da conta AWS.
# A senha do banco não passa por aqui: o RDS gera e guarda no Secrets Manager.
# Uso: infra/aws/put-secrets.sh [região]
set -euo pipefail

REGIAO="${1:-us-east-1}"
ENV_FILE="$(dirname "$0")/../.env"

valor() {
  sed -n "s/^$1=//p" "$ENV_FILE" | tr -d '\r'
}

for par in "spotify-client-id:SPOTIFY_CLIENT_ID" "spotify-client-secret:SPOTIFY_CLIENT_SECRET"; do
  nome="/beshuffle/${par%%:*}"
  conteudo="$(valor "${par##*:}")"
  if [ -z "$conteudo" ]; then
    echo "Falta ${par##*:} em infra/.env" >&2
    exit 1
  fi
  aws ssm put-parameter --region "$REGIAO" --name "$nome" --type SecureString \
    --value "$conteudo" --overwrite >/dev/null
  echo "Gravado: $nome"
done
