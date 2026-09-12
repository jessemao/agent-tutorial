# 02 Verification：T05-G01 / 五个项目 Skill

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `add14f73bf857604e5f738fe35111ab4ef1b3e05` |
| 被验证分支/提交 | `T05-G01`；实现候选 `ff63847e8ffd663159a7cfd5a7b6359cef747f63` |
| Spec/Standards | `spec.md` v1.1（APPROVED）；`design.md` v1（APPROVED）；`interface.md` v1（APPROVED）；Tickets 01～05 |
| 执行环境 | macOS；项目脚本；Python 由项目 `uv` 环境执行；本 Ticket 不涉及 Docker/Java/Maven/DB |

## 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `./scripts/validate-t05-skills.sh work-item-start` | PASS | exit 1；缺少 `.agents/skills/work-item-start`，符合预期 Red | `artifacts/ticket-01-automated-validation.txt` |
| Skill 包结构 | `uv run python .../quick_validate.py .agents/skills/work-item-start` | PASS | `Skill is valid!` | `artifacts/ticket-01-automated-validation.txt` |
| 目标门禁 | `./scripts/validate-t05-skills.sh work-item-start` | PASS | `work-item-start: OK` | `artifacts/ticket-01-automated-validation.txt` |
| 治理回归 | `./scripts/validate-ai-governance.sh` | PASS | 项目治理材料与现有项目 Skills 完整 | `artifacts/ticket-01-automated-validation.txt` |
| T01 独立会话深度验证 | T01 Bug 成功建档、目录冲突、缺必要事实 | PASS | 成功路径只生成固定文档和安全附件；冲突时哈希不变；缺事实零写入 | `artifacts/ticket-01-t01-deep-validation.md` |
| T02 独立会话深度验证 | T02 需求调整成功建档、Git 不可得、无效附件、敏感附件 | PASS | 成功路径输入隔离且停在建档验收点；三类失败路径均零写入 | `artifacts/ticket-01-t02-deep-validation.md` |

### Ticket 02：`work-item-discover`

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `./scripts/validate-t05-skills.sh work-item-discover` | PASS | exit 1；缺少 `.agents/skills/work-item-discover`，符合预期 Red | `artifacts/ticket-02-automated-validation.txt` |
| Skill 包结构 | `uv run python .../quick_validate.py .agents/skills/work-item-discover` | PASS | `Skill is valid!` | `artifacts/ticket-02-automated-validation.txt` |
| T01～T04 路由矩阵 | 检查四类任务的调查重点与专业 Skill 路由 | PASS | Bug、需求调整、新增需求、业务重构分别路由；统一输出 Analysis 骨架 | `artifacts/ticket-02-automated-validation.txt` |
| 阶段写入边界 | 检查允许与禁止产物、人工批准停止点 | PASS | 只允许发现阶段文件；明确禁止代码、测试和后续证据 | `artifacts/ticket-02-automated-validation.txt` |
| 目标门禁 | `./scripts/validate-t05-skills.sh work-item-discover` | PASS | `work-item-discover: OK` | `artifacts/ticket-02-automated-validation.txt` |
| 治理回归 | `./scripts/validate-ai-governance.sh` | PASS | 项目治理材料与现有项目 Skills 完整 | `artifacts/ticket-02-automated-validation.txt` |
| `grill-with-docs` 行为验收 | 真正 T04 起点、三轮人工问答及共同理解确认 | PASS | 调查、质询、设计三个步骤保持顺序；确认前持续阻断设计/Spec，确认后只开放 `codebase-design` | `artifacts/ticket-02-grill-with-docs-acceptance.md` |
| 设计与 Spec 顺序修正 | T05-G91 完整链路验收反馈与 Skill 下一入口检查 | PASS | 需设计时固定为 `codebase-design` → 人工确认 → `to-spec`；无需设计时必须记录 N/A 证据后才能直接进入 `to-spec` | `.agents/skills/work-item-discover/SKILL.md` |

### Ticket 03：`work-item-execute`

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `./scripts/validate-t05-skills.sh work-item-execute` | PASS | exit 1；缺少 `.agents/skills/work-item-execute`，符合预期 Red | `artifacts/ticket-03-automated-validation.txt` |
| Skill 包结构 | `uv run python .../quick_validate.py .agents/skills/work-item-execute` | PASS | `Skill is valid!` | `artifacts/ticket-03-automated-validation.txt` |
| 授权前置 | 检查批准原文、对象版本、代码起点、当前 Ticket、可改/禁改范围 | PASS | 任一缺失均停止；多 Ticket 不允许模糊选择 | `artifacts/ticket-03-automated-validation.txt` |
| 当前切片与阶段边界 | 检查允许写入、测试失败及下一入口 | PASS | 只允许当前票实施与开发验证；禁止 Review、独立测试、QA、Decision、Git 交付和自动下一票 | `artifacts/ticket-03-automated-validation.txt` |
| 目标门禁 | `./scripts/validate-t05-skills.sh work-item-execute` | PASS | `work-item-execute: OK` | `artifacts/ticket-03-automated-validation.txt` |
| 治理回归 | `./scripts/validate-ai-governance.sh` | PASS | 项目治理材料与现有项目 Skills 完整 | `artifacts/ticket-03-automated-validation.txt` |

