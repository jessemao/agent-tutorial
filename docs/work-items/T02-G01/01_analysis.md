# 01 Analysis：T02-G01

## 元数据

| 字段 | 内容 |
| --- | --- |
| 任务类型 | 需求调整 |
| Agent/工具 | Codex；仓库只读检查 |
| 代码起点与当前版本 | `s2-t02-start`；`91af1d6fa4f4851129d660624691136d4cb27697` |
| Spec/Standards | Spec 未生成；`STD-WMS-0.7-03` |

## 输入与事实

- 已读取材料：根及 `business-wms`/`training-server` 的 `AGENTS.md`，Workflow、Standards、T02 学员任务卡、`README.md`、`inputs/input-evidence.md`、架构文档、入库/库存源码、前端调用和起点集成测试。
- 已确认事实：旧规则允许计划 10 一次收满；首批 4 返回 `WMS_INBOUND_FULL_RECEIPT_REQUIRED`，单据、累计实收和库存不变。
- 已确认事实：新需求要求逐批入账、收满完成、禁止超收、重试/并发不重复入账，并保留旧的一次收满调用。
- 假设：任务卡第 7 章的全部场景是待业务/UI 所有者正式确认的候选规则，不是已批准 Spec。
- 未知项：入库幂等键范围与历史重放响应；批次持久化授权；审计原子性级别；UI 是固定 4+6 还是输入任意本次量。
- 失败或澄清证据：`inputs/partial-receipt-rejected.png`；`T02InboundBaselineIntegrationTest.partialQuantityIsRejectedWithoutChangingOrderOrInventory()`。

## 定位或设计结论

- 调用链/业务流程：`POST /api/wms/inbounds/{id}/receive` → `InboundController.receive(...)` → `InboundOperations.receive(...)` → `InboundService.receive(...)` → 悲观锁定入库单 → `InboundOrder.receiveAll(...)` → `InventoryOperations.receive(...)` → 同步审计。
- 目标 Module、类与方法：`business-wms` 的 `InboundOrder`、`InboundService`、`InboundOrderRepository`、`InboundStatus`、`InboundView`、入库 Web DTO/Controller；`training-server` 的 T02 公开 API 测试、`useInboundScenario`、`InboundPage`、前端类型与 API 调用。
- 需求差异：`InboundOrder.receiveAll(...)` 强制 `quantity == plannedQuantity`，将 `receivedQuantity` 直接覆盖为本次量并立即转 `RECEIVED`；状态枚举只有 `CREATED/RECEIVED`。
- 不能只删除足量校验：当前赋值不会累加，首批会提前完成，第二批会被状态校验拒绝；且入库单只保存一个 `receiptIdempotencyKey`，无法识别多批或重放较早批次。
- 仓库已有可复用能力：`InboundService` 以 `@Transactional` 包围单据、库存与审计调用；`InboundOrderRepository.findLockedById(...)` 悲观锁可串行同单收货；`InventoryOperations.receive(...)` 是唯一允许的库存写入口；库存流水已以 `(operation_type, idempotency_key)` 唯一约束处理重放和并发冲突。
- 当前 UI/API 事实：路径、`{idempotencyKey, quantity}` 请求和 `InboundView` 字段已能承载“本次量”与“累计量”，剩余量可由计划减累计推导；不存在新增响应字段的已证需求。
- 当前 UI 缺口：状态类型只识别 `CREATED/RECEIVED`，按钮只在 `CREATED` 可用，弹窗只发送固定首批 4，幂等键由数量拼接，两批相同数量会误用同键，错误文案硬编码为单据/库存均为 0。
- 候选方向 A（推荐）：保留现有 HTTP 路径、请求字段、响应封装和已有字段；新增 `PARTIALLY_RECEIVED` 公开状态与持久化的逐批幂等证据，由入库聚合累加并校验剩余量，继续通过 `InventoryOperations`。
- 候选方向 B（不推荐）：仅在入库单保留最后一个幂等键；文件更少，但无法安全重放较早批次，不满足任务卡的已声明验收要求。

## 影响与边界

