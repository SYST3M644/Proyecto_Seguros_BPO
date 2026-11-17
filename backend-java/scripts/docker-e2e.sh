#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
ACTOR="15.842.371-5"
FECHA=$(date -u -d '3 days ago' +%F)
JSON=$(jq -n --arg fecha "$FECHA" '{compania:"ASG-01",tipo:"TS-01",fechaOcurrencia:$fecha,comuna:"Maipú",descripcion:"Choque ficticio de prueba Docker"}')
RESP=$(curl -fsS -X POST "$BASE_URL/api/v1/siniestros" -H 'Content-Type: application/json' -H "X-Role: ASEGURADO" -H "X-Actor-Id: $ACTOR" -d "$JSON")
NUMERO=$(jq -er '.numero | select(test("^SIN-[0-9]{2}-[0-9]{6}$"))' <<< "$RESP")
curl -fsS "$BASE_URL/api/v1/siniestros/$NUMERO" -H "X-Role: ASEGURADO" -H "X-Actor-Id: $ACTOR" | jq -e --arg num "$NUMERO" '.id==$num and .estado=="Recibido"'
curl -fsS "$BASE_URL/api/v1/siniestros/$NUMERO/historial" -H "X-Role: ASEGURADO" -H "X-Actor-Id: $ACTOR" | jq -e 'length > 0'
CODE=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/v1/siniestros/$NUMERO" -H "X-Role: ASEGURADO" -H "X-Actor-Id: 9.418.552-1")
test "$CODE" = 403
echo "$NUMERO" > /tmp/seguros-bpo-last-number
echo "Flujo E2E correcto: $NUMERO"
