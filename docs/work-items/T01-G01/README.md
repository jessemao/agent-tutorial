# Work Item：T01-G01

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T01-G01` |
| 任务类型 | Bug 修复 |
| 状态 | REVIEWING |
| 业务/规则所有者 | 课堂任务卡 |
| 开发负责人（实施前批准人） | 用户（开发角色，已批准实施范围） |
| UI/契约所有者（涉及变更时） | 不适用：本修复不改变 UI/API 契约 |
| UI 测试/QA 复测验收方 | UI 测试/QA（具体人员未提供） |
| 创建时间 | 2026-09-02 17:14:17 +0800 |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482`（`s2-t01-start`） |
| 当前代码版本 | `01412b23ec296f8a88e384d46251ec57a0f0fad8` |
| Standards ID/版本 | `STD-WMS-0.7-03`；当前工作版 |
| 根及 Module `AGENTS.md` | 根 `AGENTS.md`、`business-wms/AGENTS.md`、`training-server/AGENTS.md` 均已读取 |
| 原始输入路径 | `inputs/browser-failure.md`、`inputs/browser-failure.png`、T01 任务材料 |

## 目标与范围摘要

- 目标：取消 RESERVED 出库单时通过既有库存入口释放预占，使库存恢复为可用 10、预占 0。
- 允许修改：`business-wms` 的 `ShipmentService.cancel` 最小编排变更；当前 Work Item 过程文档。
- 禁止修改：测试、配置、公开 API、库存算法、数据库结构、事务边界、平台模块及未批准的其他任务文档。
- 当前阻塞：UI/QA 复测、正式 Decision、讲师目标分支、GitHub remote/PR 目标及合并记录尚未完成。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/`；截图和文字记录是本任务输入 | 已创建 |
| Spec | 不适用 | 预期行为已有用户输入和现有目标测试支持；本修复不改变规则或公开契约 | N/A |
| Tickets | 不适用 | 只有一个可独立验证的修复切片，无拆分依赖 | N/A |
| Design | 不适用 | 不改变 Module、数据、事务或并发边界；沿用既有库存释放入口 | N/A |
| Interface | 不适用 | 不改变 HTTP/UI/Module 公开契约、字段、状态或错误码 | N/A |
| Review | 适用 | `01_review.md` | 已完成 |
| Impact | 适用 | `02_impact.md` | 已完成 |
| Agent Task | 适用 | `03_agent-task.md` | 已完成 |
| Verification | 适用 | `04_verification.md` | Docker 已验证；UI/QA 待复测 |
| Code Review | 适用 | `code-review.md` | 已完成，待人工裁决 |
| Problem Review | 适用 | `05_problem-review.md` | 已完成，待责任人确认 |
| Decision | 适用 | `06_decision.md` | 待人工裁决 |
| Delivery | 适用 | `07_delivery.md` | 待提交、分支/PR 信息及最终放行 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 问题事实、根因、修复目标和范围 | 用户（开发角色） | 已确认；2026-09-02，本次对话具体时分未提供 |
| 实现后 | 页面、接口和回归复测 | UI 测试/QA 验收方 | 待复测 |
| 交付前 | 评审结论、风险和是否放行 | 课程讲师/交付负责人 | 待裁决 |

## 当前下一步

- Agent 可以执行：整理评审、验证和交付证据；等待 UI/QA 复测及交付决策。
- Agent 必须停止：不得代替 UI/QA 验收、交付负责人放行或有权限人员合并。
- 需要人工处理：补充 UI/QA 复测证据、正式 Decision、讲师目标分支、GitHub remote/仓库、PR 和合并记录。
