# T01-G01 分析与实施任务设计

## 1. 分析范围与版本

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T01-G01` |
| 任务类型 | Bug 修复 |
| 代码分支 | `main` |
| 被分析版本 | `03dd2ceda73e5bd9826b1d1a7fce824816071334` |
| 失败输入 | `inputs/browser-failure.md`、`inputs/browser-failure.png` |
| Standards | `STD-WMS-0.7-03` |
| 分析阶段状态 | `WAITING_FOR_SCOPE_APPROVAL` |

本阶段只完成事实核对、调用链分析、根因定位、候选原因排除、影响分析和实施任务设计；未修改业务代码、测试或配置。

## 2. 已确认事实

### 2.1 页面失败事实

- 页面已有单据 `SO-T01-8265CA2BF96B`，SKU 为 `219770897155104`。
- 用户提供的前置事实：取消前总库存为 `10`，该单据预占 `6`。
- 页面当前状态为 `CANCELLED`，可用量为 `4`，预占量为 `6`。
- 预期状态为 `CANCELLED`，可用量为 `10`，预占量为 `0`。
- 以上是页面和用户输入的事实，不是根因，也不等同于业务所有者对规则的正式批准。

### 2.2 自动化复现

在当前版本通过 Docker 运行：

```text
docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am -Dtest=WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：失败，`Tests run: 1, Failures: 1, Errors: 0, Skipped: 0`。日志中的最终查询结果为 `availableQuantity=4`、`reservedQuantity=6`；断言在 `training-server/src/test/java/com/acme/training/WmsFlowIntegrationTest.java:45` 期望可用量 `10`，实际为 `4`。

该结果与页面失败事实一致，证明当前代码版本可以稳定暴露同一缺陷。

## 3. 调用链追踪

取消入口和库存写入链路如下：

```text
ShipmentController.cancel
  -> ShipmentOperations.cancel
  -> ShipmentService.cancel
  -> locked(shipmentId)
  -> order.cancel(idempotencyKey, normalizedReason)
  -> RESERVED 状态下应调用 inventoryOperations.release(inventoryCommand(...))
  -> InventoryService.release
  -> InventoryService.change("RELEASE", ...)
  -> InventoryBalance.release(quantity)
  -> 保存 InventoryBalance
  -> 保存 InventoryMovement(RELEASE)
  -> audit(RELEASE, INVENTORY)
```

源码证据：

1. `ShipmentService.cancel` 位于 `business-wms/src/main/java/com/acme/training/wms/outbound/ShipmentService.java`，在锁定出库单后判断 `order.getStatus() == ShipmentStatus.RESERVED`，随后调用 `order.cancel(...)`。
2. RESERVED 分支中的 `inventoryOperations.release(inventoryCommand(order, idempotencyKey))` 当前被注释，实际没有进入库存写入链路。
3. `InventoryService.release` 已通过 `InventoryOperations` 提供 RELEASE 操作，并委托 `InventoryBalance.release`。
4. `InventoryBalance.release` 已实现“预占量减、可用量加”的变更，并校验预占量不能为负。
5. `InventoryService.change` 负责锁定库位、检查按操作类型和幂等键的历史流水、保存余额、记录库存流水和审计。
6. `ShipmentService.cancel` 与 `InventoryService.release` 均处于事务方法中；当前建议不改变事务入口或边界。

## 4. 根因结论

根因是 `ShipmentService.cancel` 的 RESERVED 取消路径只执行了出库单状态变更 `order.cancel(...)`，没有调用已经存在的 `InventoryOperations.release(...)`。因此事务内单据可提交为 `CANCELLED`，库存余额保持预占前的 `available=4 / reserved=6`，也不会生成对应的 RELEASE 库存流水。

这是调用缺失，不是库存释放算法缺失。建议修复目标是恢复该既有调用，使 RESERVED 出库单取消时复用现有库存操作、库存维度、数量和取消幂等键。

## 5. 候选原因排除

