# Work Item：<任务标题>

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID |  |
| 任务类型 | Bug / 需求调整 / 新增需求 / 重构 / 组件或 Skill 复用 / 文档配置 |
| 状态 | DRAFT / DISCOVERING / WAITING_FOR_SCOPE_APPROVAL / APPROVED_FOR_IMPLEMENTATION / IMPLEMENTING / REVIEWING / FUNCTIONAL_TESTING / QA_REVIEWING / WAITING_FOR_DELIVERY_DECISION / ACCEPTED / REWORK / ROLLED_BACK / BLOCKED |
| 业务/规则所有者 |  |
| 开发负责人（实施前批准人） |  |
| UI/契约所有者（涉及变更时） |  |
| 测试工程师 / 团队 | 待指定，不得由 Agent 猜测 |
| QA 审核人 / 团队（不同于测试工程师） | 待指定，不得合并角色 |
| 业务验收人 |  |
| 创建时间 |  |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 |  |
| 当前代码版本 |  |
| Standards ID/版本 |  |
| 根及 Module `AGENTS.md` |  |
| 原始输入路径 |  |

## 目标与范围摘要

- 目标：
- 允许修改：
- 禁止修改：
- 当前阻塞：

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs |  | `inputs/` |  |
| Spec |  | `spec.md` |  |
| Tickets |  | `tickets/` |  |
| Design |  | `design.md` |  |
| Interface |  | `interface.md` |  |
| Analysis | 适用 | `01_analysis.md` |  |
| Verification | 适用 | `02_verification.md` |  |
| Review | 适用 | `03_review.md` |  |
| 独立功能测试 | 适用 | `functional-test.md` |  |
| QA 审核 | 适用 | `qa-review.md` |  |
| Decision & Delivery | 适用 | `04_decision.md` |  |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 根因/设计、实施目标、可改与禁改范围 | 开发负责人 |  |
| 独立测试 | 完整需求的功能与风险测试 | 测试工程师 |  |
| QA 审核 | 流程、规范、追溯与缺陷关闭 | QA 人员 |  |
| 业务验收 | 业务目标与风险 | 业务所有者 |  |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 |  |

## 当前下一步

- Agent 可以执行：
- Agent 必须停止：
- 需要人工处理：

## 待确认事项与负责人

| 事项 ID | 当前阶段 | 待确认内容及对象版本 | 所需角色 | 具名负责人 / 团队 | 状态 | 结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- | --- | --- |
| | | | | 待指定 | PENDING / CONFIRMED / BLOCKED | |

负责人未知时由讲师或交付负责人指定，保留待确认项，不得将 Agent、开发负责人或泛称“QA/测试”代填。进入独立测试前必须指定测试工程师；进入 QA 审核前必须指定另一团队的 QA。未知结果不能预填 PASS。
