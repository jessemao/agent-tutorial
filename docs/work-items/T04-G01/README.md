# Work Item：统一库存业务修改入口

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T04-G01` |
| 任务类型 | 业务重构 |
| 状态 | `ACCEPTED` |
| 业务/规则所有者 | 待人工确认 |
| 开发负责人（实施前批准人） | 当前用户代理；已批准 Spec、Design、Interface 与 Tickets 实施范围 |
| UI/契约所有者（涉及变更时） | 当前用户代理；已批准外部契约完全保持 |
| 测试工程师 / 团队 | Codex / 独立测试角色；由用户在 2026-09-10 本轮明确指定 |
| QA 审核人 / 团队（不同于测试工程师） | 当前 Codex 会话完成初审但与测试角色不独立，不能签署 PASS；独立 QA 待指定 |
| 业务验收人 | 待人工确认 |
| 创建时间 | 2026-09-10（Asia/Shanghai） |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `s2-t04-start` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| 当前代码版本 | 业务代码候选 `f49448795a114b32d242442f15c66db2da66c27e`；T04 结束提交由课程标签 `s3-t04-answer` 固定，包含 Review、测试、QA 初审与 Decision 记录 |
| Standards ID/版本 | `STD-WMS-0.7-06`；当前代码起点版本 |
| 根及 Module `AGENTS.md` | `AGENTS.md`；后续涉及 `business-wms` 时必须同时读取 `business-wms/AGENTS.md` |
| 原始输入路径 | `docs/training/T04_统一库存业务入口_学员任务卡.md`；`inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：在保持外部行为不变的前提下，统一库存业务修改入口。
- 允许修改：开发负责人已批准并验收 Ticket 01—03；已完成统一 Interface 扩展、三个调用方迁移、旧 Seam 删除及架构事实同步。
- 禁止修改：不得改变 HTTP 契约、错误码、业务结果、幂等、事务、锁顺序、流水、审计、数据库结构、平台模块或依赖；评审返工仅允许修正文档追溯。
- 当前阻塞：QA 初审记录 4 项 OPEN Findings：测试与 QA 角色不独立、独立测试前缺少草稿 MR/PR、独立测试覆盖及可复核原始证据不完整、MySQL 并发专项未补测或获风险接受。详见 `qa-review.md`。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/input-evidence.md` | 已建档，待人工验收 |
| Spec | 适用 | `spec.md` | v1 `APPROVED` |
| Tickets | 适用 | `tickets/` | Ticket 01—03 accepted |
| Design | 适用 | `design.md` | APPROVED；人工原话“批准” |
| Interface | 适用 | `interface.md` | 开发负责人及 UI/契约所有者已批准外部契约完全保持 |
| Analysis | 适用 | `01_analysis.md` | 候选 A 与查询归属已人工确认 |
| Verification | 适用 | `02_verification.md` | Ticket 01—03 开发证据均已记录并获开发负责人验收 |
| Review | 适用 | `03_review.md` | 候选 `f494487` 双轴评审已落盘；2 个 Standards Findings 已修复并关闭 |
| 独立功能测试 | 适用 | `functional-test.md` | PASS；候选 `f494487`；独立 HTTP/UI 证据已持久化 |
| QA 审核 | 适用 | `qa-review.md` | BLOCKED；4 项 Findings OPEN；需由不同人员/团队复核并签署 |
| Decision & Delivery | 适用 | `04_decision.md` | 人工最终决定 ACCEPTED 已原样记录；交付合并门禁仍 BLOCKED |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 重构候选、行为保持范围、设计、实施目标、可改与禁改范围 | 当前用户代理开发负责人及 UI/契约所有者 | APPROVED；2026-09-10；人工原话：“批准。/to-tickets T04-G01” |
| 独立测试 | 完整需求的功能与风险测试 | Codex / 用户本轮指定的独立测试角色 | PASS；2026-09-10；见 `functional-test.md` |
| QA 审核 | 流程、规范、追溯与缺陷关闭 | 当前 Codex 初审；独立 QA 待指定 | BLOCKED；见 `qa-review.md` |
| 业务验收 | 业务行为保持与风险 | 业务所有者 | 待确认、待执行 |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 | 待确认、待执行 |

## 当前下一步

- Agent 可以执行：等待交付负责人安排关闭 QA-F-01—04，或形成完整正式例外；随后在明确授权下提交、推送并创建草稿 MR/PR。
- Agent 必须停止：不得把业务 ACCEPTED 写成 QA PASS 或实际合并；QA/例外和 MR/PR 门禁满足前不得合并。
- 需要人工处理：指定独立 QA 或正式裁决例外；确认目标分支、MR/PR 与合并负责人。

## 待确认事项与负责人

| 事项 ID | 当前阶段 | 待确认内容及对象版本 | 所需角色 | 具名负责人 / 团队 | 状态 | 结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- | --- | --- |
| T04-Q01 | 建档 | `ae996fe5` 上的建档内容是否准确 | 学员/讲师 | 待指定 | PENDING | 待人工验收 |
| T04-Q02 | 发现 | 选择哪个有代码证据的重构候选及查询归属 | 当前用户代理决策角色 | 当前用户代理 | CONFIRMED | 2026-09-10：选择候选 A；同意纳入 `listTransferTasks`；见 `01_analysis.md` 人工原话 |
| T04-Q03 | 设计 | 必须保持的行为、A1 目标 Interface、迁移顺序与回退方式 | 开发负责人及受影响所有者 | 当前用户代理开发负责人 | CONFIRMED | 2026-09-10：人工原话“批准”；允许进入 `/to-spec`，不授权实施 |
| T04-Q04 | 质量交接 | 独立测试工程师、不同团队 QA 与业务验收人 | 课程讲师/交付负责人 | 测试工程师与 QA 初审均为当前 Codex；独立 QA/业务验收人待指定 | BLOCKED | 功能测试范围内 PASS；QA 初审 4 项 Findings OPEN；见 `qa-review.md` |
| T04-Q05 | 业务验收 | 候选 `f494487` 的业务行为保持与剩余风险 | 业务所有者 | 当前用户代理 | CONFIRMED | 2026-09-10；原话“通过业务验收。现在Accept” |
| T04-Q06 | 最终决定 | 候选 `f494487` 的交付决定 | 当前用户代理 | 当前用户代理 | CONFIRMED | 2026-09-10；原话“通过业务验收。现在Accept”；`04_decision.md` 记录为 ACCEPTED |

负责人未知时由讲师或交付负责人指定，Agent 不得推测或代签。
