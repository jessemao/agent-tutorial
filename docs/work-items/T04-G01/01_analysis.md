# 01 Analysis：T04-G01

## 元数据

| 字段 | 内容 |
| --- | --- |
| 任务类型 | 业务重构 |
| Agent/工具 | Codex；`improve-codebase-architecture`（按项目治理裁剪为只读候选发现）；只读探索子代理 |
| 代码起点与当前版本 | `s2-t04-start` / `T04-G01` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| Spec/Standards | Spec 尚未生成；`STD-WMS-0.7-06`、根及 `business-wms/AGENTS.md`、`docs/ai-governance/standards/architecture.md` |

## 输入与事实

- 已读取材料：`README.md`、`inputs/input-evidence.md`、T04 学员任务卡、根及 `business-wms/AGENTS.md`、Workflow、角色规范、Skills 编排契约、`STANDARDS.md`、架构/文档/AI 安全标准、`docs/architecture.md`、T03 已批准 Spec/Design/Interface、库存实现、Web 调用方和相关集成测试。
- 已确认事实：
  - `InventoryOperations` 的文档注释声明它是库存变化的唯一公开 Seam，但当前 Interface 只有收货、预占、释放、出库、盘点调整和余额查询，没有移库。
  - `InventoryTransferOperations` 另行公开 `transfer` 与 `listTransferTasks`；唯一 Adapter `LegacyInventoryTransferService` 的两个方法均直接透传给 `InventoryService`，没有转换、规则或独立事务。
  - `InventoryService` 同时包含常规库存修改和完整移库 Implementation；移库并未在第二个 Adapter 内实现。
  - `InventoryController` 是第二个移库 Seam 的唯一生产调用方，同时注入 `InventoryOperations` 和 `InventoryTransferOperations`。
  - 其他生产调用方 `InboundService`、`ShipmentService`、`StocktakeService` 均通过 `InventoryOperations` 改变库存。
  - 直接依赖第二 Seam 的测试只有 `T03TransferContractIntegrationTest` 和 `InventoryConcurrencyTest`；其余移库行为主要通过 HTTP Seam 验证。
  - 当前提交执行 `./scripts/classroom-test.sh T04 baseline` 的结果为 35 tests、0 failures、0 errors，可作为重构前保护网的起点记录；最终设计前仍须确认完整保持行为清单。
  - `business-wms/AGENTS.md` 明确把第二移库 Seam 定义为 T04 受控遗留例外，并要求本任务在行为不变的前提下消除。
- 假设：无。候选强度只依据当前代码、调用方、规则和测试证据，不代表人工选择或批准。
- 未知项：最终 Interface 语义、迁移顺序、可改/禁改范围和具名开发负责人仍待设计与人工确认。候选及移库任务查询归属已由人工决定。
- 失败或澄清证据：`docs/architecture.md` 第 27—45 行把 `InventoryOperations.transfer` 描述为当前架构事实，但 T04 起点源码刻意拆分该能力。该差异可能是教学基线的预置结构，也构成现行架构文档与源码不一致；需由开发负责人确认后在设计阶段处理，不能在本阶段静默修改文档。

## 定位或设计结论

- 调用链/业务流程：
  - 常规写入：Inbound/Shipment/Stocktake/Inventory Controller → `InventoryOperations` → `InventoryService` → 库位锁、余额、幂等、流水和审计。
  - 移库写入：Inventory Controller → `InventoryTransferOperations` → `LegacyInventoryTransferService` → `InventoryService.transfer` → 双库位稳定锁序、两端余额、双流水和审计。
  - 移库查询：Inventory Controller → `InventoryTransferOperations.listTransferTasks` → Legacy Adapter → `InventoryService.listTransferTasks` → Movement Repository。
