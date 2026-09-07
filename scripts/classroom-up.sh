#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

export CLASSROOM_UID="${CLASSROOM_UID:-$(id -u)}"
export CLASSROOM_GID="${CLASSROOM_GID:-$(id -g)}"
mkdir -p .classroom-runtime/frontend-node-modules .classroom-runtime/npm

docker image inspect training-wms-classroom:0.7 >/dev/null 2>&1 || {
  echo 'Missing image: training-wms-classroom:0.7' >&2
  echo 'Ask the instructor for the course image, then run this script again.' >&2
  exit 1
}

docker compose -f compose.classroom.yml up -d --no-build classroom frontend

for _ in {1..90}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo 'Classroom is ready: http://localhost:8080'
    exit 0
  fi
  sleep 2
done

echo 'Classroom did not become ready. Run ./scripts/classroom-status.sh for details.' >&2
exit 1
