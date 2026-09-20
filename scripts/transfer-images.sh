#!/usr/bin/env bash
set -euo pipefail

pause() {
    echo
    read -r -p "Press Enter to exit..."
}

trap pause EXIT

API_URL="${API_URL:-http://localhost:8080}"
ENDPOINT="${API_URL%/}/api/v1/images/transfer"

if ! command -v curl >/dev/null 2>&1; then
    echo "curl is required" >&2
    exit 1
fi

echo "Image transfer"
echo "API: ${ENDPOINT}"
echo "1) LOCAL -> S3"
echo "2) S3 -> LOCAL"
echo

read -n 1 -r -p "Choice [1/2]: " CHOICE
echo

case "$CHOICE" in
    1)
        FROM="LOCAL"
        TO="S3"
        ;;
    2)
        FROM="S3"
        TO="LOCAL"
        ;;
    *)
        echo "Expected 1 or 2" >&2
        exit 1
        ;;
esac

echo
echo "Copying ${FROM} -> ${TO} ..."

response=$(curl -sS -w $'\n%{http_code}' \
    -X POST \
    -H "Content-Type: application/json" \
    -d "{\"from\":\"${FROM}\",\"to\":\"${TO}\"}" \
    "${ENDPOINT}")

http_code=$(printf '%s' "$response" | tail -n1)
body=$(printf '%s' "$response" | sed '$d')

echo
echo "HTTP ${http_code}"
if [[ -n "$body" ]]; then
    if command -v jq >/dev/null 2>&1; then
        printf '%s\n' "$body" | jq .
    else
        printf '%s\n' "$body"
    fi
fi
