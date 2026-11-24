#!/bin/bash
# Reinicia a API quando o RDS troca a senha do banco.
#
# O RDS guarda a senha mestra no Secrets Manager e a troca sozinho a cada 7 dias. A API lê a senha uma vez, quando o
# container sobe (deploy.sh grava em /opt/beshuffle/app.env), então depois da troca ela continua com a senha velha e
# o banco recusa tudo ("password authentication failed"). Foi o que derrubou o site entre 21/11 e 23/11/2026.
#
# Este script roda a cada 5 minutos (vigia-senha-db.timer): compara a senha do segredo com a do container e, só se
# forem diferentes, refaz o deploy com a mesma imagem (o deploy.sh relê o segredo). Nunca escreve a senha no log.
set -euo pipefail

# Dois vigias ao mesmo tempo (ou um deploy manual junto) não se atropelam.
exec 9>/var/lock/beshuffle-vigia.lock
flock -n 9 || exit 0

# shellcheck disable=SC1091
source /opt/beshuffle/deploy.conf

[ -f /opt/beshuffle/app.env ] || exit 0   # ainda não houve deploy
docker inspect beshuffle-api >/dev/null 2>&1 || exit 0   # container ausente: o deploy cuida

NO_SEGREDO=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id "$DB_SECRET_ARN" \
  --query SecretString --output text | jq -r .password)
NO_CONTAINER=$(grep '^DB_PASSWORD=' /opt/beshuffle/app.env | cut -d= -f2-)

if [ -z "$NO_SEGREDO" ] || [ "$NO_SEGREDO" = "null" ]; then
  echo "vigia: não consegui ler a senha do segredo; nada foi alterado" >&2
  exit 1
fi

if [ "$NO_SEGREDO" = "$NO_CONTAINER" ]; then
  exit 0
fi

TAG=$(docker inspect -f '{{.Config.Image}}' beshuffle-api | sed 's/.*://')
echo "vigia: a senha do banco mudou no Secrets Manager; reiniciando a API com a imagem $TAG"
/opt/beshuffle/deploy.sh "$TAG"
