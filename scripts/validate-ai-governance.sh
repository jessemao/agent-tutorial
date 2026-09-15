#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"

text_search() {
  if command -v rg >/dev/null 2>&1; then
    rg "$@"
  else
    local argument
    for argument in "$@"; do
      if [[ -d "$argument" ]]; then
        grep -r "$@"
        return
      fi
    done
    grep "$@"
  fi
}

required_files=(
  "AGENTS.md"
  "docs/ai-governance/README.md"
  "docs/ai-governance/workflow.md"
  "docs/ai-governance/deliverables.md"
  "docs/ai-governance/roles-and-approvals.md"
  "docs/ai-governance/validation.md"
  "docs/ai-governance/change-management.md"
  "docs/ai-governance/exceptions.md"
  "docs/ai-governance/standards/architecture.md"
  "docs/ai-governance/standards/requirements-design.md"
  "docs/ai-governance/standards/clean-code.md"
  "docs/ai-governance/standards/ai-security.md"
  "docs/ai-governance/standards/testing.md"
  "docs/ai-governance/standards/documentation.md"
  "docs/work-items/README.md"
  "docs/ai-governance/templates/work-item.md"
  "docs/ai-governance/templates/input-evidence.md"
  "docs/ai-governance/templates/task-card.md"
  "docs/ai-governance/templates/spec.md"
  "docs/ai-governance/templates/ticket.md"
  "docs/ai-governance/templates/design.md"
  "docs/ai-governance/templates/interface.md"
  "docs/ai-governance/templates/analysis.md"
  "docs/ai-governance/templates/review.md"
  "docs/ai-governance/templates/verification.md"
  "docs/ai-governance/templates/decision.md"
  "docs/ai-governance/templates/functional-test.md"
  "docs/ai-governance/templates/qa-review.md"
  "platform-contracts/AGENTS.md"
  "platform-web-starter/AGENTS.md"
  "business-wms/AGENTS.md"
  "training-server/AGENTS.md"
  ".agents/skills/project-system-of-record/SKILL.md"
  ".agents/skills/project-system-of-record/agents/openai.yaml"
  ".agents/skills/project-system-of-record/references/lifecycle.md"
  ".agents/skills/project-system-of-record/references/process-logging.md"
  ".agents/skills/init-work-item/SKILL.md"
  ".agents/skills/init-work-item/agents/openai.yaml"
  "skills-lock.json"
)

for required_file in "${required_files[@]}"; do
  if [[ ! -s "$required_file" ]]; then
    echo "[BLOCK] missing or empty: $required_file" >&2
    exit 1
  fi
done

required_governance_headings=(
  "docs/ai-governance/roles-and-approvals.md|## 2. 决策权矩阵"
  "docs/ai-governance/validation.md|## 2. 当前真实能力"
  "docs/ai-governance/change-management.md|## 4. 在途 Work Item"
  "docs/ai-governance/exceptions.md|## 1. 当前有效例外"
  "docs/ai-governance/standards/ai-security.md|## 2. 不可信指令与 Prompt Injection"
  "docs/ai-governance/standards/requirements-design.md|## 3. Spec 批准前检查"
)

for required_governance_heading in "${required_governance_headings[@]}"; do
  governance_file="${required_governance_heading%%|*}"
  governance_heading="${required_governance_heading#*|}"
  if ! text_search -Fq "$governance_heading" "$governance_file"; then
    echo "[BLOCK] required governance heading missing in $governance_file: $governance_heading" >&2
    exit 1
  fi
done

required_heading_pairs=(
  "docs/ai-governance/templates/work-item.md|## 产物适用性"
  "docs/ai-governance/templates/input-evidence.md|## 证据边界"
  "docs/ai-governance/templates/task-card.md|## 验收条件"
  "docs/ai-governance/templates/design.md|## 备选方案与取舍"
  "docs/ai-governance/templates/interface.md|## 错误与边界行为"
  "docs/ai-governance/templates/analysis.md|## 影响与边界"
  "docs/ai-governance/templates/verification.md|## 验收映射"
  "docs/ai-governance/templates/spec.md|## 审批记录"
  "docs/ai-governance/templates/spec.md|## 用户旅程与交互决定"
  "docs/ai-governance/templates/interface.md|## UI 交互契约"
  "docs/ai-governance/templates/ticket.md|## 验收条件"
  "docs/ai-governance/templates/review.md|## Spec 符合性矩阵"
  "docs/ai-governance/templates/review.md|## 问题复盘"
  "docs/ai-governance/templates/decision.md|## 最终决定"
)

