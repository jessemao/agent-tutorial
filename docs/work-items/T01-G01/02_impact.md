# 02 Impact：T01-G01

## 影响范围

| Module | 所有者 | 类/方法或契约 | 影响 | 可改/禁改 |
| --- | --- | --- | --- | --- |
| `business-wms` | 仓储事业部；实施前批准角色为开发 | `ShipmentService.cancel`；`InventoryOperations.release` | RESERVED 出库单取消后，单据状态变为 `CANCELLED`，但库存未从 `4/6` 恢复为 `10/0`，并缺少 RELEASE 流水 | 建议仅改 `ShipmentService.cancel` 的既有释放编排；禁改 `InventoryService`、`InventoryBalance` 算法、公开契约、事务边界和平台模块 |
| `training-server` | 课程维护团队；UI 测试和 QA 为复测验收方 | `ShipmentController` 公开 API；`WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory` | 提供复现/验收入口；目标测试当前在库存断言处失败 | 本阶段禁改 Controller、配置和测试；后续仅在获批实施阶段按批准验证方案处理测试 |

## 依赖与调用方

- 上游调用方：`ShipmentController.cancel` 通过 `ShipmentOperations.cancel` 调用出库取消服务；课堂页面只调用公开 API。
- 下游依赖：`ShipmentService.cancel` 依赖 `InventoryOperations.release`；实现由 `InventoryService` 通过 `InventoryBalance.release` 更新库存并记录 `InventoryMovement`。
- 依赖方向是否变化：否；继续沿用业务模块内部既有 `ShipmentService -> InventoryOperations` 方向。
- 新增公开接口/Module/依赖：无。

## 数据、状态与事务

- 数据表/实体：`ShipmentOrder` 对应 `wms_shipment_order`；`InventoryBalance` 对应 `wms_inventory_balance`；库存流水为 `InventoryMovement` 对应 `wms_inventory_movement`。页面输入中的 `INV-756754` 为截图事实，不作为数据库主键映射结论。
- 状态变化：当前路径将 `ShipmentOrder.status` 置为 `CANCELLED`；正确的最小目标是对原 RESERVED 数量执行一次 RELEASE，使可用量 `4 -> 10`、预占量 `6 -> 0`。
- 事务归属：`ShipmentService.cancel` 和 `InventoryService.release` 均为 Spring `@Transactional` 业务入口；建议保持现有事务边界，释放失败时不得留下仅单据取消的部分成功。
- 幂等/并发/回滚影响：释放应复用取消请求的幂等键；现有库存操作按 `RELEASE` 操作类型和幂等键记录流水，重复相同请求应安全重放，冲突请求应继续由既有幂等契约处理。当前缺陷是未产生 RELEASE 调用/流水；并发锁顺序和事务语义不建议改变。

## API 与 UI 契约

- 请求/响应字段：取消请求继续使用现有 `idempotencyKey`、`reason`；库存查询继续返回现有 `availableQuantity`、`reservedQuantity`。
- 状态/错误码：取消成功仍为 `CANCELLED`；库存释放失败应沿用既有库存错误契约并触发事务回滚，具体验收矩阵待批准。
- 兼容性：不建议改变 HTTP 路径、请求/响应结构、错误码、公开接口或前端判断。
- UI 截图/原型：[`inputs/browser-failure.png`](inputs/browser-failure.png)；截图文字事实见 [`inputs/browser-failure.md`](inputs/browser-failure.md)。

## 测试影响

- 首个失败或验收测试：Docker 目标测试 `WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory`；修复前实际 `4/6`，修复后验证目标为 `10/0`。
- Module 回归：Docker 已执行并通过，详见 `04_verification.md`。
- 全量验证：Docker `clean verify` 已执行并通过，详见 `04_verification.md`。

## 修改边界

### 允许修改

- 实施仅修改 `business-wms` 的 `ShipmentService.cancel`，过程产物仅位于当前 Work Item 目录。
- 获得根因、修复目标和范围批准后，建议实施范围仅为 `business-wms`/`ShipmentService.cancel` 中调用既有 `InventoryOperations.release` 的最小编排变更，以及经批准的回归验证证据。

### 禁止修改

- 不修改测试、配置、数据库结构及其他任务文档；最终平台交付前不得扩大范围。
- 建议禁改 `InventoryService`、`InventoryBalance.release`、`InventoryOperations` 公开契约、HTTP API、跨 Module 依赖、事务边界、幂等模型和平台模块。
- 未经批准不得扩大到 UI、库存算法、数据库迁移、并发锁策略或其他出库状态。

## 开发负责人实施批准

| 结论 | 开发负责人 | 时间 | 备注 |
| --- | --- | --- | --- |
| APPROVED | 用户（开发角色） | 2026-09-02；具体时分未提供 | 已批准根因、修复目标和最小修改范围；UI 测试和 QA 负责复测验收 |
