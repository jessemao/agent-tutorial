#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

if [[ $# -ne 1 ]]; then
  echo 'Usage: ./scripts/export-v2-task1-student.sh <new-output-directory>' >&2
  exit 2
fi

source_ref='v2.0-task1-start'
output_dir="$1"
if [[ "$output_dir" != /* ]]; then
  output_dir="$PWD/$output_dir"
fi
if [[ -e "$output_dir" ]]; then
  echo "[BLOCK] output directory already exists: $output_dir" >&2
  exit 1
fi
if ! git cat-file -e "${source_ref}^{commit}" 2>/dev/null; then
  echo "[BLOCK] source tag does not resolve to a commit: $source_ref" >&2
  exit 1
fi

./scripts/validate-ai-governance.sh baseline
./scripts/validate-v2-task1.sh baseline

mkdir -p "$(dirname "$output_dir")"
temp_dir="$(mktemp -d "${TMPDIR:-/tmp}/training-wms-v2-task1.XXXXXX")"
cleanup() {
  if [[ -n "${temp_dir:-}" && -d "$temp_dir" ]]; then
    rm -rf -- "$temp_dir"
  fi
}
trap cleanup EXIT

git archive "$source_ref" | tar -x -C "$temp_dir"
# Scenario patches and historical task notes are instructor material, not part
# of the learner repository. Keep the source repository useful for instructors
# while making the exported baseline contain only the canonical V2 entrypoints.
rm -rf -- "$temp_dir/scenarios"
source_commit="$(git rev-parse "${source_ref}^{commit}")"
printf 'source-ref=%s\nsource-commit=%s\n' "$source_ref" "$source_commit" \
  > "$temp_dir/.student-baseline"

git -C "$temp_dir" init -q -b main
git -C "$temp_dir" add -A
git -C "$temp_dir" \
  -c user.name='Training WMS Course' \
  -c user.email='training-wms@example.invalid' \
  commit -q -m 'baseline(v2): create isolated task-one learner repository'
git -C "$temp_dir" tag "$source_ref"

(
  cd "$temp_dir"
  ./scripts/validate-ai-governance.sh baseline
  ./scripts/validate-v2-task1.sh baseline
  git diff --check
)

mv "$temp_dir" "$output_dir"
temp_dir=''
echo "[PASS] isolated learner repository created: $output_dir"
