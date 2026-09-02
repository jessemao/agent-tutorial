# Work Item：T01 浏览器复现首页

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `TRAINING-T01-HOMEPAGE` |
| 任务类型 | 新增需求 |
| 状态 | `WAITING_FOR_DELIVERY_DECISION` |
| 业务/技术/UI/QA 所有者 | 课程讲师代理课堂角色 |
| 创建时间 | 2026-09-02 |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `f5d4640` |
| 当前代码版本 | 主分支 `b6132c0`；课堂运行分支 `group-demo/t01` 为 `2410d0e` |
| Standards ID/版本 | `STD-WMS-0.7-03` |
| 根及 Module `AGENTS.md` | `AGENTS.md`、`training-server/AGENTS.md` |
| 原始输入路径 | 2026-09-02 当前对话：使用真实 WMS 页面，不使用自创培训控制台；不实现鉴权复杂逻辑 |

## 目标与范围摘要

- 目标：通过真实 WMS 出库订单与库存明细页面，在浏览器中复现并验收 T01。
- 允许修改：`training-server` 静态资源和首页入口集成测试。
- 禁止修改：业务规则、既有 API、平台契约、数据库、T01 故障和修复。
- 当前阻塞：无。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | 当前对话 | 已确认 |
| Spec | 适用 | `spec-v3.md`；`spec.md` 保留 v2 历史 | v3 已批准 |
| Tickets | 不适用 | 只有一个可独立验证的静态首页切片 | N/A |
| Design | 适用 | `design-v3.md`；`design.md` 保留 v2 历史 | v3 已批准 |
| Interface | 适用 | `interface.md` | 已批准 |
| Review | 适用 | `01_review.md` | 已完成 |
| Impact | 适用 | `02_impact.md` | 已批准 |
| Agent Task | 适用 | `03_agent-task.md` | 已批准 |
| Verification | 适用 | `04_verification.md` | v3 已完成 |
| Code Review | 适用 | `code-review.md` | v3 已完成 |
| Problem Review | 适用 | `05_problem-review.md` | v3 已完成 |
| Decision | 适用 | `06_decision.md` | 待 v3 人工决定 |
| Delivery | 适用 | `07_delivery.md` | 已准备，待人工放行 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 实现首页，不限于单元测试，不改业务代码 | 用户，课程讲师/交付负责人 | `APPROVED`，2026-09-02 |
| UI 返工 | 采用 `open-wms` 真实出库订单/库存明细页面结构；不做鉴权 | 用户，UI/契约所有者及课程讲师代理 | `APPROVED`，2026-09-02 |
| 框架重构 | 使用 South Admin React；继续不做鉴权 | 用户，UI/契约所有者及课程讲师代理 | `APPROVED`，2026-09-02 |
| 交付前 | Spec/Standards 评审和真实浏览器结果 | 用户 | 待决策 |

## 当前下一步

- Agent 已完成：South Admin React 重构、Docker 构建、依赖审计、自动测试和真实浏览器验证。
- Agent 必须停止：不得代替课程讲师填写最终放行结论。
- 需要人工处理：在 `06_decision.md` 选择 `ACCEPT` 或 `REWORK`。