### Ticket 04：`work-item-review`

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `./scripts/validate-t05-skills.sh work-item-review` | PASS | exit 1；缺少 `.agents/skills/work-item-review`，符合预期 Red | `artifacts/ticket-04-automated-validation.txt` |
| Skill 包结构 | `uv run python .../quick_validate.py .agents/skills/work-item-review` | PASS | `Skill is valid!` | `artifacts/ticket-04-automated-validation.txt` |
| 固定 Diff 与证据版本 | 检查起点、候选、祖先关系、三点 Diff 和文档/验证对应关系 | PASS | 无法唯一解析、空 Diff、非祖先或版本漂移均停止 | `artifacts/ticket-04-automated-validation.txt` |
| 双轴落盘与裁决边界 | 检查 Spec/Standards 矩阵、Findings 和禁止动作 | PASS | 必须写入 `03_review.md`；不边审边改、不自动关闭 Finding | `artifacts/ticket-04-automated-validation.txt` |
| 目标门禁 | `./scripts/validate-t05-skills.sh work-item-review` | PASS | `work-item-review: OK` | `artifacts/ticket-04-automated-validation.txt` |
| 治理回归 | `./scripts/validate-ai-governance.sh` | PASS | 项目治理材料与现有项目 Skills 完整 | `artifacts/ticket-04-automated-validation.txt` |

### Ticket 05：`work-item-decision`

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `./scripts/validate-t05-skills.sh work-item-decision` | PASS | exit 1；缺少 `.agents/skills/work-item-decision`，符合预期 Red | `artifacts/ticket-05-automated-validation.txt` |
| Skill 包结构 | `uv run python .../quick_validate.py .agents/skills/work-item-decision` | PASS | `Skill is valid!` | `artifacts/ticket-05-automated-validation.txt` |
| 人工决定保真与事实解析 | 检查最小决定输入、Git/Work Item/项目管理事实解析、Finding 和交付证据 | PASS | 最终决定原文缺失或关键证据冲突时停止；非阻塞元数据不可得时显式标注；PASS 不推导 ACCEPTED | `.agents/skills/work-item-decision/SKILL.md`、T05-G91 Decision 验收 |
| 合并前停止 | 检查 ACCEPTED 下一入口与禁止动作 | PASS | 只提示有权限的人审核合并；禁止合并、强推、发布和答案标签 | `artifacts/ticket-05-automated-validation.txt` |
| 五 Skill 结构门禁 | `./scripts/validate-t05-skills.sh all` | PASS | 五个项目 Skill 均登记且结构有效 | `artifacts/ticket-05-automated-validation.txt` |
| T05 总门禁 | `./scripts/classroom-verify.sh T05` | PASS | `T05 verification passed.` | `artifacts/ticket-05-automated-validation.txt` |
| 完整短命令行为链 | 临时 Work Item `T05-G91` 依次跨五个人工关口 | PASS | 建档、三轮问答、设计/Spec、实施、两轮 REWORK Review、Finding 裁决和最小 Decision 输入均已执行；最终人工决定 ACCEPTED，未自动合并 | `artifacts/ticket-05-full-chain-result.md` |

## 验收映射