- 受影响 Module、调用方和所有者：`business-wms` 入库领域（仓储事业部）；`training-server` 课堂 UI/集成测试（课程维护团队）；公开 HTTP/UI 枚举语义需 UI/契约所有者确认。找到的入库收货调用方只有课堂前端和集成测试。
- 数据、状态、事务、幂等与并发：累计量和状态属于入库聚合；同单悲观锁可保护剩余量不变式；任意历史批次重放需要逐批持久化证据；单据、批次、库存余额、库存流水和同步审计失败需共同回滚。
- API/UI/公共契约：推荐保持路径、请求和已有响应字段；`PARTIALLY_RECEIVED` 是新的公开枚举值，必须确认兼容。全局处理器当前将所有 `PlatformException` 映射为 HTTP 409，校验失败为 HTTP 400/`INVALID_REQUEST`。
- 允许修改（建议，待批准）：`business-wms` 入库聚合、服务、批次持久化、状态和对应 Web 边界；`training-server` T02 公开行为测试、并发/回滚测试、课堂 UI 及前端契约类型；相关 API 示例文档。
- 禁止修改（建议）：`platform-contracts`、`platform-web-starter`、跨 Module 依赖方向；不得让 Controller 直访 Repository，不得绕过 `InventoryOperations`，不得把入库业务规则放入 `training-server`。
- 风险与回滚：新增批次持久化属于数据结构变更，未批准前不得实施；同步 audit publisher 失败可触发数据库回滚，但现有平台没有 outbox/after-commit 能力，“外部审计投递与 DB 严格原子”无法在默认边界内保证。回滚时可回退本任务 Diff，但已生成的批次数据迁移方式待设计确认。

## 实施任务

- 目标行为：待所有者确认下方决策后，由 Spec 固化分批状态、剩余量、超收、历史重放、冲突、并发、回滚、审计和旧调用兼容规则。
- 首个测试或验证接缝：`training-server` 的公开 HTTP 集成测试，先表达计划 10 按 4+6 收货的红灯，并保留一次收满 10 的绿灯。
- 最小实施步骤：待 Spec/Design/Interface 确认后由 `/implement` 生成；本阶段不实施。
- Tickets 裁剪：本需求以同一公开收货 Interface 完成一个可独立验证的端到端切片，后端、UI 与迁移不能分别交付为有效业务结果，因此不拆 Tickets。
- 验证命令：`./scripts/classroom-test.sh T02 baseline`（旧规则）；实施后 `./scripts/classroom-verify.sh T02`；Module 回归与全量验证由脚本统一执行。
- 越界停止条件：未确认业务/契约决定；未批准数据结构或公开状态变更；需修改平台模块、公共错误映射、跨 Module 依赖或审计事务模型。

## 待所有者确认的决策树

### 已确认（2026-09-08）

1. 业务规则：业务决策方确认将任务卡第 7 章全部场景作为 T02 Spec 的强制验收规则。
2. 幂等语义：决策为幂等键在 `RECEIVE` 操作类型内全局唯一；安全重放返回首次批次完成时的响应快照。
3. 数据范围：数据决策方允许新增入库批次持久化实体/表，保留任意成功批次的幂等证据与结果快照。
4. 审计语义：审计决策方确认每个成功批次记录一条 INBOUND 审计，安全重放不新增审计；audit publisher 失败时数据库回滚，不要求外部投递与 DB 严格原子。
5. UI/契约：UI/契约决策方确认保持现有路径、请求字段、响应封装和已有字段，只新增 `PARTIALLY_RECEIVED` 状态；课堂 UI 固定执行 4 再 6。

6. 错误契约：UI/契约决策方确认超收返回 `WMS_INBOUND_OVER_RECEIPT`，已完成后再收货返回 `WMS_INBOUND_STATE`，两者均保持 HTTP 409；零数或负数继续由请求校验返回 HTTP 400/`INVALID_REQUEST`。
7. UI 交互：UI 决策方确认 `CREATED` 显示“待收货”并提交固定首批 4；`PARTIALLY_RECEIVED` 显示“部分收货”并提交固定剩余 6；`RECEIVED` 显示“已完成”且禁用收货。请求结束后关闭弹窗；失败页面展示刷新后的真实累计量、待收量和库存，不硬编码为 0。

### 待确认

- 无。决策树已收敛，已由 `/to-spec T02-G01` 固化为 `spec.md` v1、`design.md` 和 `interface.md`；当前等待开发负责人审批。

## 开发负责人实施批准

| 结论 | 开发负责人 | 时间 | 批准对象与条件 |
| --- | --- | --- | --- |
| APPROVED | Captain Mao（开发负责人） | 2026-09-08 13:59:58 CST | 用户未附加理由或额外条件；批准 `spec.md` v1、`design.md`、`interface.md`、兼容策略及本文可改/禁改范围，越界时停止 |
