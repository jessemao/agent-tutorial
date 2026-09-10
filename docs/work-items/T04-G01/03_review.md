# 03 Review：T04-G01

## 被审对象

- 代码差异起点与被审版本：`ae996fe51322f76dd4605de66c6a47cd84ee230b...f49448795a114b32d242442f15c66db2da66c27e`
- 提交列表：`f494487 refactor(t04): unify inventory operations seam`
- `01_analysis.md`：`docs/work-items/T04-G01/01_analysis.md`
- `02_verification.md`：`docs/work-items/T04-G01/02_verification.md`
- Spec/Standards：`spec.md` v1 / APPROVED；`STD-WMS-0.7-06`
- 使用的 Skills 与版本：项目锁定的 `code-review`；Clean Code 专项来源提交 `1b6b3cc1264b8fbe921c65002d05a3bf90ede178`

## Spec 符合性矩阵

| 要求 | 代码符号 | 测试 | 真实结果 | 证据 | 结论 |
| --- | --- | --- | --- | --- | --- |
| AC-01：只保留统一库存公开 Interface | `InventoryOperations`；删除旧 Interface/Adapter | 编译、注入与零引用搜索 | 统一 Interface 含两个移库方法，旧 Java 符号搜索为零 | 固定 Diff、`02_verification.md` | PASS |
| AC-02：其他库存能力保持 | 既有库存调用方与 `InventoryService` | Inventory Regression、WMS Flow、全量回归 | 相关行为测试通过，业务方法体未改 | 固定 Diff、开发验证 | PASS |
| AC-03：正常及全量移库保持 | `InventoryOperations.transfer` | T03 Happy Path | 既有公开行为测试通过 | 固定 Diff、开发验证 | PASS |
| AC-04：失败错误及最终状态保持 | 移库错误路径 | T03 Contract/Atomicity | 既有错误和回滚测试通过 | 固定 Diff、开发验证 | PASS |
| AC-05：幂等语义保持 | 移库幂等路径 | T03 Contract/Atomicity | 重放、冲突与记录断言通过 | 固定 Diff、开发验证 | PASS |
| AC-06：并发不变式保持 | 反向移库路径 | `InventoryConcurrencyTest` | 默认 H2 并发测试通过；MySQL Profile 专项未在本轮执行 | 固定 Diff、开发验证 | PASS |
| AC-07：任务列表保持 | `listTransferTasks`、`GET /transfers` | T03 Contract | 列表行为测试通过，无数据结构 Diff | 固定 Diff、开发验证 | PASS |
| AC-08：分阶段迁移及最终验证 | Ticket 01—03 | T04 baseline、Module、前端、全量、健康检查 | 候选 `f494487` 复验全部通过 | `02_verification.md`、`artifacts/README.md` 及六份持久日志 | PASS |

Spec 轴结论：0 Findings。固定 Diff 完整实现已批准的 Expand → Migrate → Contract 范围；未发现遗漏、错误实现或范围蔓延。

## Standards 符合性矩阵

| 规则 ID | 适用性 | 代码符号 | 检查证据 | 例外 | 结论 |
| --- | --- | --- | --- | --- | --- |
| STD-ARCH-01、STD-ARCH-02 | MUST | `InventoryOperations`、Controller、Service | 单一库存 Seam；依赖方向不变；旧透传 Adapter 删除 | 无 | PASS |
| STD-OWN-01、STD-API-01 | MUST | 固定生产代码 Diff | 修改位于批准范围；无 HTTP/UI、平台或公共错误契约变化 | 无 | PASS |
| STD-DATA-01、STD-DATA-02 | MUST | `InventoryService` | 方法体、事务、锁、幂等、流水和审计逻辑无 Diff | 无 | PASS |
| STD-JAVA-01、STD-SEC-01、STD-SUPPLY-01 | MUST | 生产代码、POM | 无依赖/JDK/POM、安全或供应链变化 | 无 | PASS |
| STD-TEST-01 | MUST | 既有公开行为测试 | 重构使用同一公开行为保护集与结构搜索；未弱化测试 | 无 | PASS |
| STD-TEST-02 | MUST | `02_verification.md`、`artifacts/` | 候选 `f494487` 已复验；六份持久日志有索引、时间和 SHA-256 | 无 | PASS |
| STD-DOC-01 | MUST | Work Item `README.md`、`02_verification.md` | 当前版本、批准范围、Review 状态和下一步已与候选事实对齐 | 无 | PASS |
| STD-DELIVERY-01 | MUST | 本 Review | Spec 与 Standards 分轴完成；Decision 尚未生成 | 无 | PASS |

