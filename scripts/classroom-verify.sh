#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

task="${1:-}"
case "$task" in
  T01|T02|T03)
    ./scripts/classroom-test.sh "$task" target
    docker compose -f compose.classroom.yml exec -T frontend npm run build
    ./scripts/classroom-test.sh "$task" module
    ./scripts/classroom-test.sh "$task" all
    ;;
  T04)
    ./scripts/classroom-test.sh T04 baseline
    docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl business-wms -am test
    docker compose -f compose.classroom.yml exec -T frontend npm run build
    docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify
    ;;
  *)
    echo 'Usage: ./scripts/classroom-verify.sh T01|T02|T03|T04' >&2
    exit 2
    ;;
esac

curl -fsS http://localhost:8080/actuator/health >/dev/null

echo "$task verification passed."
