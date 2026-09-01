# AI Coding 标准产物定义

## 1. 目录

每项任务在 `evidence/<group>/<task>/` 下保存：

```text
01_review.md
02_impact.md
03_agent-task.md
04_verification.md
code-review.md
05_problem-review.md
06_decision.md
07_delivery.md
```

对应模板位于 `docs/ai-governance/templates/`。此外，需求与任务切分分别使用 `spec.md` 和 `ticket.md`。

## 2. 通用元数据

除任务入口外，每份文件都应记录：任务 ID、生成时间、Agent/工具、代码起点、当前代码版本、已批准 Spec 路径/版本、Standards ID/版本和状态。

## 3. 文件职责

| 文件 | 由谁生成 | 必须回答 | 完成条件 |
| --- | --- | --- | --- |
| `01_review.md` | Agent | 输入是什么、现象能否复现、事实/假设/待确认项、调用链或需求冲突是什么 | 人确认问题或需求语义 |
| `02_impact.md` | Agent | 影响哪些 Module、类/方法、契约、数据、事务、调用方和测试；可改/禁改是什么 | 所有者批准范围 |
| `03_agent-task.md` | Agent | 改哪个行为、从哪个测试接缝开始、分几步、何时停止 | 人批准实施任务 |
| `04_verification.md` | Agent | 哪些命令在什么代码版本运行，真实结果是什么，验收条件如何映射 | 测试结果可复现且无伪造 |
| `code-review.md` | Agent | Spec 和 Standards 两轴分别有什么发现 | 每条发现有位置、证据和状态 |
| `05_problem-review.md` | Agent 草拟 | 问题为何发生、哪道门禁应更早发现、如何防止重复 | 责任人确认改进项 |
| `06_decision.md` | 人决策，Agent 记录 | 每条发现接受、退回、例外还是回滚 | 决策人、理由、时间和后续动作完整 |
| `07_delivery.md` | Agent 汇总，人确认 | 最终提交、Diff、Spec、Standards、测试、回滚和剩余风险是什么 | 最终放行人确认 |

## 4. 两张独立评审矩阵

Spec 矩阵列：`Spec 条目 | 代码符号 | 测试 | 真实结果 | 结论 | 证据路径`。

Standards 矩阵列：`规则 ID | 适用性 | 代码符号 | 检查证据 | 例外/人工裁决 | 结论`。

两张矩阵不得合并成一句“Review 已通过”。Review 提供发现，Decision 才记录人的正式裁决。

## 5. 禁止的占位内容

以下内容不算完成：空表格、`待补充`、`后续确认`、无理由的 `N/A`、没有命令输出的“测试通过”、没有批准人的“已批准”、没有代码位置的泛化建议。