| 验收条件 | 实现/配置 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| 固定八个契约章节 | `.agents/skills/work-item-start/SKILL.md` | T05 目标门禁 | 章节齐全 | PASS |
| 显式调用与项目锁定 | `agents/openai.yaml`、`skills-lock.json` | T05 目标门禁、治理门禁 | 隐式调用关闭；项目来源与文件哈希登记 | PASS |
| T01/T02 结构一致、输入隔离 | `$work-item-start` | 两个独立 Agent 会话 | 两者均为模板 README、`inputs/input-evidence.md` 和允许的安全附件；T02 无 T01 特征 | PASS |
| 冲突、缺事实、Git 不可得、无效/敏感附件安全停止 | `$work-item-start` 停止条件 | 两个独立 Agent 会话的五条失败路径 | 均在写入前停止；目录冲突场景原文件哈希不变 | PASS |
| 不复制分析方法、不修改 T01 或第三方 Skill | Skill 内容与当前差异 | 静态检查、范围检查 | 新 Skill 仅复用 `$init-work-item`；未修改第三方 Skill | PASS |
| T01～T04 按类型路由专业能力 | `.agents/skills/work-item-discover/SKILL.md` 路由矩阵 | Ticket 02 目标门禁与路由检查 | 四类调查重点和专业入口明确；未复制专业方法 | PASS |
| 人只回答所有者决定 | `$work-item-discover` 读取顺序、固定产物与禁止动作 | 契约检查 | 仓库事实先由 Agent 调查；人工问题必须标注所有者和对象版本 | PASS |
| 发现阶段不越界 | `$work-item-discover` 允许写入与停止条件 | 阶段边界检查 | 禁止代码、测试、后续证据、批准和 Git 交付操作 | PASS |
| 人工范围收敛不被设计替代 | `$work-item-discover` 范围收敛关口 | T02～T05 路由与下一入口检查 | 四类任务均强制先执行 `$grill-with-docs`；未逐项回答时保持 BLOCKED，不能进入设计或 `/to-spec` | PASS |
| `grill-with-docs` 真实问答 | T04-G84 / `s2-t04-start` | 三轮八题加最终共同理解确认 | 所有人工回答逐项落盘；未由 Agent 代答；最终只解锁 `codebase-design` | PASS |
| 设计与 Spec 的条件路由 | T05-G91 完整链路验收 | `work-item-discover` 下一入口检查 | 必要设计不得被 `/to-spec` 跳过；设计获人工确认后才固化 Spec；无需设计分支保留可核验 N/A | PASS |
| 实施授权完整才可执行 | `.agents/skills/work-item-execute/SKILL.md` | Ticket 03 授权前置检查 | 批准原文、对象版本、代码起点、当前票和范围全部必填 | PASS |
| 每张 Ticket 独立停止 | `$work-item-execute` 下一入口 | 阶段边界检查 | 即使全绿也等待当前票人工验收，不自动下一票或 Review | PASS |
| 开发、测试工程师和 QA 分离 | `$work-item-execute` 固定产物与禁止动作 | 角色边界检查 | `02_verification.md` 只记录开发验证，不代替独立测试或 QA | PASS |
| 固定候选后才能评审 | `.agents/skills/work-item-review/SKILL.md` | Ticket 04 版本契约检查 | 起点、候选、三点 Diff、文档和 Verification 必须一致 | PASS |
| Review 必须落盘 | `$work-item-review` 固定产物 | 双轴落盘检查 | 对话摘要不算完成；必须生成完整 `03_review.md` | PASS |
| Review 与裁决分离 | `$work-item-review` 禁止动作和下一入口 | Finding 边界检查 | 不边审边改、不关闭 Finding、不生成 Decision 或代签测试/QA | PASS |
| 人工决定原意保真 | `.agents/skills/work-item-decision/SKILL.md` | Ticket 05 最小决定输入、事实解析与固定产物检查 | 人只提供最终决定原文；角色、时间、版本及交付元数据从会话、Git、Work Item 或项目管理系统解析，无法取得时显式标注；Agent 不补写结论 | PASS |
| ACCEPTED 不等于自动合并 | `$work-item-decision` 禁止动作和下一入口 | 合并前停止检查 | 只提示有权限的人操作；禁止合并、强推、发布和标签 | PASS |
| 完整五入口结构与总门禁 | 五个项目 Skills、锁文件和治理材料 | `validate-t05-skills.sh all`、`classroom-verify.sh T05` | 全部通过 | PASS |

## 开发交接

- 开发人员 / Ticket / TDD 红绿版本 / 执行时间：Agent；Ticket 01；Red 与 Green 均基于 `add14f73` 后的当前工作树；2026-09-11 12:00:15 CST。
- Ticket 02：Agent；Red 与 Green 均基于 `add14f73` 后的当前工作树；2026-09-11 12:37:58 CST。
- Ticket 03：Agent；Red 与 Green 均基于 `add14f73` 后的当前工作树；2026-09-11 13:39:26 CST。
- Ticket 04：Agent；Red 与 Green 均基于 `add14f73` 后的当前工作树；2026-09-11 13:48:11 CST。
- Ticket 05：Agent；Red 与 Green 均基于 `add14f73` 后的当前工作树；2026-09-11 13:53:36 CST。
- 开发负责人确认：五张 Ticket 均已逐项验收；T05-G91 完整链路最终人工决定 ACCEPTED。
- 独立测试：N/A；用户确认 T05 是 Skill 生成任务且未修改业务代码。
- QA 审核：N/A；同一人工裁剪决定。
- 业务验收入口：`04_decision.md`（业务人员作出决定后生成）。

本文件记录五张 Ticket 的开发验证与完整链路验收；不代替人工最终 Decision。

## 未覆盖与剩余风险

