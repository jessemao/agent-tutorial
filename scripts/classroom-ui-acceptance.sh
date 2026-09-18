#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

if [[ "${1:-}" != "TASK1" ]] || [[ ! "${2:-}" =~ ^V2-T1-G[0-9]{2,}$ ]]; then
  echo 'Usage: ./scripts/classroom-ui-acceptance.sh TASK1 V2-T1-Gxx' >&2
  exit 2
fi

group_id="$2"
classroom_port="${CLASSROOM_PORT:-8080}"
run_id="${ACCEPTANCE_RUN_ID:-$(date '+%Y%m%d-%H%M%S')}"
work_item_dir="docs/work-items/$group_id"
relative_output="$work_item_dir/artifacts/ui-acceptance/$run_id"
output_dir="$repo_dir/$relative_output"

if [[ ! -d "$work_item_dir" ]]; then
  echo "[BLOCK] Work Item directory does not exist: $work_item_dir" >&2
  exit 1
fi
if ! curl -fsS "http://localhost:${classroom_port}/actuator/health" >/dev/null; then
  echo '[BLOCK] Classroom is not ready. Run ./scripts/classroom-up.sh first.' >&2
  exit 1
fi
if [[ -e "$output_dir" ]]; then
  echo "[BLOCK] Acceptance output already exists: $relative_output" >&2
  exit 1
fi

mkdir -p "$output_dir"
candidate_commit="$(git rev-parse HEAD)"
started_at="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

set +e
docker compose -f compose.classroom.yml exec -T \
  -e PLAYWRIGHT_BASE_URL=http://127.0.0.1:5173 \
  -e PLAYWRIGHT_ARTIFACT_DIR="/workspace/$relative_output" \
  frontend npm run test:e2e
exit_code=$?
set -e

finished_at="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
{
  echo "work_item=$group_id"
  echo "candidate_commit=$candidate_commit"
  echo "started_at=$started_at"
  echo "finished_at=$finished_at"
  echo "playwright_exit_code=$exit_code"
} > "$output_dir/run.txt"

echo "[EVIDENCE] $relative_output"
echo '[NOTE] The script records execution evidence only; people decide PASS / RETURN / BLOCKED.'
exit "$exit_code"
