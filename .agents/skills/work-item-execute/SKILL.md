---
name: work-item-execute
description: 在 training-wms 中显式执行一个已经批准的 Work Item 或当前 Ticket，先核对批准原文、版本和修改边界，再路由既有 implement/tdd 能力完成当前切片并记录开发验证。仅用于用户明确调用 /work-item-execute；不得隐式触发、自动评审或跨 Ticket。
---

# Work Item Execute

这是项目实施阶段的薄编排入口。它只负责验证实施授权、限制当前切片、调用现有 `$implement` / `$tdd` 并固定开发证据；Red–Green–Refactor 和代码专业规则仍由既有 Skills 与 Standards 所有，不复制到本文件。

## 输入

- 用户必须显式调用 `/work-item-execute <Work Item ID 或 Ticket 路径>`。
- 必须存在当前实施依据：已批准的 `01_analysis.md`，以及任务适用且已批准的 Spec、Design、Interface、Tickets 或明确裁剪记录。
- 必须能找到开发负责人的批准原文、批准对象版本、代码起点、当前 Ticket、允许修改和禁止修改范围。
- 多 Ticket 任务必须指定当前 Ticket 路径；只给 Work Item ID 时，只能在存在唯一可执行切片时继续。

## 读取顺序

1. 根 `AGENTS.md` 和当前 Ticket 涉及的全部 Module `AGENTS.md`。
2. Workflow、Roles and Approvals、Deliverables、Testing Standards 和项目 Skills 契约。
3. 当前 Work Item 的 README、输入、`01_analysis.md`、批准原文和实际 Git 状态。
4. 当前 Ticket；核对 `Blocked by`、状态、允许/禁止范围、验收条件、验证命令和停止条件。
5. 适用且已批准的 Spec、Design、Interface 或裁剪记录，确认对象版本没有漂移。
6. 相关代码、测试、调用方和依赖，确认首个验证接缝仍有效。
7. 调用现有 `$implement`，并在批准的接缝调用 `$tdd`；上游默认 Review、提交或发布行为在本项目中禁用。

## 允许写入

- 仅当前 Ticket 明确批准范围内的实现代码和开发测试。
- 当前 Work Item 的 `02_verification.md` 和当前 Ticket 必需的 `artifacts/`。
- 当前 Work Item README 与当前 Ticket 状态中，只允许同步已发生的实施/验证事实和待确认项；不得静默改变批准依据。

任何新路径、调用方、依赖或契约不在批准范围内时，先停止并请求重新批准，不得以“顺手修复”扩展修改。

## 固定产物

- 当前切片的 Red 证据，或批准的非代码替代验证及其理由。
- 最小 Green 与必要 Refactor 的实际差异，不复制专业 TDD 方法说明。
- 目标测试、受影响 Module 回归、项目全量验证以及所有失败/跳过的解释。
- `02_verification.md` 中的代码起点、被验证版本、命令、环境、原始日志路径、验收映射、未覆盖风险和开发交接。
- 当前 Ticket 实际写入文件清单、检查结果、停止原因、待人工确认事项和唯一下一入口。

开发验证只证明开发人员完成当前 Ticket，不代替独立功能测试、QA、业务验收或代码评审。

## 停止条件

出现任一情况时停止，保留真实证据，不进入 Review 或下一 Ticket：

- 缺少批准原文、批准对象版本、代码起点、当前 Ticket 或明确修改边界。
- 当前 Ticket 被阻塞、不是唯一可执行切片，或前一 Ticket 尚未人工验收。
- Git 当前版本、批准依据或实际 Diff 与记录版本漂移。
- 首个验证接缝不能表达验收条件，Red 来自编译/环境错误而非目标行为，或需要弱化/删除测试。
- 目标、Module 或全量验证失败；必要测试被跳过且没有批准理由；结果无法对应当前版本。
- 修改超出当前 Ticket，或发现 Spec、Interface、依赖、事务、数据库、公共契约、所有权需要变化。
- 需要绕过批准业务入口、写入受限数据或执行未经授权的外部/高风险动作。

## 禁止动作

- 不得复制 `$implement`、`$tdd` 或代码 Standards 的方法内容，也不得修改第三方 Skill。
- 不得扩大范围、改变已批准业务语义、降低测试强度或把失败标成通过。
- 不得生成或更新 `03_review.md`、`functional-test.md`、`qa-review.md` 或 `04_decision.md`。
- 不得把开发 TDD 当作测试工程师的独立测试或 QA 审核。
- 不得自动调用 `/work-item-review`，不得提交、推送、创建 MR/PR、批准或合并。
- 不得自动进入下一 Ticket，即使当前测试全部通过。

## 下一入口

- 当前 Ticket 实施和开发验证完成后：停止，等待开发负责人对当前 Ticket 人工验收。
- 多 Ticket 任务获当前票验收后：由人显式调用下一张 Ticket 的 `/work-item-execute <ticket-path>`。
- 全部切片均获验收且项目要求的完整验证通过后：先由人授权形成固定候选提交，再提示 `/work-item-review <Work Item ID>`；本入口不自动执行。
- 任一停止条件触发：保持当前 Ticket，返回需重新批准或修复的准确对象。

## 验收

- 未批准、批准版本不完整、Ticket 被阻塞或比较版本漂移时必须零实施写入。
- 合法调用只能产生当前 Ticket 批准范围内的代码/开发测试和开发验证证据。
- 构造测试失败、范围变化、Spec/Interface/依赖/事务变化，验证均应停在当前 Ticket。
- 检查当前票完成后没有 Review、独立测试、QA、Decision、Git 提交、MR/PR 或下一 Ticket 写入。
- 执行项目 T05 Skill 门禁、AI Governance 门禁和 `git diff --check`，记录真实版本和结果。
