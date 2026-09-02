# 01 Review：T01-G01

## 元数据

| 字段 | 内容 |
| --- | --- |
| Agent/工具 | Codex；读取源码并运行 Docker 目标测试 |
| 代码起点 | `ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482`（`group-demo/t01`） |
| 当前代码版本 | `01412b23ec296f8a88e384d46251ec57a0f0fad8`；基线 `ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482` |
| Spec 路径/版本 | N/A；本 Bug 不改变规则或公开契约 |
| Standards ID/版本 | `STD-WMS-0.7-03` |

## 已读取材料

- 根 `AGENTS.md`：已读取 `/Users/captainmao/Desktop/Personal_Project/培训讲解/training-wms/AGENTS.md`。
- Module `AGENTS.md`：已读取 `business-wms/AGENTS.md` 和 `training-server/AGENTS.md`。
- 任务材料：用户任务描述、单据号 `SO-T01-D7AD3B69B7BA`、SKU `362845437756754`、附件截图。
- 架构/API/测试：已读取 `business-wms` 和 `training-server` 的局部规则、取消出库 API、库存操作契约、相关源码和集成测试；未修改业务代码和测试。
- 使用的 Skill：`diagnosing-bugs` 用于建立红灯反馈回路和按候选原因验证；`legacy-docs` 用于遵循文档治理写作边界。

## 事实、假设与待确认项

### 已确认事实

1. Work Item ID 为 `T01-G01`，任务类型为 Bug 修复，当前状态为 `DISCOVERING`。
2. 页面已有单据号为 `SO-T01-D7AD3B69B7BA`，已有 SKU 为 `362845437756754`。
3. 用户提供的页面事实为：取消前总库存 10、预占 6；当前单据状态为 `CANCELLED`，可用量 4、预占量 6；预期可用量 10、预占量 0。
4. 截图显示页面提示“库存校验异常”，并显示“出库单已取消，但占位库存仍为 6，有效库存仅为 4”。
5. 截图显示库存 ID `INV-756754`、库存量 10、原始量 10、占位量 6、有效库存 4，并关联单据 `SO-T01-D7AD3B69B7BA`。
6. 上述库存和页面状态仅作为问题事实记录，不作为根因结论。
7. 当前代码中的 T01 取消路径在 `ShipmentService.cancel` 的 RESERVED 分支中计算 `releaseInventory=true`，但 `inventoryOperations.release(...)` 被注释掉；同一位置保留了“shipment state changes, but reserved inventory is not released”的缺陷说明。
8. `InventoryService.release` 委托 `InventoryBalance.release`，其变换为 `reservedQuantity -= quantity`、`availableQuantity += quantity`，与 `4/6 -> 10/0` 的预期一致。

### 假设

1. 无。未将截图文本或用户提供的预期解释为根因、业务规则批准或修复方案；以下定位结论仅基于当前源码和 Docker 目标测试证据。

### 待人工确认

1. UI 测试/QA 复测人员和复测时间。
2. 课程讲师/交付负责人最终放行结论。

## 复现或澄清证据

- 命令/步骤：
  `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am -Dtest=WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory -Dsurefire.failIfNoSpecifiedTests=false test`
- 真实结果：Docker 中 `WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory` 失败；取消接口返回 `CANCELLED`，随后库存查询返回 `availableQuantity=4`、`reservedQuantity=6`，测试在 `WmsFlowIntegrationTest.java:45` 因期望 `10` 实际 `4` 失败。测试结果为 1 次运行、1 个失败、0 个错误、0 个跳过。
- 日志/截图路径：[`inputs/browser-failure.png`](inputs/browser-failure.png)；文字记录见 [`inputs/browser-failure.md`](inputs/browser-failure.md)。
- 运行环境：Docker 镜像 `training-wms-classroom:0.7`，`linux/amd64`，镜像 ID `sha256:e4aa9722736925f4b03cc446dd27cda37a496c6bfa54c9f2a997d16d32e19c06`；测试使用 H2。

## Agent 定位结果

- 调用链或业务流程：`ShipmentController.cancel` (`POST /api/wms/shipments/{id}/cancel`) → `ShipmentOperations.cancel` → `ShipmentService.cancel` → `locked(shipmentId)` 锁定出库单 → `order.cancel(...)` 将状态置为 `CANCELLED` → RESERVED 分支应调用 `InventoryOperations.release` → `InventoryService.release` → `InventoryBalance.release` → 保存库存并记录 RELEASE 流水 → 记录取消审计。
- 目标 Module、类与方法：主要为 `business-wms` 的 `ShipmentService.cancel`；依赖的稳定契约为 `InventoryOperations.release`。`training-server` 仅提供公开 API 装配和 `WmsFlowIntegrationTest` 验证入口，不承载业务规则。
- 根因或需求冲突：根因已定位为 `ShipmentService.cancel` 在订单状态已为 RESERVED 且执行 `order.cancel(...)` 后，没有调用既有的 `inventoryOperations.release(inventoryCommand(order, idempotencyKey))`，导致单据状态提交为 `CANCELLED`，库存保持 `4/6`，且不会产生 RELEASE 库存流水。当前未发现需要改变公开 API、库存算法或事务边界的需求冲突。
- 候选原因及排除：
  1. **取消状态判断错误：排除。** 目标测试中取消接口返回 200 且单据为 `CANCELLED`；源码中 RESERVED 状态会使 `releaseInventory` 为 true。
  2. **库存释放算法错误：排除为当前主因。** `InventoryBalance.release` 的实现明确执行预占量减、可用量加；问题发生在该方法未被取消路径调用。
  3. **前端展示计算错误：排除为当前主因。** Docker 集成测试直接通过库存公开查询 API 得到 `4/6`，未经过页面计算即可复现相同错误状态。
  4. **取消幂等冲突或事务回滚：排除为当前主因。** 首次取消返回成功且无异常；当前结果是单据状态变更成功而库存未变更，不符合释放失败后整体回滚的表现。
- 修复建议：在既有 RESERVED 分支调用现成的 `InventoryOperations.release`，复用当前出库数量、库存维度和取消幂等键；不新增 API、不修改库存算法、不绕过库存操作契约。

## 请求人工裁决

- 需要确认的结论：由开发批准根因为“取消 RESERVED 出库单时遗漏调用库存释放”，批准目标行为为取消后库存从 `4/6` 恢复为 `10/0`，并批准下述最小修改范围；UI 测试和 QA 负责后续复测验收。
- 建议下一步：由 UI 测试和 QA 复测验收，补齐正式 Decision 与平台交付信息；不得由 Agent 代替最终放行。
- 状态：READY_FOR_REVIEW
