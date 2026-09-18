#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

mode="${1:-baseline}"
work_item_id="${2:-}"

usage() {
  echo 'Usage: ./scripts/validate-v2-task1.sh baseline|m1|m2|m3|m4|m5 [V2-T1-Gxx]' >&2
  exit 2
}

case "$mode" in
  baseline|m1|m2|m3|m4|m5) ;;
  *) usage ;;
esac

required_skills=(
  grill-with-docs codebase-design to-spec to-tickets implement code-review
)
for skill in "${required_skills[@]}"; do
  if [[ ! -s ".agents/skills/$skill/SKILL.md" ]]; then
    echo "[BLOCK] V2 task-one Skill is missing: $skill" >&2
    exit 1
  fi
done

retired_skills=(
  work-item-start work-item-discover work-item-execute work-item-review work-item-decision
)
for skill in "${retired_skills[@]}"; do
  if [[ -e ".agents/skills/$skill/SKILL.md" ]] || rg -Fq "\"$skill\"" skills-lock.json; then
    echo "[BLOCK] T05 aggregate Skill must not be present in the V2 baseline: $skill" >&2
    exit 1
  fi
done

required_files=(
  docs/training/V2_任务一_库存盘点与差异调整_学员任务卡.md
  docs/training/task1-acceptance/README.md
  docs/training/task1-acceptance/01_UI盘点与差异调整_验收任务卡.md
  docs/training/task1-acceptance/02_UI驳回与状态保护_验收任务卡.md
  docs/training/task1-acceptance/03_Playwright自动化UI验收_验收任务卡.md
  docs/ai-governance/templates/reuse-decision.md
  scripts/classroom-ui-acceptance.sh
  training-server/frontend/e2e/inventory-count.spec.ts
  training-server/frontend/playwright.config.ts
  training-server/src/test/resources/application-mysql-verification.yml
)
for file in "${required_files[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "[BLOCK] V2 task-one material is missing: $file" >&2
    exit 1
  fi
done

task_card="${required_files[0]}"
for phase in M1 M2 M3 M4 M5; do
  if ! rg -Fq "$phase" "$task_card"; then
    echo "[BLOCK] V2 task card is missing phase $phase" >&2
    exit 1
  fi
done
if ! rg -Fq 'reuse-decision.md' "$task_card"; then
  echo '[BLOCK] V2 task card does not require the M5 reuse decision.' >&2
  exit 1
fi
if ! rg -Fq '/to-tickets V2-T1-Gxx' "$task_card" || \
   ! rg -Fq 'docs/work-items/V2-T1-Gxx/tickets/' "$task_card"; then
  echo '[BLOCK] V2 task card must route conditional to-tickets output into the current Work Item.' >&2
  exit 1
fi

if ! rg -Fq 'image: mysql:8.0' compose.classroom.yml || \
   ! rg -Fq 'PLAYWRIGHT_BROWSERS_PATH: /opt/ms-playwright' compose.classroom.yml || \
   ! rg -Fq 'TASK1 baseline|module|all|mysql' scripts/classroom-test.sh; then
  echo '[BLOCK] V2 task-one MySQL or UI acceptance runtime is incomplete.' >&2
  exit 1
fi

