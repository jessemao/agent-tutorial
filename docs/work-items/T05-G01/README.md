# Work Item：T05 项目级治理 Skill 复用

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T05-G01 |
| 任务类型 | 组件或 Skill 复用 |
| 状态 | ACCEPTED |
| 业务/规则所有者 | 待指定 |
| 开发负责人（实施前批准人） | 用户（课堂代理；具名信息待登记） |
| UI/契约所有者（涉及变更时） | 用户（AI 治理所有者课堂代理；具名信息待登记） |
| 测试工程师 / 团队 | N/A；本任务只生成项目 Skills、未修改业务代码，人工确认裁剪 |
| QA 审核人 / 团队（不同于测试工程师） | N/A；同一人工裁剪决定 |
| 业务验收人 | 待指定 |
| 创建时间 | 2026-09-11 10:57:28 CST |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 任务原始起点 | `add14f73bf857604e5f738fe35111ab4ef1b3e05`（原 `s2-t05-start`） |
| 当前 Review 起点 | `f4948301639c033c8a11db6bca2292c1f8e1a078`（更新后的 `s2-t05-start` / `baseline/t05`） |
| 实现提交 | `ff63847e8ffd663159a7cfd5a7b6359cef747f63` |
| 首次候选 | `689170ff2e4592d07f8fa523100b1892133e6d3e` |
| REWORK 内容提交 | `d76cfb2a81f87834e111bf1987375dab07101d55` |
| 来源绑定提交 | `5ce0e2af151c69db24f0cfa02cf7f323afad16b6` |
| 最终复评候选 | Review 启动时以 `git rev-parse HEAD` 固定，并在 `03_review.md` 记录完整 SHA；README 不自引用其所在提交 SHA |
| Standards ID/版本 | 当前代码版本中的 `STANDARDS.md` 与 `docs/ai-governance/standards/` |
| 根及 Module `AGENTS.md` | 根 `AGENTS.md`；当前建档阶段尚未确定受影响 Module |
| 原始输入路径 | `inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：从 T01～T04 的真实重复 Prompt 中提炼项目级治理 Skills，并验证一个 Skill。
- 允许修改：按五张已批准 Ticket 的顺序，逐个新增对应项目 Skill、必要项目 Skill 配置、锁文件登记和当前任务开发验证证据。
- 禁止修改：第三方 Skill、业务代码、业务测试、T01 手写教学方式、业务 API/UI/数据库及未被当前 Ticket 批准的治理文件。
- 当前结果：最终候选 `5809f83` 已完成双轴复评；全部 Finding 已关闭；人工最终决定已原样记录为 ACCEPTED。当前未配置远端、目标分支或 MR/PR，未执行合并。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/` | 已创建基础输入证据 |
| Spec | 适用 | `spec.md` | v1.1，APPROVED（含 Decision 最小输入与质量阶段裁剪） |
| Tickets | 适用 | `tickets/` | 五张，ready-for-agent |
| Design | 适用 | `design.md` | v1，APPROVED |
| Interface | 适用 | `interface.md` | v1，APPROVED |
| Analysis | 适用 | `01_analysis.md` | 已完成分类、契约和方向确认 |
| Verification | 适用 | `02_verification.md` | 五张 Ticket 与完整链路开发验证完成 |
| Review | 适用 | `03_review.md` | 最终复评完成；0 项 OPEN，READY_FOR_DECISION |
| 独立功能测试 | 不适用 | T05 只生成项目 Skills、未修改业务代码 | N/A（人工确认） |
| QA 审核 | 不适用 | T05 只生成项目 Skills、未修改业务代码 | N/A（人工确认） |
| Decision & Delivery | 适用 | `04_decision.md` | ACCEPTED；正式 MR/PR 与合并待外部交付环境处理 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 复用设计、五个 Skill 契约、调用方、接口边界、可改与禁改范围 | 开发负责人；AI 治理所有者确认治理契约 | 待确认 |
| 独立测试 | 完整需求的功能与风险测试 | 测试工程师 | N/A；用户确认 T05 不执行该业务代码阶段 |
| QA 审核 | 流程、规范、追溯与缺陷关闭 | QA 人员 | N/A；同一人工裁剪决定 |
| 业务验收 | 业务目标与风险 | 业务所有者 | ACCEPTED / 2026-09-12 / 用户原话“ACCEPTED /work-item-decision T05-G01”；企业身份源未配置 |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 | ACCEPTED / 2026-09-12 / 同一人工决定；正式 MR/PR 与合并未执行 |

## 当前下一步

- Agent 可以执行：只读报告最终候选、验证、Review 和 Decision 事实。
- Agent 必须停止：不得自行提交、推送、创建或批准 MR/PR，不得执行合并、发布或打标签。
- 需要人工处理：在正式交付环境配置或确认目标分支和 MR/PR，由有权限的人决定是否合并。

## 待确认事项与负责人

| 事项 ID | 当前阶段 | 待确认内容及对象版本 | 所需角色 | 具名负责人 / 团队 | 状态 | 结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- | --- | --- |
| T05-G01-Q01 | APPROVED_FOR_IMPLEMENTATION | Spec v1、Design v1、Interface v1 的五个命令契约与治理边界 | AI 治理所有者代理 | 用户（课堂代理；具名信息待登记） | CONFIRMED | APPROVED / 2026-09-11 11:35:58 CST |
| T05-G01-Q02 | APPROVED_FOR_IMPLEMENTATION | 五个项目 Skill 的实现顺序、测试接缝、可改/禁改范围及逐项回滚 | 开发负责人代理 | 用户（课堂代理；具名信息待登记） | CONFIRMED | APPROVED / 2026-09-11 11:35:58 CST |
| T05-G01-Q03 | QUALITY_STAGE_APPLICABILITY | T05 是否执行独立业务功能测试与 QA | 业务/AI 治理所有者、交付负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED：N/A | 用户原话：“T05是skills的生成任务，没有改动代码，所以不用测试和QA的环节” |
| T05-G01-Q04 | IMPLEMENTING | T05-G91 完整链路及 `work-item-decision` 最小输入反馈 | 开发负责人、AI 治理所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED | T05-G91 最终决定 ACCEPTED；用户同意将调整提交到 T05 基线并继续收口 |
| T05-G01-Q05 | REVIEWING | 实现候选 `ff63847` 及完整链路开发验证 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话“同意”；授权按已说明步骤形成候选并完成 Review/Decision 收口 |
| T05-G01-Q06 | REVIEWING | SPEC-01～03、STD-01～02 / 候选 `689170f` | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：REWORK | 用户原话：“全部帮我解决掉。” |
| T05-G01-Q07 | REVIEWING | 五项 Finding 修复 / 最终候选 `5809f83` | 开发负责人、AI 治理/契约所有者 | 独立双轴 Review；人工最终决定待给出 | CONFIRMED：RESOLVED | Spec PASS、Standards PASS；0 项 OPEN |
| T05-G01-Q08 | WAITING_FOR_DELIVERY_DECISION | 候选 `5809f83` 的业务/治理验收和最终决定 | 业务/AI 治理所有者、交付负责人 | 当前 Codex 会话用户；企业身份源未配置 | CONFIRMED：ACCEPTED | 用户原话：“ACCEPTED /work-item-decision T05-G01”；记录时间 2026-09-12 16:10:16 CST |
| T05-G01-Q09 | DELIVERY | 正式目标分支、MR/PR 与实际合并 | 有权限的交付负责人 | 待由企业项目管理事实系统解析 | PENDING | 当前仓库无远端；本入口未执行合并 |
