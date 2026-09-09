#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

task="${1:-}"
case "$task" in
  T01|T02|T03) ;;
  *)
    echo 'Usage: ./scripts/classroom-verify.sh T01|T02|T03' >&2
    exit 2
    ;;
esac

./scripts/classroom-test.sh "$task" target
docker compose -f compose.classroom.yml exec -T frontend npm run build
./scripts/classroom-test.sh "$task" module
./scripts/classroom-test.sh "$task" all
curl -fsS http://localhost:8080/actuator/health >/dev/null

echo "$task verification passed."