count_source='business-wms/src/main/java/com/acme/training/wms/inventory/InventoryCountService.java'
if [[ "$mode" == baseline ]]; then
  if [[ -f .student-baseline ]]; then
    recorded_ref="$(sed -n 's/^source-ref=//p' .student-baseline | head -n 1)"
    recorded_commit="$(sed -n 's/^source-commit=//p' .student-baseline | head -n 1)"
    if [[ "$recorded_ref" != 'v2.0-task1-start' || ! "$recorded_commit" =~ ^[0-9a-f]{40}$ ]]; then
      echo '[BLOCK] .student-baseline does not contain valid source provenance.' >&2
      exit 1
    fi
    if [[ "$(git rev-parse 'v2.0-task1-start^{commit}')" != "$(git rev-parse HEAD)" ]]; then
      echo '[BLOCK] learner baseline tag must point to the isolated repository HEAD.' >&2
      exit 1
    fi
    if git diff --name-only v2.0-task1-start -- | rg -q . || \
       git ls-files --others --exclude-standard | rg -q .; then
      echo '[BLOCK] learner baseline contains uncommitted changes.' >&2
      exit 1
    fi
    if git remote | rg -q .; then
      echo '[BLOCK] learner repository must not contain a remote.' >&2
      exit 1
    fi
    if [[ "$(git tag --list)" != 'v2.0-task1-start' ]]; then
      echo '[BLOCK] learner repository must contain only the v2.0-task1-start tag.' >&2
      exit 1
    fi
    if git rev-list --all --objects | rg -q \
      'business-wms/src/main/java/com/acme/training/wms/count/|training-server/src/test/.*/(T06Count|InventoryCount)|docs/work-items/T06-'; then
      echo '[BLOCK] historical inventory-count answer is reachable in learner repository history.' >&2
      exit 1
    fi
  fi
  if [[ -e .agents/skills/init-work-item ]]; then
    echo '[BLOCK] the V2 baseline must not ship a prebuilt init-work-item Skill; M1 only creates the Work Item record.' >&2
    exit 1
  fi
  if [[ -e "$count_source" ]] || \
     find training-server/src/test -type f \( -name 'T06Count*' -o -name 'InventoryCount*' \) -print -quit | rg -q . || \
     rg -q 'Stocktake|stocktake|COUNT_ADJUST|adjustFromCount' \
       business-wms/src/main training-server/src/main training-server/src/test; then
    echo '[BLOCK] inventory-count answer capability leaked into the V2 baseline.' >&2
    exit 1
  fi
  echo '[PASS] V2 task-one baseline boundary is valid.'
  exit 0
fi

if [[ ! "$work_item_id" =~ ^V2-T1-G[0-9]{2,}$ ]]; then
  usage
fi

# V2 基线不携带 docs/work-items/README.md；阶段门禁沿用基线治理校验，
# 再由本脚本检查当前 Work Item 的阶段材料。
./scripts/validate-ai-governance.sh baseline

work_item="docs/work-items/$work_item_id"
readme="$work_item/README.md"

require_file() {
  if [[ ! -s "$work_item/$1" ]]; then
    echo "[BLOCK] $mode requires: $work_item/$1" >&2
    exit 1
  fi
}

require_dir() {
  if [[ ! -d "$work_item/$1" ]]; then
    echo "[BLOCK] $mode requires directory: $work_item/$1" >&2
    exit 1
  fi
}

forbid_path() {
  if [[ -e "$work_item/$1" ]]; then
    echo "[BLOCK] $mode must not create future-stage material: $work_item/$1" >&2
    exit 1
  fi
}

require_heading() {
  local file="$1"
  local heading="$2"
  if ! rg -Fq "$heading" "$file"; then
    echo "[BLOCK] required heading missing in $file: $heading" >&2
    exit 1
  fi
}

require_table_value() {
  local file="$1"
  local label="$2"
  if ! awk -F '|' -v expected="$label" '
    function trim(value) {
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      return value
    }
    trim($2) == expected {
      value = trim($3)
      if (value != "" && value !~ /^(待补充|待指定|TBD|<|按企业流程指定)/) found = 1
    }
    END { exit !found }
  ' "$file"; then
    echo "[BLOCK] required value missing or still a placeholder in $file: $label" >&2
    exit 1
  fi
}

require_list_value() {
  local file="$1"
  local label="$2"
  if ! awk -v expected="- ${label}：" '
    index($0, expected) == 1 {
      value = substr($0, length(expected) + 1)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      if (value != "" && value !~ /^(待补充|待指定|TBD|<)/) found = 1
    }
    END { exit !found }
  ' "$file"; then
    echo "[BLOCK] required value missing or still a placeholder in $file: $label" >&2
    exit 1
  fi
}

require_confirmed_grill_role() {
  local file="$1"
  local role="$2"
  if ! awk -F '|' -v expected="$role" '
    function trim(value) {
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      return value
    }
      trim($2) == expected &&
      trim($3) != "" && trim($4) != "" && trim($5) != "" &&
      trim($7) == "CONFIRMED" { found = 1 }
    END { exit !found }
  ' "$file"; then
    echo "[BLOCK] M2 requires a confirmed, named Grill decision from $role in $file." >&2
    exit 1
  fi
}

require_status() {
  local expected="$1"
  local actual
  actual="$(sed -n 's/^| 状态 | `\{0,1\}\([^`|]*\)`\{0,1\} |$/\1/p' "$readme" | head -n 1 | xargs)"
  if [[ "$actual" != "$expected" ]]; then
    echo "[BLOCK] $mode requires Work Item status $expected; found ${actual:-unparseable}." >&2
    exit 1
  fi
}

changed_files() {
  {
    git diff --name-only v2.0-task1-start --
    git ls-files --others --exclude-standard
  } | sort -u
}

