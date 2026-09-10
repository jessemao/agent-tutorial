# Design：统一库存业务修改入口

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T04-G01` |
| 状态 | `APPROVED` |
| Spec 路径/版本 | `spec.md` / v1 / `APPROVED` |
| 代码起点 | `s2-t04-start` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| Standards ID/版本 | `STD-WMS-0.7-06` / 代码起点版本 |
| 所有者 | 仓储事业部；开发负责人由当前用户代理 |

## 问题与设计目标

- 要解决的能力缺口：库存 Module 当前暴露 `InventoryOperations` 与 `InventoryTransferOperations` 两个公开 Seam；后者只有一个纯透传 `LegacyInventoryTransferService` Adapter，使调用方必须记住移库例外，并与“库存变化必须经过 `InventoryOperations`”的模块规则不一致。
- 必须保持的行为：全部 HTTP 路径、请求/响应、错误码和错误优先级；收货、预占、释放、出库、盘点及余额查询；移库双库位稳定锁序、原子两端余额、双流水、一次审计、幂等首次快照/重放/冲突、任务列表内容和排序；现有 UI 操作与刷新恢复。
- 非目标：改变任何业务规则或 UI/HTTP 契约；拆分新的查询 Module；创建通用命令总线、万能 Command、新框架或新依赖；修改 `platform-*`、数据库、事务属性、Repository 或跨 Module 依赖；顺手重构 `InventoryService` 内部算法。

## 现状与约束

- 相关 Module、类/方法和调用方：
  - `InventoryOperations` 是常规库存变化的公开 Interface，生产调用方为 `InventoryController`、`InboundService`、`ShipmentService` 和 `StocktakeService`。
  - `InventoryTransferOperations` 公开 `transfer` 与 `listTransferTasks`，生产调用方只有 `InventoryController`。
  - `LegacyInventoryTransferService` 是唯一 Adapter，两个方法都直接调用 `InventoryService` 同名方法。
  - `T03TransferContractIntegrationTest` 与 `InventoryConcurrencyTest` 直接注入第二 Interface；其余核心移库行为通过 HTTP Seam 测试。
- 现有数据、事务、并发和异常语义：真实 Implementation 已全部位于 `InventoryService`；移库方法自身是事务入口，先校验请求，再按库位 ID 升序锁两个库位，在锁内判定幂等，锁定两端已有余额，原子扣加并写双流水和审计。任务列表是既有流水的只读投影。
- 不可修改的边界：公开 HTTP/UI、业务结果、错误码、数据结构、锁顺序、事务、幂等、流水、审计、平台 Module 和依赖均保持不变。

## 方案

- 责任和依赖方向：直接扩展现有 `InventoryOperations` Interface，加入具名的 `transfer(InventoryTransferCommand)` 和 `listTransferTasks()`。`InventoryService` 继续作为唯一生产 Adapter，并为已有同名 Implementation 增加 Interface 覆盖关系。Controller 和直接模块测试迁移到 `InventoryOperations` 后，删除 `InventoryTransferOperations` 与 `LegacyInventoryTransferService`。依赖方向仍为 Web/业务调用方 → 库存 Interface → 库存 Implementation → Repository/平台协作者。
- Module 深度：保留具名领域动作及专用 Command 类型，让 Interface 暴露调用者必须知道的不变量，而把锁、事务、幂等、余额、流水、审计和投影隐藏在 Implementation 后。删除纯透传 Adapter 通过 deletion test：复杂性不会散回调用方，而只移除无收益的间接层。
- Locality 与 Leverage：所有库存变化及已确认的移库任务查询由一个公开 Seam 提供；Controller 和直接测试只学习一个库存 Interface，Inbound/Shipment/Stocktake 无需修改。规则仍集中在 `InventoryService`，不复制或搬迁 Implementation。
- 依赖分类：领域对象和幂等决策是 in-process；JPA Repository、事务数据库及现有测试数据库是 local-substitutable；平台 `AuditRecorder`/`IdempotencyGuard` 是同进程已注入协作者。不存在 remote-owned 或 true-external 依赖，不新增 Port 或 Adapter。
- 数据与状态变化：无数据模型或业务状态变化；不做数据迁移。源/目标余额、库存流水和任务投影保持现状。
- 事务、幂等、锁顺序与并发：不移动 `InventoryService.transfer` 的 Implementation，不从同类内部绕过 Spring 代理调用；Controller 经统一 Interface 调用同一个 Spring Bean。`@Transactional`、库位 ID 升序锁、余额锁和幂等检查顺序保持不变。
- 异常与失败后最终状态：所有现有 `PlatformException` 代码、HTTP 映射、触发顺序和回滚结果保持；接口迁移不得捕获、包装或转换异常。
- 可观测性与回滚：每个迁移步骤运行同一 T04 baseline。迁移前后记录 Bean 注入、调用方搜索、HTTP 响应、余额、流水、审计和任务列表。无数据库变化，回滚只需按 Contract → Migrate → Expand 的逆序恢复类型和调用方。

### 推荐迁移顺序

1. **Expand**：在 `InventoryOperations` 增加 `transfer` 和 `listTransferTasks`；让 `InventoryService` 显式实现两者，旧 Interface/Adapter 暂时保留。运行相同保护测试。
2. **Migrate**：将 `InventoryController`、`T03TransferContractIntegrationTest`、`InventoryConcurrencyTest` 的第二 Interface 依赖迁到 `InventoryOperations`。其他生产调用方保持不动。运行相同保护测试并核对 Spring Bean 唯一注入。
3. **Contract**：搜索确认 `InventoryTransferOperations` 与 `LegacyInventoryTransferService` 零调用后删除两者，同步 `docs/architecture.md` 的当前事实，再执行目标、Module、前端和全量验证。

## 备选方案与取舍

| 方案 | 优点 | 代价/风险 | 结论 |
| --- | --- | --- | --- |
| A1：直接扩展现有具名 Interface | 最小调用方 Diff；Java 类型明确；动作易发现；真实 Implementation 不移动；最高 Locality/Leverage；容易分步回滚 | Interface 增至 9 个方法，写与读共存 | **推荐**；最符合当前调用图和行为保持目标 |
| A2：封闭泛型 `change(InventoryChange<R>)` + 具名查询 | 顶层 mutation 方法稳定，返回类型比万能结果更安全 | 所有写调用方都要迁移；增加 action/factory/dispatch；事务属性被单入口绑定；导航更间接 | 不采用；当前没有频繁新增动作的路线图证据，复杂度大于收益 |
| A3：`execute(Instruction)` / `query(Query)` 两入口 | 表面方法数最少，概念统一 | 产生 Instruction/Query/Result 字段组合和运行时分支，弱化领域动作与静态类型，接近被禁止的万能 Command | 排除；未形成真实深度，复杂性只是从方法表面转移 |
| A4：另建只读查询 Interface | 命令/查询职责形式上分开 | 只有一个生产 Adapter/调用方，形成 hypothetical Seam；扩大注入和迁移范围 | 排除；人工已同意 `listTransferTasks` 纳入统一 Interface |

## 测试接缝

| 设计决定 | 验证方式 | 失败信号 |
| --- | --- | --- |
| 外部 HTTP/UI 行为不变 | T04 baseline 中的 Happy Path、Contract、Atomicity、Inventory Regression/WMS Flow；前端构建与人工复测 | 路径、字段、状态、错误码、页面结果或列表变化 |
| 统一公开 Seam | 调用方搜索；Controller 构造器和直接模块测试只依赖 `InventoryOperations` | 仍存在第二移库 Interface/Adapter 或生产调用方 |
| Spring 事务调用链不变 | 经 HTTP 和统一 Interface 执行成功、失败及受控流水失败测试 | 部分余额/流水提交、代理未生效或异常映射变化 |
| 幂等与并发不变 | T03 Contract、Atomicity、InventoryConcurrency，必要时课程 MySQL Profile | 重放重复写、冲突改变数据、反向移库死锁或不守恒 |
| 查询归属与列表行为不变 | `GET /api/wms/inventory/transfers` 及列表断言；直接 Interface 查询 | 任务缺失、字段/顺序变化或引入新数据表 |
| 其他库存调用方保持 | Module 回归、`mvn clean verify`、调用方 Diff 检查 | Inbound/Shipment/Stocktake 出现不必要修改或旧行为回归 |

## 待裁决项与批准

| 事项 | 所有者 | 结论 | 时间 |
| --- | --- | --- | --- |
| 选择候选 A | 当前用户代理决策角色 | APPROVED：人工原话“1. 选A” | 2026-09-10（Asia/Shanghai） |
| 将 `listTransferTasks` 纳入统一 Interface | 当前用户代理决策角色 | APPROVED：人工原话“2. 同意纳入” | 2026-09-10（Asia/Shanghai） |
| 采用 A1 具名 Interface 方案及 Expand/Migrate/Contract 顺序 | 当前用户代理开发负责人 | APPROVED；人工原话：“批准” | 2026-09-10（Asia/Shanghai） |
| 保持行为、可改/禁改范围与回滚方式 | 当前用户代理开发负责人 | APPROVED；允许进入 `/to-spec`，尚不授权实施 | 2026-09-10（Asia/Shanghai） |
