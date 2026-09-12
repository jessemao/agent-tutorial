---
name: work-item-review
description: 在 training-wms 中显式评审一个具有固定起点和候选提交的 Work Item，调用既有 code-review 分别执行 Spec 与 Standards 两轴审查，并将完整结果写入 03_review.md 后停在人工 Finding 裁决点。仅用于用户明确调用 /work-item-review；不得隐式触发、边审边改或自行裁决。
---

# Work Item Review

这是项目评审阶段的薄编排入口。它固定被审对象、补足本项目的落盘和权限约束，并调用现有 `$code-review`；双轴专业评审方法仍由该 Skill 所有，不复制或修改第三方内容。

## 输入

- 用户必须显式调用 `/work-item-review <Work Item ID>`。
- Work Item 必须唯一记录差异起点和候选提交；候选必须是提交而不是未固定工作树。
- 必须存在并能对应候选版本的 `01_analysis.md`、适用且已批准的 Spec/Design/Interface/Tickets，以及 `02_verification.md`。
- 必须能确定适用 Standards、根和相关 Module `AGENTS.md`、当前 Skill 版本及开发负责人对全部切片的验收记录。

## 读取顺序

1. 根 `AGENTS.md`、Workflow、Roles and Approvals、Review Template 和项目 Skills 契约。
2. 当前 Work Item README、输入、Analysis、批准记录、Spec/Design/Interface/Tickets 和 Verification。
3. 用 Git 唯一解析起点与候选 SHA，确认候选基于声明起点；固定三点 Diff、提交列表和文件列表。
4. 读取 Diff 涉及的全部 Module `AGENTS.md` 和适用 Standards。
5. 核对 Verification 的被验版本、命令和原始证据确实对应候选提交。
6. 调用现有 `$code-review`，让 Spec 与 Standards 两轴保持独立；项目约束要求结果必须落盘。

若用户没有另给比较点，先从 Work Item 解析；只有无法唯一解析时才向用户询问，不能猜测 `main`、最近提交或当前工作树。

## 允许写入

- 当前 Work Item 的 `03_review.md`。
- 当前评审必要的只读证据或 `artifacts/` 日志。
- 当前 Work Item README 中与评审阶段一致的真实状态、被审版本和待裁决项。

评审开始后不得修改被审代码、测试、批准依据、Spec、Design、Interface、Tickets 或 `02_verification.md` 来让结果通过。

## 固定产物

`03_review.md` 必须完整包含：

- 唯一起点 SHA、候选 SHA、三点 Diff 命令、提交列表、文件范围和被审 Skill/Standards 版本。
- Analysis、Spec、Design、Interface、Tickets、Verification 与候选版本的对应关系。
- 独立的 Spec 符合性矩阵：遗漏/部分实现、范围蔓延和实现错误。
- 独立的 Standards 符合性矩阵：规则来源、证据、硬性违反与判断性代码气味。
- 每个 Finding 的轴、级别、文件/位置、证据、影响和建议；不得跨轴重新排序掩盖问题。
- 问题成因、防复发动作、未覆盖风险和逐条待人工裁决项。

对话中的摘要不是交付物；没有成功写入 `03_review.md` 时，本入口必须报告未完成。

## 停止条件

出现任一情况时，在写入虚假评审结论前停止：

- 起点或候选不能唯一解析、Diff 为空、候选不基于起点，或候选不是固定提交。
- Work Item 文档声明版本、实际 Diff、Verification 被验版本或证据互相漂移。
- 缺少适用且已批准的 Analysis、Spec/Design/Interface/Tickets、Verification 或 Standards。
- 任一 Spec/Standards 评审轴无法执行，或子评审失败、缺失、互相污染。
- 评审要求修改代码、测试、批准依据或第三方 `$code-review` 才能继续。
- 当前候选已被后续提交替代，或评审期间被审引用发生变化。

停止时报告稳定原因、冲突版本和需要哪个负责人修正，不得退化成只在对话中给出非固定 Review。

## 禁止动作

- 不得边审边改，不得修复 Findings 后继续沿用旧候选或旧评审。
- 不得把测试全绿当作 Spec/Standards 双轴通过，也不得合并或重排两个轴的 Findings。
- 不得自行关闭 Finding、给出人工例外、接受风险或推导 `APPROVED` / `ACCEPTED`。
- 不得生成或更新 `functional-test.md`、`qa-review.md` 或 `04_decision.md`，不得代签测试工程师、QA、业务或交付负责人。
- 不得提交、推送、创建或操作 MR/PR，不得批准、合并、发布或打标签。
- 不得修改第三方 Skill、业务代码、测试、治理规范、Templates 或门禁脚本。

## 下一入口

- 存在 Finding：停在逐条人工裁决；`REWORK` 由人显式返回对应 Ticket 的 `/work-item-execute`，修复后形成新候选并重新 Review。
- 没有未关闭阻塞项且 Review 获人工确认：先读取当前 Work Item 对独立功能测试和 QA 的适用性决定。适用时交测试工程师生成 `functional-test.md`，随后由另一团队 QA 生成 `qa-review.md`；若两阶段均已有权角色明确批准为 N/A，则直接进入业务/治理验收与 Decision 前置核对，并保留 N/A 原文和理由。
- 只有一个质量阶段为 N/A 时，只跳过该阶段，不得顺带裁剪另一个阶段；缺少适用性决定时按 Workflow 默认执行独立功能测试和 QA。
- 独立测试、QA 和业务验收均不能由本入口启动、代签或推导。
- Decision 仅在所需人工结论和交付证据完整后，由人显式调用 `/work-item-decision <Work Item ID>`。

## 验收

- 有效固定 Diff 能生成完整、落盘的 `03_review.md`，Spec 与 Standards 两轴独立且 Findings 可定位。
- 无效引用、非祖先候选、空 Diff、候选/Verification 漂移和缺失评审轴必须稳定停止。
- 对比评审前后候选树，证明评审没有修改被审代码或测试。
- 检查没有 Decision、独立测试、QA、Finding 自动关闭、Git 交付或 MR/PR 操作。
- 执行项目 T05 Skill 门禁、AI Governance 门禁和 `git diff --check`，记录真实版本和结果。
