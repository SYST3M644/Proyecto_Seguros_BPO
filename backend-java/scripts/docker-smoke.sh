#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
curl -fsS "$BASE_URL/api/v1/health" | jq -e '.status == "UP" and .service == "api-gateway"'
curl -fsS "$BASE_URL/api/v1/companias/ASG-01" | jq -e '.codigo == "ASG-01"'
curl -fsS "$BASE_URL/api/v1/tipos-siniestro/TS-01" | jq -e '.codigo == "TS-01"'
curl -fsS "$BASE_URL/openapi/catalogos" | jq -e '.openapi and .paths["/api/v1/companias"]'
curl -fsS "$BASE_URL/openapi/siniestros" | jq -e '.openapi and .paths["/api/v1/siniestros"]'
curl -fsS "$BASE_URL/v3/api-docs/swagger-config" | jq -e '[.urls[].url] | index("/openapi/catalogos") and index("/openapi/siniestros")'
curl -fsSL "$BASE_URL/swagger-ui.html" | grep -qi swagger
echo "Smoke Docker API Gateway + Swagger: OK"