- 目标 Module、类与方法：当前发现范围仅限 `business-wms` 库存 Module 的公开 Interface、Adapter、Implementation、Controller 调用方及其 `training-server` 测试 Seam；尚未批准目标结构。
- 根因/需求差异/重构或复用设计：当前不存在功能故障。结构摩擦来自同一库存 Module 暴露两个表达库存变化的公开 Seam，而第二 Seam 只有一个生产调用方和一个纯透传 Adapter；调用方必须记住“通常走 `InventoryOperations`，移库例外”。

### 候选 A：消除移库写操作的第二公开 Seam与纯透传 Adapter

- 推荐强度：**Strong**。
- 文件与证据：
  - `InventoryOperations.java:5-25`：声称唯一库存修改 Seam，但缺少移库能力。
  - `InventoryTransferOperations.java:9-14`：第二公开 Interface。
  - `LegacyInventoryTransferService.java:11-28`：唯一 Adapter，两个方法直接透传。
  - `InventoryService.java:83-137`：真实移库和查询 Implementation 已在库存实现内。
  - `InventoryController.java:25-50`：唯一生产调用方被迫学习两个库存 Interface。
- deletion test：删除第二 Interface/Adapter 不会删除或分散移库复杂性；真正的事务、锁、余额、幂等、流水和审计仍集中在 `InventoryService`。仅一个生产调用方和两个直接测试调用方需要迁移，说明该 Adapter 是浅 Module。
- Locality：库存修改规则及其验证入口可集中在同一个公开 Seam，减少规则位置与调用路径的分歧。
- Leverage：Inbound、Shipment、Stocktake、Controller 与测试可学习同一个库存修改 Interface，而无需记忆移库例外。
- 必须保持：`POST /api/wms/inventory/transfer`、`GET /api/wms/inventory/transfers`、JSON 和错误码；双库位稳定锁序；两端原子更新；幂等首次快照、安全重放与冲突；双流水、一次审计、任务列表结果和排序。
- 测试 Seam：现有 T03 Happy Path、Contract、Atomicity、Concurrency 和 T04 baseline；模块级非法输入和反向并发测试需要迁移到最终统一 Interface。
- 主要风险：Spring 代理和事务调用路径必须保持；迁移不能把真实规则搬入 Controller 或新透传层，也不能借机改变外部契约。

### 候选 B：单独裁决写入 Interface 与移库任务查询的归属

- 推荐强度：**Worth exploring**。
- 文件与证据：`InventoryTransferOperations` 同时包含改变库存的 `transfer` 和只读的 `listTransferTasks`，但原始目标只明确“统一库存业务修改入口”。
- deletion test：消除写侧第二 Seam 后，查询能力仍必须由某个 Interface 提供；它的放置是独立范围决定，不能因删除整个类型而机械迁移。
- Locality/Leverage：统一 mutation 能直接提高库存规则的 Locality；任务查询是否放在同一 Interface 对 mutation 的 Leverage 没有直接收益。
- 风险：为一个 Controller 和一个实现新建查询 Interface 会形成“One adapter means a hypothetical seam”；把查询直接并入修改 Interface 又可能扩大该 Interface 表面积。
- 测试 Seam：`GET /api/wms/inventory/transfers` 以及 Happy Path/Atomicity 中的任务列表断言。
- 待人工决定：T04 的统一目标是否包含任务查询；若包含，应在后续 `codebase-design` 比较至少两种 Interface 方案，不在发现阶段预定答案。

### 候选 C：合并直接收货与入库单收货入口

- 推荐强度：**Speculative / 建议排除**。
- 反证：两个 HTTP 场景最终已共同调用 `InventoryOperations.receive`；删除任一路由会删除不同业务能力，而不是集中实现复杂性。库存规则已有 Locality，合并会改变外部契约并越过本任务范围。

### 候选 D：把三种库存 Command 合成万能 Command

- 推荐强度：**Speculative / 建议排除**。
- 反证：普通库存变化、盘点快照调整和双库位移库表达不同不变量。删除专用类型会让非法字段组合和校验复杂性回流调用方，降低 Module 深度与测试可读性，并可能改变错误/幂等语义。

