#!/bin/bash
set -euo pipefail

AWS_REGION="${AWS_REGION:?AWS_REGION is required}"
ECR_REGISTRY="${ECR_REGISTRY:?ECR_REGISTRY is required}"
ECR_REPO="${ECR_REPO:?ECR_REPO is required}"
APP_ENV_CONTENT_BASE64="${APP_ENV_CONTENT_BASE64:?APP_ENV_CONTENT_BASE64 is required}"

CONTAINER_NAME="${CONTAINER_NAME:-codoc-backend}"
DOCKER_NETWORK="${DOCKER_NETWORK:-codoc_default}"
HOST_BIND_IP="${HOST_BIND_IP:-127.0.0.1}"
HOST_PORT="${HOST_PORT:-8080}"
APP_PORT="${APP_PORT:-8080}"
HEALTH_PATH="${HEALTH_PATH:-/api/health}"
APP_ENV_PATH="${APP_ENV_PATH:-/home/ubuntu/codoc/.env}"
LOG_HOST_DIR="${LOG_HOST_DIR:-/home/ubuntu/codoc/logs/backend}"
IMAGE_TAG="${IMAGE_TAG:-dev}"
IMAGE_URI="${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"

echo "[deploy-backend-dev] writing env file to ${APP_ENV_PATH}"
mkdir -p "$(dirname "${APP_ENV_PATH}")"
printf '%s' "${APP_ENV_CONTENT_BASE64}" | base64 -d > "${APP_ENV_PATH}"

echo "[deploy-backend-dev] logging in to ECR"
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${ECR_REGISTRY}"

echo "[deploy-backend-dev] pulling image ${IMAGE_URI}"
docker pull "${IMAGE_URI}"

if ! docker network inspect "${DOCKER_NETWORK}" >/dev/null 2>&1; then
  echo "[deploy-backend-dev] missing docker network: ${DOCKER_NETWORK}"
  exit 1
fi

mkdir -p "${LOG_HOST_DIR}"

echo "[deploy-backend-dev] replacing container ${CONTAINER_NAME}"
docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  --network "${DOCKER_NETWORK}" \
  -p "${HOST_BIND_IP}:${HOST_PORT}:${APP_PORT}" \
  -v "${LOG_HOST_DIR}:/app/logs" \
  --env-file "${APP_ENV_PATH}" \
  "${IMAGE_URI}"

echo "[deploy-backend-dev] waiting for health check"
for _ in $(seq 1 60); do
  if curl -fsS "http://localhost:${HOST_PORT}${HEALTH_PATH}" >/dev/null; then
    echo "[deploy-backend-dev] health check passed"
    docker image prune -f >/dev/null 2>&1 || true
    exit 0
  fi
  sleep 5
done

echo "[deploy-backend-dev] health check failed"
docker logs --tail 200 "${CONTAINER_NAME}" 2>&1 || true
exit 1
