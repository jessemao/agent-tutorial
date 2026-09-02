# 03 Review：T01-G01

## 评审对象

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T01-G01` |
| 评审分支 | `main` |
| Diff 起点 | `03dd2ceda73e5bd9826b1d1a7fce824816071334` |
| 被审版本 | `1635a6ebbff0d631c21db9aeae7101b5bf50a020` |
| 代码差异 | `business-wms/src/main/java/com/acme/training/wms/outbound/ShipmentService.java` 1 个调用恢复；未修改测试文件 |
| Spec 来源 | 无独立 `spec.md`；以 `01_analysis.md` 中开发负责人待批准的根因、目标和范围作为本次评审依据 |
| Standards | `STD-WMS-0.7-03`；架构、测试、AI 安全和文档引导 |

本文件只记录 Agent 评审发现和建议，不代替开发负责人批准、UI/QA 复测或最终 Decision。

## Spec 轴

评审依据为页面失败事实、现有目标测试和 `01_analysis.md` 第 7 节的批准范围。该 Bug 没有独立 `spec.md`，不据此虚构 Spec 批准。

| 检查项 | 证据 | 结果 |
| --- | --- | --- |
| RESERVED 出库单取消后释放库存 | `ShipmentService.cancel` 恢复 `inventoryOperations.release(inventoryCommand(order, idempotencyKey))` | PASS |
| 单据状态保持 `CANCELLED` | 未改变 `order.cancel(...)` | PASS |
| 库存达到 `available=10 / reserved=0` | 目标测试通过；`02_verification.md` 已记录 | PASS |
| 释放使用既有数量、库存维度和幂等键 | 复用现有 `inventoryCommand(order, idempotencyKey)` | PASS |
| 不改变公开 API、UI、其他出库路径和历史数据 | 差异只在一个 RESERVED 分支 | PASS |
| 是否存在范围外行为 | 未发现新增接口、抽象、依赖、配置或测试修改 | PASS |

Spec 轴结论：未发现缺失实现、错误实现或范围外改动。由于无独立 Spec，最终业务规则批准仍需由相应人工角色保留在后续 Decision 中。

## Standards 轴

| Rule | 检查范围 | 证据与发现 | 状态 |
| --- | --- | --- | --- |
| `STD-ARCH-01` | 库存写入口和调用链 | 出库服务继续通过 `InventoryOperations` 释放，不直接访问库存 Repository 或余额 | PASS |
| `STD-OWN-01` | 所有权与差异范围 | 仅改 `business-wms` 出库服务的批准分支，无跨 Module 改动 | PASS |
| `STD-API-01` | API/UI 契约 | 请求、响应、路径、状态码和错误码均未改变 | PASS |
| `STD-DATA-01` | 状态、库存、流水和事务 | 复用现有 `@Transactional` 取消入口及 `InventoryService` 的保存/流水机制，未改变事务边界 | PASS |
| `STD-DATA-02` | 幂等与锁 | 复用现有取消幂等键、库存锁和 RELEASE 操作，未新增并发路径 | PASS |
| `STD-TEST-01` | 缺陷测试 | 修复前目标测试稳定失败，修复后同一测试通过并出现 RELEASE 流水 | PASS |
| `STD-TEST-02` | 目标、Module、全量验证 | Module 与全量 Docker 验证通过，但 `MysqlVerificationEnvironmentTest` 跳过 | BLOCKED |
| `STD-AI-SEC-01` | 输入与证据 | 页面截图已确认脱敏；未引入外部不可信指令或敏感数据 | PASS |
| `STD-DOC-01` | 过程文档与版本 | `01_analysis.md`、`02_verification.md`、本评审均指向当前基线；提交后需重新固定版本 | PASS |
| `STD-DELIVERY-01` | 评审、复测和交付追溯 | UI/QA 复测、人工 Decision 和 Draft PR 尚未闭合；代码与过程文档已提交 | BLOCKED |

## Findings

| ID | 轴/级别 | 位置或证据 | Finding | 建议动作 | 状态 |
| --- | --- | --- | --- | --- | --- |
| F-01 | Standards / 阻塞 | `02_verification.md`；`MysqlVerificationEnvironmentTest` | MySQL 专项测试因环境守卫跳过，当前没有 MySQL 语义证据 | 补跑隔离 MySQL，或由有权角色记录明确的环境例外、风险和补偿措施 | OPEN |
| F-02 | Standards / 阻塞 | `02_verification.md` UI/QA 表格 | 页面复测尚未提供，自动化接口测试不能替代 UI/QA 验收 | UI/QA 使用修复后的服务复测单据状态、可用量、预占量和页面提示，并填写结论 | OPEN |
| F-03 | Delivery / 阻塞 | `02_verification.md`；提交 `1635a6ebbff0d631c21db9aeae7101b5bf50a020` | 代码与过程文档已提交；验证命令在提交前工作树执行，需在交付平台以该提交复核 | 在 MR/PR 中固定该提交并复核验证版本 | OPEN |
| F-04 | Delivery / 阻塞 | Git remote/目标信息 | 当前未确认可用远端和讲师目标分支，无法安全创建关联 Draft MR/PR | 提供或确认远端仓库及目标分支后再推送和创建 Draft PR | OPEN |

未发现需要立即返工的代码级 Finding。上述阻塞项均属于验证、人工验收或交付信息闭合，不应通过修改业务代码绕过。

## 问题成因与防复发动作

### 问题成因

缺陷由取消路径中的副作用调用被注释造成：代码保留了 RESERVED 状态判断和单据取消，却遗漏了既有库存 RELEASE 操作。因此状态、余额、库存流水之间失去一致性；目标测试能够在实施前暴露该问题。

### 防复发动作

1. 在出库状态转换评审中逐项核对“状态变更、库存副作用、流水、审计和事务回滚”是否成对出现。
2. 保持目标集成测试同时断言单据状态、最终库存余额和关键 RELEASE 流水，不只断言 HTTP 成功。
3. 将取消 RESERVED 分支列入代码评审检查清单，禁止仅依据状态字段变化判断取消完成。
4. 对 H2 通过但 MySQL 跳过的情况保留独立风险项，不能把环境跳过写成全环境通过。

## 评审结论

Spec 轴：PASS，未发现代码范围外行为。

Standards 轴：代码差异符合适用规则，但受 MySQL 跳过、UI/QA 复测、提交版本和远端/目标分支信息阻塞。

建议状态：`READY_FOR_DECISION`，不得创建 `04_decision.md`，不得自行批准或合并。
