# Work Item：调整入库分批收货规则

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T02-G01 |
| 任务类型 | 需求调整 |
| 状态 | ACCEPTED |
| 业务/规则所有者 | 待人工确认 |
| 开发负责人（实施前批准人） | Captain Mao |
| UI/契约所有者（涉及变更时） | 待人工确认 |
| UI 测试/QA 复测验收方 | 待人工确认 |
| 创建时间 | 2026-09-08 13:28:33 CST |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `s2-t02-start` (`91af1d6fa4f4851129d660624691136d4cb27697`) |
| 当前代码版本 | 分支 `T02-G01`，findings 修复提交 `a1501c3e24e723b63fb67ea0595eda2412d36ca7`；答案标签 `s3-t02-answer` 指向本次收尾提交 |
| Standards ID/版本 | `STD-WMS-0.7-03` |
| 根及 Module `AGENTS.md` | `AGENTS.md`；建档阶段未修改 Module 文件 |
| 原始输入路径 | `docs/training/T02_调整入库分批收货规则_学员任务卡.md`；`inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：调整现有入库收货规则，使分批到货可逐批登记并入库；详细规则待发现和人工确认。
- 允许修改：待分析并由开发负责人批准。
- 禁止修改：建档阶段不修改生产代码、测试、配置、公共契约或其他任务文档。
- 当前阻塞：无实施前阻塞；UI 测试/QA 复测角色需在进入复测前指定。

## 产物适用性

| 产物 | 适用/不适用 | 路径或 N/A 理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/input-evidence.md` | 已建档，待人工验收 |
| Spec | 适用 | `spec.md` | v1，已批准 |
| Tickets | 不适用 | N/A：只有一个通过现有收货 Interface 验证的端到端切片 | 已裁剪 |
| Design | 适用 | `design.md` | 已批准 |
| Interface | 适用 | `interface.md` | 已批准 |
| Analysis | 适用 | `01_analysis.md` | 调查与需求输入确认已完成 |
| Verification | 适用 | `02_verification.md` | 已完成；MySQL 专项通过，独立 UI/QA 仍待提供 |
| Review | 适用 | `03_review.md` | 已完成；8 项 findings 均已处理，Agent 建议 READY_FOR_REVIEW |
| Decision & Delivery | 适用 | `04_decision.md` | 人工决定 ACCEPTED；答案标签已建立；未合并 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | `spec.md` v1、`design.md`、`interface.md`、兼容策略、实施目标、可改与禁改范围 | Captain Mao（开发负责人） | APPROVED；2026-09-08 13:59:58 CST；用户未附加理由或额外条件，按批准文档实施，越界时停止 |
| 实现后 | 页面、接口和回归复测 | UI 测试/QA 验收方 | 未提供独立复测；人工决定已接受功能正确 |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 | ACCEPTED；2026-09-08；原文见 `04_decision.md` |

## 收尾状态

- T02 实现、findings 整改、验证、评审和人工决定均已记录；答案标签为 `s3-t02-answer`。
- 未执行合并；未把缺失的独立 UI/QA 复测或 MR/PR 链接代填为已完成。
- 当前仓库未配置 Git remote，本地提交和标签无法推送；配置远端后需推送分支与 `s3-t02-answer`。
