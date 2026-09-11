#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"

skills=(
  work-item-start
  work-item-discover
  work-item-execute
  work-item-review
  work-item-decision
)

required_baseline_files=(
  docs/training/T05_平台组件与Skill复用_学员任务卡.md
  docs/training/T05_平台组件与Skill复用_讲师参考.md
  docs/training/T05_平台组件与Skill复用_评分细则.md
  docs/ai-governance/templates/work-item.md
  docs/ai-governance/templates/input-evidence.md
  docs/ai-governance/templates/analysis.md
  docs/ai-governance/templates/verification.md
  docs/ai-governance/templates/review.md
  docs/ai-governance/templates/decision.md
  docs/ai-governance/templates/functional-test.md
  docs/ai-governance/templates/qa-review.md
  skills-lock.json
)

for required_file in "${required_baseline_files[@]}"; do
  if [[ ! -s "$required_file" ]]; then
    echo "[BLOCK] T05 baseline file is missing or empty: $required_file" >&2
    exit 1
  fi
done

hash_file() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    shasum -a 256 "$1" | awk '{print $1}'
  fi
}

validate_skill() {
  local skill="$1"
  local skill_dir=".agents/skills/$skill"
  local skill_file="$skill_dir/SKILL.md"
  local agent_file="$skill_dir/agents/openai.yaml"

  if [[ ! -s "$skill_file" || ! -s "$agent_file" ]]; then
    echo "[BLOCK] T05 Skill files are incomplete: $skill_dir" >&2
    exit 1
  fi

  if ! rg -q "^name: $skill$" "$skill_file"; then
    echo "[BLOCK] T05 Skill frontmatter name does not match: $skill_file" >&2
    exit 1
  fi

  for heading in \
    "## 输入" "## 读取顺序" "## 允许写入" "## 固定产物" \
    "## 停止条件" "## 禁止动作" "## 下一入口" "## 验收"; do
    if ! rg -Fq "$heading" "$skill_file"; then
      echo "[BLOCK] T05 Skill contract heading is missing in $skill_file: $heading" >&2
      exit 1
    fi
  done

  if ! rg -Fq 'allow_implicit_invocation: false' "$agent_file"; then
    echo "[BLOCK] T05 stage Skill must require explicit invocation: $agent_file" >&2
    exit 1
  fi

  local lock_block
  lock_block="$(sed -n "/    \"$skill\": {/,/^    }/p" skills-lock.json)"
  if [[ -z "$lock_block" ]] || ! rg -Fq '"source": "training-wms"' <<<"$lock_block"; then
    echo "[BLOCK] T05 Skill project source is not registered: $skill" >&2
    exit 1
  fi

  local recorded_hash
  recorded_hash="$(sed -n 's/.*"computedHash": "\([0-9a-f]\{64\}\)".*/\1/p' <<<"$lock_block" | head -n 1)"
  local actual_hash
  actual_hash="$(hash_file "$skill_file")"
  if [[ -z "$recorded_hash" || "$recorded_hash" != "$actual_hash" ]]; then
    echo "[BLOCK] T05 Skill hash is missing or stale: $skill" >&2
    exit 1
  fi

  echo "$skill: OK"
}

mode="${1:-}"
case "$mode" in
  baseline)
    for skill in "${skills[@]}"; do
      if [[ -e ".agents/skills/$skill" ]]; then
        echo "[BLOCK] T05 answer leaked into the classroom baseline: .agents/skills/$skill" >&2
        exit 1
      fi
    done
    echo '[PASS] T05 classroom baseline is ready.'
    ;;
  all)
    for skill in "${skills[@]}"; do
      validate_skill "$skill"
    done
    echo '[PASS] all T05 project Skills are registered and structurally valid.'
    ;;
  work-item-start|work-item-discover|work-item-execute|work-item-review|work-item-decision)
    validate_skill "$mode"
    ;;
  *)
    echo 'Usage: ./scripts/validate-t05-skills.sh baseline|all|work-item-start|work-item-discover|work-item-execute|work-item-review|work-item-decision' >&2
    exit 2
    ;;
esac
