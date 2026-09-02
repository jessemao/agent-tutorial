# Work Item：T01-G01 取消出库未释放库存

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T01-G01` |
| 任务类型 | Bug 修复 |
| 状态 | `REVIEWING` |
| 业务/规则所有者 | 待提供 |
| 开发负责人（实施前批准人） | 待提供 |
| UI/契约所有者（涉及变更时） | 待提供；当前未确认涉及契约变更 |
| UI 测试/QA 复测验收方 | 待提供 |
| 创建时间 | `2026-09-02 23:10 CST` |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `main` / `03dd2ceda73e5bd9826b1d1a7fce824816071334` |
| 当前代码版本 | `main` / `1635a6ebbff0d631c21db9aeae7101b5bf50a020` |
| Standards ID/版本 | `STD-WMS-0.7-03` |
| 根及 Module `AGENTS.md` | 根 `AGENTS.md`；本轮未修改 Module 代码 |
| 原始输入路径 | `inputs/browser-failure.md`、`inputs/browser-failure.png` |

## 目标与范围摘要

- 目标：修复 RESERVED 出库单取消后库存未释放的问题，并保留可追溯验证证据。
- 允许修改：已批准范围内的 `ShipmentService.cancel` 最小调用修复，以及当前 Work Item 过程文档。
- 禁止修改：测试断言、配置、公开契约、事务边界、其他库存算法和其他业务路径。
- 当前阻塞：UI/QA 复测、MySQL 环境测试裁决、人工 Decision、提交/推送和 Draft PR 仍未完成。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/` | 已创建 |
| Spec | 待分析 | `spec.md` | 本阶段不创建 |
| Tickets | 待分析 | `tickets/` | 本阶段不创建 |
| Design | 待分析 | `design.md` | 本阶段不创建 |
| Interface | 待分析 | `interface.md` | 本阶段不创建 |
| Analysis | 适用 | `01_analysis.md` | 已创建 |
| Verification | 适用 | `02_verification.md` | 已创建 |
| Review | 适用 | `03_review.md` | 当前阶段创建 |
| Decision & Delivery | 适用 | `04_decision.md` | 本阶段不创建 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 根因、实施目标、可改与禁改范围 | 开发负责人 | 待决定 |
| 实现后 | 页面、接口和回归复测 | UI 测试/QA 验收方 | 待决定 |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 | 待决定 |

## 当前下一步

- Agent 可以执行：整理双轴评审、提交当前分支并准备 Draft MR/PR。
- Agent 必须停止：不得创建 `04_decision.md`，不得代替人工批准、UI/QA 复测或合并。
- 需要人工处理：检查两张评审矩阵、未关闭 Findings、代码差异和 `02_verification.md`；补充 UI/QA 复测及最终决定。