- 候选方向及排除证据：候选 A 有规则、调用方、历史和 deletion test 的直接支持；候选 B 是 A 必须显式裁决的范围问题；C、D 均无法通过 deletion test，且会扩大契约或把复杂性推给调用方。

### 人工候选决定

| 决策项 | 人工原话 | 记录结论 | 时间 |
| --- | --- | --- | --- |
| 重构候选 | `1. 选A` | 选择候选 A：消除移库写操作的第二公开 Seam 与纯透传 Adapter | 2026-09-10（Asia/Shanghai） |
| 移库任务查询归属 | `2. 同意纳入` | 将 `listTransferTasks` 纳入统一的 `InventoryOperations` Interface，后续设计不得另建只有单一 Adapter/调用方的查询 Interface | 2026-09-10（Asia/Shanghai） |

上述决定只批准进入设计阶段，不构成 `APPROVED_FOR_IMPLEMENTATION`，也不授权修改代码或测试。

## 影响与边界

- 受影响 Module、调用方和所有者：潜在影响 `business-wms` 库存 Module、Inventory Controller、Inbound/Shipment/Stocktake 调用方及 `training-server` 公开行为测试；Module 所有者为仓储事业部，开发负责人和具名人员待指定。
- 数据、状态、事务、幂等与并发：不得改变数据表、余额和流水结构、事务边界、幂等判定、首次快照、稳定锁顺序或失败最终状态。
- API/UI/公共契约：默认全部保持；当前没有批准 HTTP/UI 字段、路径、错误码或交互变化。
- 用户旅程、输入控制、结果可见性与刷新后查询：移库输入、成功/失败反馈、已完成任务列表和刷新恢复均必须保持；本任务只调整内部结构。
- 依赖能力归属：移库功能与测试来自代码起点；统一入口属于本次任务；平台、数据库和新框架不属于本次任务。
- 允许修改：当前只允许更新 T04-G01 的分析、设计与 Interface 草案；生产代码和测试尚未批准修改。
- 禁止修改：业务代码、测试、已批准 T03 文档、HTTP/UI 契约、数据库、`platform-*`、Module 依赖、事务/锁/幂等/流水/审计语义。
- 风险与回滚：错误迁移可能导致 Spring Bean 注入歧义、事务代理失效、测试绕过公开 Seam、任务查询丢失或行为漂移。实施前须固定起始提交；每张 Ticket 保持测试全绿，可回退当前 Ticket。

## 实施任务

- 目标行为：将移库写操作及 `listTransferTasks` 纳入统一 `InventoryOperations` Interface，消除第二公开 Seam 和纯透传 Adapter；具体 Interface 与迁移步骤待设计，外部行为必须与起点等价。
- 首个测试或验证接缝：复用 `./scripts/classroom-test.sh T04 baseline`，并在设计阶段逐项映射 HTTP、模块调用、幂等、回滚、并发、流水和审计保护。
- 最小实施步骤：尚未批准。后续设计应比较 Expand、Migrate、Contract 的可回退顺序；本分析不预定具体代码改法。
- 验证命令：起点保护网为 `./scripts/classroom-test.sh T04 baseline`；Module、前端和全量命令待设计映射，但不得弱于 T04 学员任务卡。
- 越界停止条件：候选或方案改变外部行为、公开契约、平台/数据库/依赖、事务/锁/幂等语义；无法明确任务查询归属；需要新公共抽象但没有第二 Adapter 或真实调用方；起点保护测试失败。

## 开发负责人实施批准

| 结论 | 开发负责人 | 时间 | 批准对象与条件 |
| --- | --- | --- | --- |
| 设计已批准，尚未批准实施 | 当前用户代理开发负责人 | 2026-09-10（Asia/Shanghai） | 人工原话：“批准”；确认 A1 Design/Interface、保持行为、Expand/Migrate/Contract 顺序及边界，允许进入 `/to-spec`；Spec 未形成和获批前不授权实施 |
