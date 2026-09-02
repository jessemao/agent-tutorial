#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

export CLASSROOM_UID="${CLASSROOM_UID:-$(id -u)}"
export CLASSROOM_GID="${CLASSROOM_GID:-$(id -g)}"
mkdir -p .classroom-runtime

docker image inspect training-wms-classroom:0.7 >/dev/null 2>&1 || {
  echo 'Missing image: training-wms-classroom:0.7. Ask the instructor to provide it.' >&2
  exit 1
}

docker compose -f compose.classroom.yml up -d --no-build classroom

for _ in {1..90}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo 'Classroom is ready: http://localhost:8080'
    exit 0
  fi
  sleep 2
done

echo 'Classroom did not become ready. Ask the instructor to check Docker.' >&2
exit 1
