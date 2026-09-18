#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

if [[ "${1:-}" != "TASK1" ]] || [[ ! "${2:-}" =~ ^V2-T1-G[0-9]{2,}$ ]]; then
  echo 'Usage: ./scripts/classroom-verify.sh TASK1 V2-T1-Gxx' >&2
  exit 2
fi

classroom_port="${CLASSROOM_PORT:-8080}"

./scripts/validate-v2-task1.sh m3 "$2"
docker compose -f compose.classroom.yml exec -T classroom \
  /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl training-server -am test
docker compose -f compose.classroom.yml exec -T frontend npm run build
docker compose -f compose.classroom.yml exec -T classroom \
  /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify
git diff --check
curl -fsS "http://localhost:${classroom_port}/actuator/health" >/dev/null

echo "TASK1 candidate verification passed for $2; record M4 evidence before running the m4 gate."
