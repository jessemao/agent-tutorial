#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

export CLASSROOM_UID="${CLASSROOM_UID:-$(id -u)}"
export CLASSROOM_GID="${CLASSROOM_GID:-$(id -g)}"
classroom_port="${CLASSROOM_PORT:-8080}"
expected_course_ref='v2.0-task1-start'
if [[ -f .student-baseline ]]; then
  expected_course_commit="$(sed -n 's/^source-commit=//p' .student-baseline | head -n 1)"
else
  expected_course_commit="$(git rev-parse "${expected_course_ref}^{commit}")"
fi
if [[ ! "$expected_course_commit" =~ ^[0-9a-f]{40}$ ]]; then
  echo 'Invalid source commit in .student-baseline.' >&2
  exit 1
fi
mkdir -p .classroom-runtime/frontend-node-modules .classroom-runtime/npm

docker image inspect training-wms-classroom:0.7 >/dev/null 2>&1 || {
  echo 'Missing image: training-wms-classroom:0.7' >&2
  echo 'Ask the instructor for the course image, then run this script again.' >&2
  exit 1
}
docker image inspect mysql:8.0 >/dev/null 2>&1 || {
  echo 'Missing image: mysql:8.0' >&2
  echo 'Ask the instructor for the complete task-one image package, then run this script again.' >&2
  exit 1
}
actual_course_ref="$(docker image inspect training-wms-classroom:0.7 \
  --format '{{ index .Config.Labels "io.training.course.ref" }}')"
if [[ "$actual_course_ref" != "$expected_course_ref" ]]; then
  echo "Wrong classroom image: expected $expected_course_ref, found ${actual_course_ref:-unlabeled}." >&2
  echo 'Ask the instructor for the V2 task-one image, then run this script again.' >&2
  exit 1
fi
actual_course_commit="$(docker image inspect training-wms-classroom:0.7 \
  --format '{{ index .Config.Labels "org.opencontainers.image.revision" }}')"
if [[ "$actual_course_commit" != "$expected_course_commit" ]]; then
  echo "Stale classroom image: expected baseline $expected_course_commit, found ${actual_course_commit:-unlabeled}." >&2
  echo "Rebuild training-wms-classroom:0.7 from the v2.0-task1-start baseline before starting the classroom." >&2
  exit 1
fi

docker compose -f compose.classroom.yml up -d --no-build classroom frontend

for _ in {1..90}; do
  if curl -fsS "http://localhost:${classroom_port}/actuator/health" >/dev/null 2>&1; then
    echo "Classroom is ready: http://localhost:${classroom_port}"
    exit 0
  fi
  sleep 2
done

echo 'Classroom did not become ready. Run ./scripts/classroom-status.sh for details.' >&2
exit 1