Clean Code 轴未发现 Mysterious Name、Duplicated Code、Feature Envy、Data Clumps、Primitive Obsession、Repeated Switches、Shotgun Surgery、Divergent Change、Speculative Generality、Message Chains 或 Refused Bequest。删除 `LegacyInventoryTransferService` 正确消除了既有 Middle Man。

## Findings

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| F-STD-01 | MAJOR | `02_verification.md` 的被验证版本、测试证据和结论 | 被审候选为 `f494487`，但初审时文档仍多处写“未提交工作树”“当前实施会话原始输出”，且没有持久日志路径。违反 `STD-TEST-02`、测试标准第 4、7 节及文档标准第 4、5、7 节。 | 已在 `f494487` 上复验；生产/测试目录零 Diff 证明和所有规定命令原始输出已写入 `artifacts/`，索引记录执行窗口及 SHA-256；Verification 已绑定候选。 | RESOLVED |
| F-STD-02 | MINOR | `README.md` 的版本、范围摘要和下一步 | 初审时文档同时记录 Ticket 01—03 已验收，却仍称代码是未提交工作树、允许范围“尚未批准”、候选提交仍待固定。违反 `STD-DOC-01` 和文档标准第 4、5、7 节。 | README 已更新为候选 `f494487`、已批准和验收的范围、已完成 Review 及下一质量阶段。 | RESOLVED |

Standards 轴结论：初审 2 Findings，均按人工 `REWORK` 裁决修复并关闭；生产代码没有 Standards 或 Clean Code Finding。

## 问题复盘

- 直接原因：开发验证先基于未提交工作树完成，随后在评审入口形成候选提交，但 Verification 和 README 没有随版本状态迁移。
- 为什么原有检查没有更早发现：实施门禁验证了测试、范围和格式，没有在创建候选提交后执行“提交 SHA—证据路径—文档状态”一致性检查。
- 哪道门禁可以提前发现：固定评审对象后、启动双轴评审前的文档追溯检查。
- 防止重复的具体动作、负责人和期限：实施 Agent 在评审返工中保存候选版本原始日志并更新版本字段；AI 治理所有者后续评估把候选提交一致性检查加入评审入口。前者须在重新评审前完成，后者负责人尚待课程讲师指定。

## 阶段状态

- 开发证据版本：候选提交 `f49448795a114b32d242442f15c66db2da66c27e`；复验日志与哈希见 `artifacts/README.md`。
- 独立功能测试：待执行；两个代码评审 Findings 已关闭，由待指定测试工程师生成 `functional-test.md`。
- QA 审核：待执行；独立功能测试后由不同团队 QA 生成 `qa-review.md`。

代码评审不代签上述结论；未到对应阶段不等于代码缺陷，缺失必需交付证据仍阻止最终放行。

## Agent 建议

READY_FOR_DECISION

> 本文件不记录人工接受或合并决定；正式裁决写入 `04_decision.md`。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| F-STD-01、F-STD-02 是否按建议返工 | 开发负责人 | 当前用户代理 | CONFIRMED | REWORK；人工原话：“REWORK，帮我解决这两个问题”；两项已按建议修复并关闭 |
| 固定候选的独立功能测试 | 测试工程师 | 待指定 | PENDING | Findings 已关闭，等待指定人员并执行 |
| 固定候选的 QA 审核 | QA 人员 | 待指定 | BLOCKED | 独立功能测试后执行 |

仅记录本阶段实际证据及有权人员的原文结论；缺席或未知负责人必须保留待确认，不得由 Agent 代签，也不得把开发测试、测试工程师结论与 QA 结论混为一项。