- 当前结果已固定为实现候选 `ff63847e8ffd663159a7cfd5a7b6359cef747f63`；最终 Review 同时包含随后仅用于版本绑定的 Work Item 文档提交。
- T02 验证尝试启动的两个嵌套 CLI 会话遇到采样流中断；最终行为证据由已分配的独立 T02 Agent 会话直接在隔离环境执行，基础设施失败原样保留在对应证据中但不作为 Skill 结果。
- Ticket 02 已完成结构、路由和边界验证；完整五入口链路按 Ticket 05 执行，本 Ticket 不提前实现后续入口。
- 初版将 T04/T05 的 `$grilling` 设为按需，可能让 `codebase-design` 替代人的范围确认；人工验收指出后已改为 T02～T05 强制 `$grill-with-docs` 关口，并重新锁定哈希。
- 完整链路验收暴露出下一入口曾把“范围收敛”直接等同于 `/to-spec`，未明确必要设计必须先完成；现已将条件路由固定在项目 Skill 契约中，避免 Spec 先行固化尚未验证的 Interface 与 Seam。
- 已以 T04 真实起点完成一次交互式验收；临时环境位于 `/tmp/t05-ticket02-grill-t04.eAPGzL/repo`，系统清理临时目录后以持久化 artifact 为准。
- Ticket 03～05 已在 T05-G91 中完成真实行为链验收；Review 经两轮 REWORK 后形成固定候选，Decision 最终记录 ACCEPTED。
- T05-G91 的 SPEC-03 被人工 WAIVED、未修复：零写入证据未包含文件树、内容哈希和 Git index 的完整前后快照。
- `work-item-decision` 最小输入反馈已同步到 Skill、Spec、Interface、Ticket、学员卡及 T05 基线；当前候选仍需双轴 Review 核对一致性。

## 结论

PASS：Ticket 01～05 已按顺序完成开发验证和人工验收；T05-G91 完整短命令链最终决定 ACCEPTED；五 Skill 结构、锁文件、AI Governance 与 T05 总门禁均通过。实现候选为 `ff63847e8ffd663159a7cfd5a7b6359cef747f63`，下一步执行 Spec/Standards 双轴 Review。

## Review REWORK 验证

- 人工授权：用户原话“全部帮我解决掉。”，对应 `03_review.md` 的 SPEC-01～03、STD-01～02。
- 已审候选：`689170ff2e4592d07f8fa523100b1892133e6d3e`；新候选在本轮修复提交后固定。
- SPEC-01：T05-G91 的 README、Analysis、Spec、Design、Interface、Verification、Review、Decision、原始输出和 fixtures 已完整持久化到 `artifacts/full-chain/T05-G91/`，并生成 SHA-256 文件清单；旧 PENDING 日志明确标记为历史准备状态。
- SPEC-02：`work-item-review` 现在先读取质量阶段适用性；适用时依次进入功能测试和 QA，已有权 N/A 证据时直接进入 Decision 前置核对，单项 N/A 不连带裁剪另一阶段。
- SPEC-03：Design、Interface 和 Tickets 01～05 已统一引用 Spec v1.1。
- STD-01：README 与 Verification 明确区分起点 `f494830`、实现提交 `ff63847`、已审候选 `689170f`、REWORK 内容提交 `d76cfb2` 和包含版本绑定的最终复评候选。
- STD-02：五个项目 Skill 锁记录新增 `sourceVersion=T05-G01-v1.1`、`sourceCommit=d76cfb2a81f87834e111bf1987375dab07101d55`、许可证和 `localModifications=false`；逐文件内容哈希已复核。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| Ticket 01 实现及 T01/T02 独立会话证据；基于 `add14f73` 的 Ticket 01 工作树 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 验收通过 / 2026-09-11；用户原文：“没问题。接下俩进入 ticket 02” |
| Ticket 02 `work-item-discover` 实现；基于 `add14f73` 的当前工作树 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 人工确认 / 2026-09-11；用户原话：“人工确认。现在进入Ticket 03” |
| Ticket 03 `work-item-execute` 实现；基于 `add14f73` 的当前工作树 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 人工验收完毕 / 2026-09-11；用户原话：“人工验收完毕，进入Ticket 04” |
| Ticket 04 `work-item-review` 实现；基于 `add14f73` 的当前工作树 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 验收通过 / 2026-09-11；用户原话：“验收通过，进入 Ticket 05” |
| Ticket 05 `work-item-decision` 实现与 T05-G91 完整链路 | 开发负责人、AI 治理所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 完整链路最终决定 ACCEPTED；Decision 最小输入反馈已同步，用户同意继续收口 |
| 独立业务功能测试与 QA | 业务/AI 治理所有者、交付负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED：N/A | 用户确认 T05 只生成项目 Skills、未修改业务代码 |
