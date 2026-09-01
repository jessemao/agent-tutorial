#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"

required_files=(
  "AGENTS.md"
  "docs/ai-governance/README.md"
  "docs/ai-governance/workflow.md"
  "docs/ai-governance/deliverables.md"
  "docs/ai-governance/standards/architecture.md"
  "docs/ai-governance/standards/clean-code.md"
  "docs/ai-governance/standards/testing.md"
  "docs/ai-governance/standards/documentation.md"
  "docs/ai-governance/templates/task-card.md"
  "docs/ai-governance/templates/spec.md"
  "docs/ai-governance/templates/ticket.md"
  "docs/ai-governance/templates/review.md"
  "docs/ai-governance/templates/impact.md"
  "docs/ai-governance/templates/agent-task.md"
  "docs/ai-governance/templates/verification.md"
  "docs/ai-governance/templates/code-review.md"
  "docs/ai-governance/templates/problem-review.md"
  "docs/ai-governance/templates/decision.md"
  "docs/ai-governance/templates/delivery.md"
  "platform-contracts/AGENTS.md"
  "platform-web-starter/AGENTS.md"
  "business-wms/AGENTS.md"
  "training-server/AGENTS.md"
  "skills-lock.json"
)

for required_file in "${required_files[@]}"; do
  if [[ ! -s "$required_file" ]]; then
    echo "[BLOCK] missing or empty: $required_file" >&2
    exit 1
  fi
done

required_heading_pairs=(
  "docs/ai-governance/templates/task-card.md|## 验收条件"
  "docs/ai-governance/templates/review.md|## 事实、假设与待确认项"
  "docs/ai-governance/templates/impact.md|## 修改边界"
  "docs/ai-governance/templates/verification.md|## 验收映射"
  "docs/ai-governance/templates/spec.md|## 审批记录"
  "docs/ai-governance/templates/ticket.md|## 验收条件"
  "docs/ai-governance/templates/agent-task.md|## 停止条件"
  "docs/ai-governance/templates/code-review.md|## Spec 符合性矩阵"
  "docs/ai-governance/templates/problem-review.md|## 防止重复"
  "docs/ai-governance/templates/decision.md|## 最终决定"
  "docs/ai-governance/templates/delivery.md|## 最终确认"
)

for required_heading_pair in "${required_heading_pairs[@]}"; do
  template_file="${required_heading_pair%%|*}"
  required_heading="${required_heading_pair#*|}"
  if ! rg -Fq "$required_heading" "$template_file"; then
    echo "[BLOCK] required heading missing in $template_file: $required_heading" >&2
    exit 1
  fi
done

clean_skills=(clean-comments clean-functions clean-general clean-names clean-tests)
for clean_skill in "${clean_skills[@]}"; do
  skill_file=".agents/skills/$clean_skill/SKILL.md"
  if [[ ! -s "$skill_file" ]] || ! rg -q "^name: $clean_skill$" "$skill_file"; then
    echo "[BLOCK] invalid Clean Code Skill: $skill_file" >&2
    exit 1
  fi
done

if command -v sha256sum >/dev/null 2>&1; then
  sha256sum -c docs/ai-governance/skills.sha256
elif command -v shasum >/dev/null 2>&1; then
  shasum -a 256 -c docs/ai-governance/skills.sha256
else
  echo "[BLOCK] sha256sum or shasum is required" >&2
  exit 1
fi

if rg -ni "baseline|基线|manifest\.sha256|BL-T" \
  AGENTS.md docs/ai-governance platform-contracts/AGENTS.md \
  platform-web-starter/AGENTS.md business-wms/AGENTS.md training-server/AGENTS.md; then
  echo "[BLOCK] removed governance terminology was reintroduced" >&2
  exit 1
fi

echo "[PASS] AI governance materials and project Skills are complete."
