# Work Item：新增库内移库

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T03-G01 |
| 任务类型 | 新增业务需求 |
| 状态 | ACCEPTED |
| 业务/规则所有者 | 当前用户代理 |
| 开发负责人（实施前批准人） | 当前用户代理 |
| UI/契约所有者（涉及变更时） | 当前用户代理 |
| UI 测试/QA 复测验收方 | 待人工确认 |
| 创建时间 | 2026-09-08 17:12:06 +0800 |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2` |
| 当前代码版本 | `T03-G01` / `0d984e5e4352d3018d3382e0404c91780d3be15b` |
| Standards ID/版本 | `STD-WMS-0.7-04` / 当前代码版本 |
| 根及 Module `AGENTS.md` | `AGENTS.md`；受影响 Module 尚待调查确认 |
| 原始输入路径 | `inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：支持将一个库位上的库存移动到另一个库位。
- 允许修改：待调查并由开发负责人批准。
- 禁止修改：当前建档阶段不得修改业务代码、测试或配置。
- 当前阻塞：无；Ticket 03 已人工验收，允许形成候选提交并进入独立评审。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/` | 已建档 |
| Spec | 适用 | `spec.md` | v1 / APPROVED |
| Tickets | 适用 | `tickets/` | Ticket 01—03 accepted |
| Design | 适用 | `design.md` | APPROVED |
| Interface | 适用 | `interface.md` | APPROVED |
| Analysis | 适用 | `01_analysis.md` | 已完成需求发现 |
| Verification | 适用 | `02_verification.md` | Ticket 01—03 证据已记录 |
| Review | 适用 | `03_review.md` | 已第四次复审；P0 已修复，剩余风险由人工批准接受 |
| Decision & Delivery | 适用 | `04_decision.md` | ACCEPTED |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | Spec v1、Design、Interface、验收条件、实施目标、可改与禁改范围 | 当前用户代理开发负责人 | 已批准；Ticket 拆分于 2026-09-08 人工确认 |
| 实现后 | Ticket 01 页面、接口和回归复测 | 当前用户代理 | ACCEPTED；2026-09-09 11:02:17 +0800 |
| 实现后 | Ticket 02 错误、回滚和幂等契约 | 当前用户代理 | ACCEPTED；2026-09-09 11:27:34 +0800；人工原话：“现在确认Tickets 2，进入 ticket 3” |
| 实现后 | Ticket 03 并发、MySQL、回归和架构文档 | 当前用户代理 | ACCEPTED；2026-09-10 00:02:50 +0800；人工原话：“Ticket 03 验收通过，同意形成候选提交并进入 code-review。” |
| 交付前 | 评审结论、风险和是否放行 | 当前用户代理 | APPROVED；2026-09-10；原文见 `04_decision.md` |

## 当前下一步

- Work Item 已由人工决定 `ACCEPTED`，交付状态按人工指令记录为完成。
- 后续如补齐 AC-10 单边历史流水自动化覆盖，须新建任务并先批准合规测试接缝。
