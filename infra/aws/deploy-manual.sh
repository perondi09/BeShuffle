#!/usr/bin/env bash
# Primeiro deploy (ou deploy sem o GitHub Actions), a partir desta máquina:
#   1. imagem ARM da API -> ECR   2. site -> S3   3. EC2 troca o container   4. limpa o cache do CloudFront
# Precisa de: aws CLI logado, Docker com buildx, Node 22 e o terraform output (rode de infra/terraform aplicado).
# Uso: infra/aws/deploy-manual.sh
set -euo pipefail
# No Git Bash do Windows: argumentos como /* e /opt/... seguem sem conversão, e a raiz fica em C:/...
export MSYS_NO_PATHCONV=1

RAIZ="$(cd "$(dirname "$0")/../.." && (pwd -W 2>/dev/null || pwd))"
saida() { terraform -chdir="$RAIZ/infra/terraform" output -raw "$1"; }

REGIAO="$(saida region)"
ECR="$(saida ecr_repository_url)"
BUCKET="$(saida site_bucket)"
DISTRIBUICAO="$(saida cloudfront_distribution_id)"
INSTANCIA="$(saida instance_id)"
TAG="manual-$(git -C "$RAIZ" rev-parse --short HEAD)"

# Login no ECR numa config temporária do Docker: o token do ECR passa do limite do Gerenciador de Credenciais do
# Windows, então ele fica num arquivo (credHelpers vazio para o registro) que é apagado no fim. ~/.docker não muda.
REGISTRO="${ECR%%/*}"
caminho() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
DOCKER_TMP="$(mktemp -d)"
trap 'rm -rf "$DOCKER_TMP"' EXIT
cp -r "$HOME/.docker/contexts" "$HOME/.docker/buildx" "$DOCKER_TMP/" 2>/dev/null || true
printf '{"cliPluginsExtraDirs":["%s"],"currentContext":"%s","credHelpers":{"%s":""}}' \
  "$(caminho "$HOME/.docker/cli-plugins")" "$(docker context show)" "$REGISTRO" >"$DOCKER_TMP/config.json"
export DOCKER_CONFIG="$(caminho "$DOCKER_TMP")"
aws ecr get-login-password --region "$REGIAO" | docker login --username AWS --password-stdin "$REGISTRO"
docker buildx build --platform linux/arm64 -f "$RAIZ/infra/docker/backend.Dockerfile" \
  -t "$ECR:$TAG" -t "$ECR:latest" --push "$RAIZ/backend"

(cd "$RAIZ/frontend" && npm ci --no-audit --no-fund && npm run build)
aws s3 sync "$RAIZ/frontend/dist/assets" "s3://$BUCKET/assets" --delete \
  --cache-control "public,max-age=31536000,immutable"
aws s3 sync "$RAIZ/frontend/dist" "s3://$BUCKET" --delete --exclude "assets/*" --cache-control "no-cache"

COMANDO=$(aws ssm send-command --region "$REGIAO" --instance-ids "$INSTANCIA" \
  --document-name AWS-RunShellScript --comment "deploy $TAG" \
  --parameters "commands=['/opt/beshuffle/deploy.sh $TAG']" --query Command.CommandId --output text)
aws ssm wait command-executed --region "$REGIAO" --command-id "$COMANDO" --instance-id "$INSTANCIA" || true
aws ssm get-command-invocation --region "$REGIAO" --command-id "$COMANDO" --instance-id "$INSTANCIA" \
  --query "[Status, StandardOutputContent, StandardErrorContent]" --output text

aws cloudfront create-invalidation --distribution-id "$DISTRIBUICAO" --paths "/*" >/dev/null
echo "Pronto: $(saida site_url)"
