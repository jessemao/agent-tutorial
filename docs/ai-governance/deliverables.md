# AI Coding 过程产物定义

> 层级：本文件从属于 `standards/documentation.md`，是任务过程产物的执行级定义。  
> 用途：定义每项任务要交付哪些文件、存放在哪里、由谁生成以及何时算完成。  
> 限制：模板完整不代表内容正确；文件必须引用当前任务、当前代码和真实证据，禁止复制历史任务结果。

## 1. 与上下游文档的关系

| 上下游 | 回答的问题 | 本文件如何使用 |
| --- | --- | --- |
| `workflow.md` | 什么阶段产生什么证据 | 依阶段启用本文件中的对应产物 |
| `standards/documentation.md` | 文档必须遵守什么强制规则 | 本文件不得放宽该标准 |
| `templates/` | 单个产物有哪些固定字段 | 按产物名选择对应模板 |
| `docs/work-items/<work-item-id>/` | 当前任务的真实内容和证据是什么 | 将按本文件生成的实例写入该目录 |

本文件不定义项目级文档规范，不改写 Workflow，不复制 Template 字段，也不填写任何特定业务答案。

## 2. 标准输出目录

每项任务在 `docs/work-items/<work-item-id>/` 下保存。该目录是本任务过程文档的唯一写入位置：

```text
README.md
inputs/
spec.md                    # 按任务类型生成
tickets/                   # 需要拆分时生成
design.md                  # 涉及方案/架构/数据/事务时生成
interface.md               # 涉及接口或 UI 契约时生成
01_review.md
02_impact.md
03_agent-task.md
04_verification.md
code-review.md
05_problem-review.md
06_decision.md
07_delivery.md
artifacts/
```

对应模板位于 `docs/ai-governance/templates/`。目录边界、命名、状态、版本和历史保留规则以 [文档检查引导](standards/documentation.md) 为准。

## 3. 创建与裁剪规则

1. 开始任务时先使用 `templates/work-item.md` 生成 `README.md`，列出所有产物的适用性。
2. `spec.md`、`tickets/`、`design.md` 和 `interface.md` 按任务类型生成；不适用时只在任务 `README.md` 记录 `N/A` 理由，不创建空文件。
3. Workflow 指定的必需阶段产物不得通过 `N/A` 跳过；不能生成时必须停止并记录阻塞原因。
4. 每个产物必须使用对应 Template；没有模板时由治理所有者先补模板，Agent 不得自创格式绕过。

### 3.1 按任务类型决定产物

`README.md`、`01_review.md`、`02_impact.md`、`03_agent-task.md`、`04_verification.md`、`code-review.md`、`05_problem-review.md`、`06_decision.md` 和 `07_delivery.md` 是所有任务的固定产物。其他产物按下表裁剪：

| 任务类型 | `spec.md` | `tickets/` | `design.md` | `interface.md` |
| --- | --- | --- | --- | --- |
| Bug 修复 | 预期行为不清、跨会话或改变契约时必需 | 多切片时必需 | 改变责任、事务或数据边界时必需 | 公开契约受影响时必需 |
| 需求调整 | 必需 | 多切片或跨会话时必需 | 实现边界、数据或事务变化时必需 | API、UI 或 Module 契约变化时必需 |
| 新增业务需求 | 必需 | 存在两个以上可独立验证切片时必需 | 必需 | 存在 API、UI、事件或 Module 调用时必需 |
| 业务重构 | 必需，记录必须保持的行为 | 多步迁移时必需 | 必需 | 公开契约需确认保持或迁移时必需 |
| 组件或 Skill 复用 | 必需 | 多调用方接入时必需 | 必需 | 组件契约或 Skill 输入输出变化时必需 |
| 纯文档或配置 | 改变可执行规则或契约时必需 | 通常 `N/A` | 改变架构/运行方式时必需 | 改变接口说明时必需 |

表中“必需”项缺失时阻断实施。条件不成立时可标记 `N/A`，但必须在任务 `README.md` 说明本次事实依据。

## 4. 通用元数据

除任务入口外，每份文件都应记录：任务 ID、生成时间、Agent/工具、代码起点、当前代码版本、已批准 Spec 路径/版本、Standards ID/版本和状态。

## 5. 产物职责与完成条件

| 文件 | 由谁生成 | 必须回答 | 完成条件 |
| --- | --- | --- | --- |
| `README.md` | Agent 草拟，人确认 | 任务 ID、类型、状态、所有者、代码起点、Standards 和产物适用性 | 任务入口唯一且能导航所有产物 |
| `spec.md` | Agent 草拟，业务/技术/UI/QA 批准 | 问题、范围、非目标、规则、决定和验收条件 | 状态、版本和批准可追溯 |
| `tickets/` | Agent 草拟，人批准 | 如何拆成可独立实施、验证和回滚的切片 | 依赖、边界和验证命令明确 |
| `design.md` | Agent 草拟，相关所有者批准 | Module、领域、数据、事务、并发、异常和回滚如何设计 | 关键取舍、备选方案和边界已裁决 |
| `interface.md` | Agent 草拟，UI/技术/调用方批准 | HTTP、UI 或 Module 契约、错误、兼容与迁移是什么 | 请求、响应、错误和兼容行为可验收 |
| `01_review.md` | Agent | 输入是什么、现象能否复现、事实/假设/待确认项、调用链或需求冲突是什么 | 人确认问题或需求语义 |
| `02_impact.md` | Agent | 影响哪些 Module、类/方法、契约、数据、事务、调用方和测试；可改/禁改是什么 | 所有者批准范围 |
| `03_agent-task.md` | Agent | 改哪个行为、从哪个测试接缝开始、分几步、何时停止 | 人批准实施任务 |
| `04_verification.md` | Agent | 哪些命令在什么代码版本运行，真实结果是什么，验收条件如何映射 | 测试结果可复现且无伪造 |
| `code-review.md` | Agent | Spec 和 Standards 两轴分别有什么发现 | 每条发现有位置、证据和状态 |
| `05_problem-review.md` | Agent 草拟 | 问题为何发生、哪道门禁应更早发现、如何防止重复 | 责任人确认改进项 |
| `06_decision.md` | 人决策，Agent 记录 | 每条发现接受、退回、例外还是回滚 | 决策人、理由、时间和后续动作完整 |
| `07_delivery.md` | Agent 汇总，人确认 | 最终提交、Diff、Spec、Standards、测试、回滚和剩余风险是什么 | 最终放行人确认 |

## 6. 两张独立评审矩阵

Spec 矩阵列：`Spec 条目 | 代码符号 | 测试 | 真实结果 | 结论 | 证据路径`。

Standards 矩阵列：`规则 ID | 适用性 | 代码符号 | 检查证据 | 例外/人工裁决 | 结论`。

两张矩阵不得合并成一句“Review 已通过”。Review 提供发现，Decision 才记录人的正式裁决。

## 7. 产物完整性门禁

以下内容不算完成：空表格、`待补充`、`后续确认`、无理由的 `N/A`、没有命令输出的“测试通过”、没有批准人的“已批准”、没有代码位置的泛化建议。

任一必需产物缺失、互相矛盾或无法追溯时，必须停止交付。
