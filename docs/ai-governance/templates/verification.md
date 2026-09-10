# 02 Verification：<任务 ID>

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 |  |
| 被验证分支/提交 |  |
| Spec/Standards |  |
| Docker/Java/Maven/DB 环境 |  |

## 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 |  | PASS / FAIL / BLOCKED |  |  |
| 目标测试 |  | PASS / FAIL / BLOCKED |  |  |
| Module 回归 |  | PASS / FAIL / BLOCKED |  |  |
| 全量验证 | `mvn clean verify` | PASS / FAIL / BLOCKED |  |  |

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01 |  |  |  | PASS / FAIL / BLOCKED |

## 开发交接

- 开发人员 / Ticket / TDD 红绿版本 / 执行时间：
- 开发负责人确认：
- 独立测试入口：`functional-test.md`（测试工程师在对应阶段生成）
- QA 审核入口：`qa-review.md`（QA 在对应阶段生成）
- 业务验收入口：`04_decision.md`（业务人员作出决定后生成）

本文件仅记录开发完成当前任务的 TDD 与必要回归，不证明独立功能测试、QA 或业务验收通过。

## 未覆盖与剩余风险

- 

## 结论

READY_FOR_REVIEW / REWORK / BLOCKED

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| | | 待指定 | PENDING / CONFIRMED / BLOCKED | |

仅记录本阶段实际证据及有权人员的原文结论；缺席或未知负责人必须保留待确认，不得由 Agent 代签，也不得把开发测试、测试工程师结论与 QA 结论混为一项。