require_changes_within() {
  local phase="$1"
  local changed
  while IFS= read -r changed; do
    [[ -z "$changed" ]] && continue
    case "$phase:$changed" in
      m1:"$work_item/README.md"|m1:"$work_item/inputs/"*) ;;
      m2:"$work_item/README.md"|m2:"$work_item/inputs/"*|m2:"$work_item/01_analysis.md"|m2:"$work_item/spec.md"|m2:"$work_item/design.md"|m2:"$work_item/interface.md"|m2:"$work_item/tickets/"*) ;;
      m3:"$work_item/"*|m3:business-wms/*|m3:training-server/*) ;;
      m4:"$work_item/"*|m4:business-wms/*|m4:training-server/*) ;;
      m5:"$work_item/"*|m5:business-wms/*|m5:training-server/*|m5:.agents/skills/*) ;;
      *)
        echo "[BLOCK] $phase change is outside the approved task boundary: $changed" >&2
        exit 1
        ;;
    esac
  done < <(changed_files)
}

require_m1_materials() {
  require_file README.md
  require_dir inputs
  for heading in '## 任务身份' '## 版本与输入' '## 目标与范围摘要' '## 人工批准点'; do
    require_heading "$readme" "$heading"
  done
  if ! rg -Fq 'v2.0-task1-start' "$readme"; then
    echo "[BLOCK] $readme must record v2.0-task1-start as the code baseline." >&2
    exit 1
  fi
  for field in '业务/规则所有者' '开发负责人（实施前批准人）' '测试责任方 / 企业流程' '代码起点' 'Standards ID/版本' '基线验证命令与结果'; do
    require_table_value "$readme" "$field"
  done
  for field in '允许修改' '禁止修改' 'Agent 必须停止'; do
    require_list_value "$readme" "$field"
  done
}

require_m2_materials() {
  require_m1_materials
  require_file 01_analysis.md
  require_file spec.md
  require_file design.md
  require_dir tickets
  for heading in '## 元数据' '## 输入与事实' '## M2 三角色决定记录' '## 定位或设计结论' '## 影响与边界' '## 实施任务' '## 开发负责人实施批准'; do
    require_heading "$work_item/01_analysis.md" "$heading"
  done
  for role in '项目经理 / 业务所有者' '开发负责人' '测试工程师'; do
    require_confirmed_grill_role "$work_item/01_analysis.md" "$role"
  done
  for heading in '## 元数据' '## 范围与非目标' '## 业务规则与设计决定' '## 用户旅程与交互决定' '## 接口、数据与事务' '## 验收条件' '## 审批记录'; do
    require_heading "$work_item/spec.md" "$heading"
  done
  for heading in '## 元数据' '## 现状与约束' '## 方案' '## 备选方案与取舍' '## 测试接缝' '## 待裁决项与批准'; do
    require_heading "$work_item/design.md" "$heading"
  done
  if ! rg -q '^\| 状态 \|.*APPROVED.*\|$' "$work_item/spec.md" || \
     ! rg -q '^\| 状态 \|.*APPROVED.*\|$' "$work_item/design.md"; then
    echo '[BLOCK] M2 requires APPROVED Spec and Design states.' >&2
    exit 1
  fi
  if ! rg -q '^\| 实施前 \|[^|]+\|[^|]+\|[^|]*(APPROVED|批准)[^|]*\|$' "$readme"; then
    echo "[BLOCK] M2 requires a recorded human implementation approval in $readme." >&2
    exit 1
  fi
  if ! rg -q '^\| (业务|项目经理) \|[[:space:]]*[^|[:space:]][^|]*\|[[:space:]]*APPROVED[[:space:]]*\|[[:space:]]*[^|[:space:]][^|]*\|' "$work_item/spec.md" || \
     ! rg -q '^\|[^|]+\|[[:space:]]*[^|[:space:]][^|]*\|[[:space:]]*APPROVED[[:space:]]*\|[[:space:]]*[^|[:space:]][^|]*\|$' "$work_item/design.md"; then
    echo '[BLOCK] M2 requires named, dated human approvals in Spec and Design.' >&2
    exit 1
  fi

  local tickets=("$work_item"/tickets/*.md)
  if [[ ! -e "${tickets[0]}" ]] || (( ${#tickets[@]} < 2 )); then
    echo '[BLOCK] M2 requires at least two vertical implementation Tickets.' >&2
    exit 1
  fi
  for ticket in "${tickets[@]}"; do
    require_heading "$ticket" '## 交付行为'
    require_heading "$ticket" '## 修改边界'
    require_heading "$ticket" '## 验收条件'
    require_heading "$ticket" '## 验证命令'
    require_heading "$ticket" '## 停止条件'
    local ticket_status_pattern='READY'
    if [[ "$mode" == "m3" || "$mode" == "m4" || "$mode" == "m5" ]]; then ticket_status_pattern='READY|DONE'; fi
    if ! rg -q "^\\*\\*状态：\\*\\*[[:space:]]*(${ticket_status_pattern})[[:space:]]*$" "$ticket" || \
       ! rg -q '^\*\*Blocked by：\*\*[[:space:]]*[^[:space:]<].*$' "$ticket" || \
       ! rg -q '^\*\*Spec：\*\*[[:space:]]*[^[:space:]<].*$' "$ticket" || \
       ! rg -q '^\*\*Standards：\*\*[[:space:]]*[^[:space:]<].*$' "$ticket" || \
       ! rg -q '^\*\*负责人：\*\*[[:space:]]*[^[:space:]<].*$' "$ticket" || \
       ! rg -q '^\*\*执行人：\*\*[[:space:]]*[^[:space:]<].*$' "$ticket" || \
       ! rg -q '^- \[[[:space:]]\][[:space:]]+[^[:space:]].*$' "$ticket"; then
      echo "[BLOCK] M2 Ticket does not satisfy the project template: $ticket" >&2
      exit 1
    fi
  done
}

require_m3_materials() {
  require_m2_materials
  if [[ ! -s "$count_source" ]]; then
    echo '[BLOCK] M3 requires inventory-count code.' >&2
    exit 1
  fi
  if ! {
    git diff --name-only v2.0-task1-start -- training-server/src/test
    git ls-files --others --exclude-standard -- training-server/src/test
  } | sort -u | rg -q '\.(java|kt|groovy)$'; then
    echo '[BLOCK] M3 requires at least one task test changed under training-server/src/test.' >&2
    exit 1
  fi
  while IFS= read -r ticket; do
    if ! rg -q '^\*\*状态：\*\*[[:space:]]*DONE[[:space:]]*$' "$ticket"; then
      echo "[BLOCK] M3 requires every Ticket to be implemented and personally verified by its executor: $ticket" >&2
      exit 1
    fi
    require_heading "$ticket" '## 实施证据'
    for evidence in '实施提交/证据' '个人验收命令' '个人验收结果' '完成时间'; do
      if ! rg -q "^- ${evidence}：[[:space:]]*[^[:space:]].*$" "$ticket"; then
        echo "[BLOCK] Ticket evidence is incomplete in $ticket: $evidence" >&2
        exit 1
      fi
      if rg -q "^- ${evidence}：[[:space:]]*(待补充|TBD|<)" "$ticket"; then
        echo "[BLOCK] Ticket evidence is still a placeholder in $ticket: $evidence" >&2
        exit 1
      fi
    done
  done < <(find "$work_item/tickets" -type f -name '*.md' -print | sort)
}

candidate_from_verification() {
  sed -n 's/^| 候选提交 | `\{0,1\}\([0-9a-f]\{7,40\}\)`\{0,1\} |$/\1/p' \
    "$work_item/02_verification.md" | head -n 1
}

require_m4_materials() {
  require_m3_materials
  require_file 02_verification.md
  require_file 03_review.md
  require_file 04_decision.md
  require_heading "$work_item/02_verification.md" '## 验收映射'
  require_heading "$work_item/03_review.md" '## Spec 符合性矩阵'
  require_heading "$work_item/03_review.md" '## Standards 符合性矩阵'
  require_heading "$work_item/04_decision.md" '## Finding 裁决'
  if ! rg -q '^READY_FOR_REVIEW[[:space:]]*$' "$work_item/02_verification.md"; then
    echo '[BLOCK] M4 requires a READY_FOR_REVIEW verification conclusion.' >&2
    exit 1
  fi
  if ! rg -q '^READY_FOR_DECISION[[:space:]]*$' "$work_item/03_review.md"; then
    echo '[BLOCK] M4 requires a READY_FOR_DECISION review recommendation.' >&2
    exit 1
  fi
  if rg -q '^\|[[:space:]]*[^|[:space:]][^|]*\|[^|]*\|[^|]*\|[^|]*\|[^|]*\|[[:space:]]*OPEN[[:space:]]*\|$' "$work_item/03_review.md"; then
    echo '[BLOCK] M4 cannot proceed while 03_review.md contains OPEN findings.' >&2
    exit 1
  fi
  if ! rg -q '^PASS[[:space:]]*$' "$work_item/04_decision.md"; then
    echo '[BLOCK] M4 requires a PASS human delivery decision.' >&2
    exit 1
  fi

  for layer in '目标测试' 'Module 回归' '全量验证'; do
    if ! awk -F '|' -v expected="$layer" '
      function trim(value) {
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
        return value
      }
      trim($2) == expected && trim($3) != "" && trim($4) == "PASS" &&
        trim($5) != "" && trim($6) != "" { found = 1 }
      END { exit !found }
    ' "$work_item/02_verification.md"; then
      echo "[BLOCK] M4 requires a complete PASS result for $layer in 02_verification.md." >&2
      exit 1
    fi
  done
  require_list_value "$work_item/02_verification.md" '跳过项及理由'
  if ! awk -F '|' '
    function trim(value) {
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      return value
    }
    trim($2) ~ /^AC-/ {
      rows++
      if (trim($3) == "" || trim($4) == "" || trim($5) == "" || trim($6) != "PASS") failed++
    }
    END { exit !(rows > 0 && failed == 0) }
  ' "$work_item/02_verification.md"; then
    echo '[BLOCK] M4 requires every AC mapping in 02_verification.md to be complete and PASS.' >&2
    exit 1
  fi
  if ! awk -F '|' '
    function trim(value) {
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      return value
    }
    trim($2) != "" && trim($2) != "要求" && trim($2) !~ /^-+$/ {
      rows++
      if (trim($3) == "" || trim($4) == "" || trim($5) == "" || trim($6) == "" || trim($7) != "PASS") failed++
    }
    END { exit !(rows > 0 && failed == 0) }
  ' "$work_item/03_review.md"; then
    echo '[BLOCK] M4 requires every Spec review row in 03_review.md to be complete and PASS.' >&2
    exit 1
  fi
  if ! awk -F '|' '
    function trim(value) {
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      return value
    }
    trim($2) != "" && trim($2) != "规则 ID" && trim($2) !~ /^-+$/ {
      rows++
      if (trim($3) !~ /^(MUST|REVIEW|N\/A)$/ || trim($4) == "" || trim($5) == "" || trim($6) == "" || trim($7) != "PASS") failed++
    }
    END { exit !(rows > 0 && failed == 0) }
  ' "$work_item/03_review.md"; then
    echo '[BLOCK] M4 requires every Standards review row in 03_review.md to be complete and PASS.' >&2
    exit 1
  fi
  for field in \
    '决策人/角色/时间' \
    '源分支/提交' \
    '目标分支' \
    'MR/PR 链接' \
    '自动检查' \
    '独立功能测试结论 / 测试责任方或企业流程 / 证据（未采用时 N/A 及理由）' \
    'QA 审核结论 / QA/质量责任方或企业流程 / 证据（未采用时 N/A 及理由）' \
    '业务验收原文 / 业务验收人 / 范围 / 时间' \
    '批准合并人/时间' \
    '实际合并结果' \
    '回滚点与方式' \
    '剩余风险与后续动作'; do
    require_list_value "$work_item/04_decision.md" "$field"
  done

  local candidate
  candidate="$(candidate_from_verification)"
  if [[ -z "$candidate" ]] || ! git cat-file -e "${candidate}^{commit}" 2>/dev/null || \
     ! git merge-base --is-ancestor "$candidate" HEAD; then
    echo '[BLOCK] 02_verification.md must identify an existing fixed candidate commit.' >&2
    exit 1
  fi
  if ! rg -Fq "$candidate" "$work_item/03_review.md" || \
     ! rg -Fq "$candidate" "$work_item/04_decision.md"; then
    echo '[BLOCK] Verification, Review and Decision must reference the same candidate commit.' >&2
    exit 1
  fi
  while IFS= read -r changed_after_candidate; do
    [[ -z "$changed_after_candidate" ]] && continue
    case "$changed_after_candidate" in
      "$work_item/README.md"|"$work_item/02_verification.md"|"$work_item/03_review.md"|"$work_item/04_decision.md"|"$work_item/functional-test.md"|"$work_item/qa-review.md"|"$work_item/artifacts/"*) ;;
      *)
        echo "[BLOCK] file changed after the fixed candidate; rerun M4: $changed_after_candidate" >&2
        exit 1
        ;;
    esac
  done < <({ git diff --name-only "$candidate" --; git ls-files --others --exclude-standard; } | sort -u)
}

require_m1_materials
require_changes_within "$mode"

case "$mode" in
  m1)
    require_status DISCOVERING
    for path in 01_analysis.md spec.md design.md interface.md tickets 02_verification.md 03_review.md 04_decision.md reuse-decision.md; do
      forbid_path "$path"
    done
    ;;
  m2)
    require_status APPROVED_FOR_IMPLEMENTATION
    require_m2_materials
    for path in 02_verification.md 03_review.md 04_decision.md reuse-decision.md; do
      forbid_path "$path"
    done
    ;;
  m3)
    require_status IMPLEMENTING
    require_m3_materials
    for path in 02_verification.md 03_review.md 04_decision.md reuse-decision.md; do
      forbid_path "$path"
    done
    ;;
  m4)
    require_status PASS
    require_m4_materials
    forbid_path reuse-decision.md
    ;;
  m5)
    require_status PASS
    require_m4_materials
    require_file reuse-decision.md
    require_heading "$work_item/reuse-decision.md" '## 五项契约检查'
    require_heading "$work_item/reuse-decision.md" '## 决定'
    if ! rg -q '^- 结论：(复用|暂缓|不复用)。$' "$work_item/reuse-decision.md"; then
      echo '[BLOCK] M5 requires one explicit reuse decision; template alternatives are not evidence.' >&2
      exit 1
    fi
    if ! rg -q '^- 理由：[[:space:]]*[^[:space:]].*$' "$work_item/reuse-decision.md"; then
      echo '[BLOCK] M5 requires a factual reason for the reuse decision.' >&2
      exit 1
    fi
    if rg -q '^- 理由：[[:space:]]*(待补充|待指定|TBD|<)' "$work_item/reuse-decision.md"; then
      echo '[BLOCK] M5 reuse reason is still a placeholder.' >&2
      exit 1
    fi
    require_list_value "$work_item/reuse-decision.md" '一次性业务答案隔离'
    if ! awk -F '|' '
      function trim(value) {
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
        return value
      }
      trim($2) != "" && trim($2) != "候选" && trim($2) !~ /^-+$/ && trim($3) != "" &&
        trim($4) != "" && trim($5) != "" { found = 1 }
      END { exit !found }
    ' "$work_item/reuse-decision.md"; then
      echo '[BLOCK] M5 requires a complete reuse candidate row; use 无/N/A when none exists.' >&2
      exit 1
    fi
    if ! awk -F '|' '
      function trim(value) {
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
        return value
      }
      trim($2) != "" && trim($2) != "候选" && trim($2) !~ /^-+$/ && trim($3) != "" &&
        trim($4) != "" && trim($5) != "" && trim($6) != "" &&
        trim($7) != "" && trim($8) != "" { found = 1 }
      END { exit !found }
    ' "$work_item/reuse-decision.md"; then
      echo '[BLOCK] M5 requires a complete five-part contract row; use 无/N/A when none exists.' >&2
      exit 1
    fi
    if rg -q '^- 结论：复用。$' "$work_item/reuse-decision.md" && \
       ! rg -q '^\| M5 \|[^|]+\|[^|]+\|[^|]*(APPROVED|批准)[^|]*\|$' "$readme"; then
      echo '[BLOCK] an M5 reuse decision requires a recorded human approval.' >&2
      exit 1
    fi
    if rg -q '^- 结论：复用。$' "$work_item/reuse-decision.md"; then
      require_list_value "$work_item/reuse-decision.md" '若复用，目标资产、所有者、批准人、使用边界和验证结果'
      if rg -q '^- 若复用，目标资产、所有者、批准人、使用边界和验证结果：[[:space:]]*(N/A|无|暂无)' "$work_item/reuse-decision.md"; then
        echo '[BLOCK] M5 reuse requires a real asset, owner, approver, boundary and verification result.' >&2
      exit 1
      fi
    fi
    if rg -q '^- 结论：暂缓。$' "$work_item/reuse-decision.md"; then
      require_list_value "$work_item/reuse-decision.md" '若暂缓，重新评估条件'
    fi
    if rg -q '^- 结论：不复用。$' "$work_item/reuse-decision.md"; then
      require_list_value "$work_item/reuse-decision.md" '若不复用，保留范围'
    fi
    ;;
esac

echo "[PASS] V2 task-one $mode gate passed for $work_item_id."