| 候选原因 | 排除/保留依据 | 结论 |
| --- | --- | --- |
| `InventoryBalance.release` 加减逻辑错误 | 当前实现明确执行 `reservedQuantity -= quantity`、`availableQuantity += quantity`，并校验预占量 | 排除为主因 |
| 库存查询或页面展示计算错误 | Docker 测试直接调用库存余额接口，未经过页面计算，仍得到 `4 / 6` | 排除为主因 |
| 库存维度或取消数量构造错误 | 现有 `inventoryCommand` 从出库单复用 SKU、仓库、库位和数量；当前链路在调用前即缺失 | 暂无证据支持 |
| 幂等冲突或重复取消 | 首次取消返回成功；失败结果是单据状态改变而库存未变，符合未调用释放，不符合释放冲突后的表现 | 排除为当前主因 |
| 事务回滚或异常吞没 | 当前取消请求无异常但库存未变；代码中可直接观察到 RELEASE 调用被注释 | 排除为当前主因 |
| 前端初始化数据错误 | 自动化测试独立创建 10 件库存、预占 6 件并复现同样结果 | 排除为主因 |

## 6. 影响分析

| 维度 | 结论 |
| --- | --- |
| Module/所有者 | 仅影响 `business-wms` 出库服务与其既有库存操作调用；所有者为仓储事业部 |
| 业务行为 | RESERVED 出库单取消后库存无法释放，后续分配可能低估可用库存 |
| 状态与数据 | 单据状态已变为 `CANCELLED`，库存余额和 RELEASE 流水未同步变化 |
| API/UI 契约 | 建议不改变 HTTP 路径、请求字段、响应字段、状态码、错误码或页面契约 |
| 库存算法 | 建议不修改 `InventoryBalance` 的库存算法 |
| 事务/并发/幂等 | 建议复用现有 `@Transactional` 取消入口、库存锁、幂等键和流水机制，不改变边界或锁顺序 |
| 调用方 | 取消出库公开入口及现有集成测试受益；其他 `reserve`、`ship`、入库、移库和盘点路径不应改变 |
| 数据修复 | 本修复不自动修正历史已残留的 `4 / 6` 数据；历史数据恢复需另行批准和操作 |

## 7. 建议实施任务

### 可改范围

1. 仅修改 `business-wms/src/main/java/com/acme/training/wms/outbound/ShipmentService.java` 的 RESERVED 取消分支。
2. 恢复调用 `inventoryOperations.release(inventoryCommand(order, idempotencyKey))`。
3. 保持现有 `ShipmentService.cancel` 事务、取消幂等判断、库存命令字段来源、审计和异常传播方式。
4. 使用现有 `WmsFlowIntegrationTest.cancellingReservedShipmentReleasesInventory` 作为目标验收；如验证发现测试不足，先停下并请求重新批准测试范围。

### 禁改范围

- 不修改 `platform-*`、`training-server` 业务装配、数据库结构或依赖。
- 不修改公开 HTTP/API/UI 契约、状态码、错误码或字段。
- 不修改 `InventoryOperations`、`InventoryService`、`InventoryBalance` 的算法、锁、幂等、流水或事务实现。
- 不新增抽象、Helper、公共接口、设计模式或跨 Module 依赖。
- 不修改其他出库状态路径、入库、移库、盘点和历史数据。
- 不删除、弱化或改写现有测试断言，不以跳过测试掩盖失败。

## 8. 验证与停止条件

获批后建议按项目顺序执行：

1. 运行目标测试，确认 `CANCELLED` 后为 `available=10 / reserved=0`，并检查 RELEASE 流水。
2. 运行受影响 Module 回归。
3. 运行 Docker `mvn clean verify`。
4. 由 UI 测试/QA 对页面、接口和回归结果进行实现后复测；UI/QA 不作为实施前批准人。

出现以下任一情况立即停止并请求审批：需要改变事务/锁/幂等或公开契约；需要修改 `InventoryService` 以外的库存机制；目标测试不能区分修复前后；测试结果无法对应当前提交；或发现历史数据修复需求。

## 9. 待开发负责人审批

请开发负责人明确批准或退回以下完整范围：

- 根因：RESERVED 取消路径遗漏 `InventoryOperations.release` 调用。
- 修复目标：取消成功时单据为 `CANCELLED`，库存从 `available=4 / reserved=6` 恢复为 `available=10 / reserved=0`，并产生既有 RELEASE 流水。
- 可改范围：仅恢复 `ShipmentService.cancel` RESERVED 分支的既有库存释放调用。
- 禁改范围：本文件第 7 节所列代码、测试、配置、契约、事务、数据和其他业务路径。
- 当前结论：等待开发负责人批准，尚未进入实施。
