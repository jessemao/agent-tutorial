# Work Item：需求设计完整性治理

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `GOV-REQ-DESIGN-01` |
| 任务类型 | 文档配置 |
| 状态 | `IMPLEMENTING` |
| 业务/规则所有者 | 当前用户代理 AI 治理所有者 |
| 开发负责人（实施前批准人） | 当前用户代理开发负责人 |
| UI/契约所有者（涉及变更时） | 当前用户代理 UI/契约所有者 |
| UI 测试/QA 复测验收方 | N/A：不改变运行页面 |
| 创建时间 | 2026-09-09 |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `435e53b7c3e93f937c3be15bedae4eaeb925355a` |
| 当前代码版本 | `baseline/t03` 工作区 |
| Standards ID/版本 | `STD-WMS-0.7-04` 草案 |
| 根及 Module `AGENTS.md` | 根 `AGENTS.md`；无 Module 生产代码修改 |
| 原始输入路径 | `inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：把 UI 操作闭环前移到需求设计和 Spec 批准阶段。
- 允许修改：AI Governance Standards、Workflow、Templates、Validation 及在途 T03-G01 采用记录。
- 禁止修改：第三方 Skills、业务规则、生产代码和课程手册。
- 当前阻塞：无；人工已同意在需求设计层面补充规则。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/` | 已记录 |
| Spec | 适用 | `spec.md` | 已批准方向 |
| Tickets | 不适用 | 单一文档治理切片 | N/A |
| Design | 不适用 | 不改变运行架构 | N/A |
| Interface | 不适用 | 不改变产品接口 | N/A |
| Analysis | 适用 | `01_analysis.md` | 已完成 |
| Verification | 适用 | `02_verification.md` | 已完成 |
| Review | 适用 | `03_review.md` | 未到阶段 |
| Decision & Delivery | 适用 | `04_decision.md` | 未到阶段 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 将 UI 完整性作为需求设计门禁 | 当前用户代理 | 同意 / 2026-09-09 |
| 实现后 | 治理材料和模板一致性 | AI 治理所有者 | 待复核 |
| 交付前 | 是否采用新 Standards | 交付负责人 | 待决定 |

## 当前下一步

- Agent 可以执行：进入治理文档双轴评审。
- Agent 必须停止：需要修改第三方 Skill、产品业务规则或课程手册时。
- 需要人工处理：后续评审并决定正式放行。