for required_heading_pair in "${required_heading_pairs[@]}"; do
  template_file="${required_heading_pair%%|*}"
  required_heading="${required_heading_pair#*|}"
  if ! text_search -Fq "$required_heading" "$template_file"; then
    echo "[BLOCK] required heading missing in $template_file: $required_heading" >&2
    exit 1
  fi
done

for work_item_dir in docs/work-items/*/; do
  [[ -d "$work_item_dir" ]] || continue
  work_item_id="$(basename "$work_item_dir")"
  if [[ ! "$work_item_id" =~ ^[A-Za-z0-9][A-Za-z0-9_-]*$ ]]; then
    echo "[BLOCK] invalid Work Item ID: $work_item_id" >&2
    exit 1
  fi
  work_item_readme="${work_item_dir}README.md"
  if [[ ! -s "$work_item_readme" ]]; then
    echo "[BLOCK] Work Item README is missing or empty: $work_item_readme" >&2
    exit 1
  fi
  for work_item_heading in "## 任务身份" "## 版本与输入" "## 产物适用性" "## 人工批准点"; do
    if ! text_search -Fq "$work_item_heading" "$work_item_readme"; then
      echo "[BLOCK] required Work Item heading missing in $work_item_readme: $work_item_heading" >&2
      exit 1
    fi
  done

  work_item_status="$(sed -n 's/^| 状态 | `\{0,1\}\([^`|]*\)`\{0,1\} |$/\1/p' "$work_item_readme" | head -n 1 | xargs)"
  if [[ -z "$work_item_status" ]]; then
    echo "[BLOCK] Work Item status cannot be parsed: $work_item_readme" >&2
    exit 1
  fi

  require_stage_file() {
    local stage_file="$work_item_dir$1"
    if [[ ! -s "$stage_file" ]]; then
      echo "[BLOCK] $work_item_id status $work_item_status requires: $stage_file" >&2
      exit 1
    fi
  }

  forbid_stage_file() {
    local stage_file="$work_item_dir$1"
    if [[ -e "$stage_file" ]]; then
      echo "[BLOCK] $work_item_id status $work_item_status must not create future-stage file: $stage_file" >&2
      exit 1
    fi
  }

  case "$work_item_status" in
    DRAFT|DISCOVERING)
      forbid_stage_file "functional-test.md"
      forbid_stage_file "qa-review.md"
      forbid_stage_file "02_verification.md"
      forbid_stage_file "03_review.md"
      forbid_stage_file "04_decision.md"
      ;;
    WAITING_FOR_SCOPE_APPROVAL)
      require_stage_file "01_analysis.md"
      forbid_stage_file "02_verification.md"
      forbid_stage_file "03_review.md"
      forbid_stage_file "04_decision.md"
      ;;
    APPROVED_FOR_IMPLEMENTATION|IMPLEMENTING)
      require_stage_file "01_analysis.md"
      forbid_stage_file "03_review.md"
      forbid_stage_file "04_decision.md"
      ;;
    REVIEWING)
      require_stage_file "01_analysis.md"
      require_stage_file "02_verification.md"
      forbid_stage_file "04_decision.md"
      ;;
    FUNCTIONAL_TESTING)
      require_stage_file "01_analysis.md"
      require_stage_file "02_verification.md"
      require_stage_file "03_review.md"
      forbid_stage_file "qa-review.md"
      forbid_stage_file "04_decision.md"
      ;;
    QA_REVIEWING)
      require_stage_file "01_analysis.md"
      require_stage_file "02_verification.md"
      require_stage_file "03_review.md"
      require_stage_file "functional-test.md"
      forbid_stage_file "04_decision.md"
      ;;
    WAITING_FOR_DELIVERY_DECISION)
      require_stage_file "01_analysis.md"
      require_stage_file "02_verification.md"
      require_stage_file "03_review.md"
      forbid_stage_file "04_decision.md"
      ;;
    ACCEPTED|ROLLED_BACK)
      require_stage_file "01_analysis.md"
      require_stage_file "02_verification.md"
      require_stage_file "03_review.md"
      require_stage_file "04_decision.md"
      ;;
    REWORK|BLOCKED)
      # These states may occur at any stage; the README must explain the blocker.
      ;;
    *)
      echo "[BLOCK] unsupported Work Item status '$work_item_status': $work_item_readme" >&2
      exit 1
      ;;
  esac
done

deprecated_process_filenames=(
  "01_review.md" "02_impact.md" "03_agent-task.md" "04_verification.md"
  "code-review.md" "05_problem-review.md" "06_decision.md" "07_delivery.md"
)

for deprecated_process_filename in "${deprecated_process_filenames[@]}"; do
  while IFS= read -r deprecated_file; do
    [[ -z "$deprecated_file" ]] && continue
    echo "[BLOCK] deprecated process document; migrate to the five-document model: $deprecated_file" >&2
    exit 1
  done < <(find docs/work-items -type f -name "$deprecated_process_filename" -print)
done

process_filenames=(
  "functional-test.md" "qa-review.md"
  "spec.md" "design.md" "interface.md"
  "01_analysis.md" "02_verification.md" "03_review.md" "04_decision.md"
  "01_review.md" "02_impact.md" "03_agent-task.md" "04_verification.md"
  "code-review.md" "05_problem-review.md" "06_decision.md" "07_delivery.md"
)

for process_filename in "${process_filenames[@]}"; do
  while IFS= read -r misplaced_file; do
    [[ -z "$misplaced_file" ]] && continue
    echo "[BLOCK] task process document is outside docs/work-items/: $misplaced_file" >&2
    exit 1
  done < <(
    find docs/ai-governance docs/training -type f -name "$process_filename" \
      ! -path "docs/ai-governance/templates/*" -print
    find . -maxdepth 1 -type f -name "$process_filename" -print
  )
done

clean_skills=(clean-comments clean-functions clean-general clean-names clean-tests)
for clean_skill in "${clean_skills[@]}"; do
  skill_file=".agents/skills/$clean_skill/SKILL.md"
  if [[ ! -s "$skill_file" ]] || ! text_search -q "^name: $clean_skill$" "$skill_file"; then
    echo "[BLOCK] invalid Clean Code Skill: $skill_file" >&2
    exit 1
  fi
done

if ! text_search -q '^name: project-system-of-record$' \
  .agents/skills/project-system-of-record/SKILL.md; then
  echo "[BLOCK] invalid project-level project-system-of-record Skill" >&2
  exit 1
fi

if ! text_search -Fq 'project-system-of-record' AGENTS.md; then
  echo "[BLOCK] AGENTS.md does not require project-system-of-record" >&2
  exit 1
fi

if ! text_search -Fq 'allow_implicit_invocation: true' \
  .agents/skills/project-system-of-record/agents/openai.yaml; then
  echo "[BLOCK] project-system-of-record implicit invocation is disabled" >&2
  exit 1
fi

fully_locked_skills=(codebase-design)
for fully_locked_skill in "${fully_locked_skills[@]}"; do
  skill_dir=".agents/skills/$fully_locked_skill"
  skill_file="$skill_dir/SKILL.md"
  if [[ ! -s "$skill_file" ]] || ! text_search -q "^name: $fully_locked_skill$" "$skill_file"; then
    echo "[BLOCK] invalid fully locked Skill: $skill_file" >&2
    exit 1
  fi

  while IFS= read -r locked_skill_file; do
    [[ -z "$locked_skill_file" ]] && continue
    if ! text_search -Fq "  $locked_skill_file" docs/ai-governance/skills.sha256; then
      echo "[BLOCK] Skill file is missing from checksum manifest: $locked_skill_file" >&2
      exit 1
    fi
  done < <(find "$skill_dir" -type f -print)
done

while read -r locked_hash locked_path; do
  [[ -z "${locked_hash:-}" || -z "${locked_path:-}" ]] && continue
  if ! text_search -Fq "\"$locked_hash\"" skills-lock.json; then
    echo "[BLOCK] checksum is not recorded in skills-lock.json: $locked_path" >&2
    exit 1
  fi
done < docs/ai-governance/skills.sha256

if command -v sha256sum >/dev/null 2>&1; then
  sha256sum -c docs/ai-governance/skills.sha256
elif command -v shasum >/dev/null 2>&1; then
  shasum -a 256 -c docs/ai-governance/skills.sha256
else
  echo "[BLOCK] sha256sum or shasum is required" >&2
  exit 1
fi

if text_search -ni "baseline|基线|manifest\.sha256|BL-T" \
  AGENTS.md docs/ai-governance platform-contracts/AGENTS.md \
  platform-web-starter/AGENTS.md business-wms/AGENTS.md training-server/AGENTS.md; then
  echo "[BLOCK] removed governance terminology was reintroduced" >&2
  exit 1
fi

if text_search -n "\.scratch/" AGENTS.md STANDARDS.md docs/ai-governance docs/agents docs/work-items; then
  echo "[BLOCK] retired Work Item path was reintroduced into current governance documents" >&2
  exit 1
fi

if ! text_search -Fq 'STD-WMS-0.7-06' STANDARDS.md || \
   ! text_search -Fq 'docs/ai-governance/standards/ai-security.md' STANDARDS.md; then
  echo "[BLOCK] current Standards ID or AI security routing is missing" >&2
  exit 1
fi

echo "[PASS] AI governance materials and project Skills are complete."
