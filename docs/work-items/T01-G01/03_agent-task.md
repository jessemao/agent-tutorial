# 03 Agent Task：T01-G01

## 已批准输入

- Spec 路径/版本：N/A；预期行为由 `01_review.md`、`02_impact.md`、用户输入和现有目标测试定义，未改变规则或契约。
- Impact 批准记录：`02_impact.md`；用户以开发负责人角色确认范围并授权进入代码修复。
- Standards ID/版本：`STD-WMS-0.7-03`。
- 代码差异起点：`ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482`。
- 适用 Module `AGENTS.md`：`business-wms/AGENTS.md`、`training-server/AGENTS.md`。

## 目标行为

- RESERVED 出库单取消后，单据为 `CANCELLED`，库存为可用 `10`、预占 `0`。
- 通过既有 `InventoryOperations.release` 产生释放和流水，保持现有事务、幂等和错误契约。

## 目标代码与测试接缝

- Module：`business-wms`；公开验证入口在 `training-server`。
- 目标类/方法：`ShipmentService.cancel`；依赖 `InventoryOperations.release`。
- 首个失败测试：`WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory`。
- 相关调用方：`ShipmentController.cancel` 的 `POST /api/wms/shipments/{id}/cancel`。

## 执行步骤

1. 使用已有 Docker 目标测试确认修复前红灯。
2. 在 RESERVED 分支调用 `inventoryOperations.release(inventoryCommand(order, idempotencyKey))`。
3. 在 Docker 中运行目标测试、Module 回归和 `clean verify`。

## 允许与禁止

### 允许

- 修改 `business-wms/src/main/java/com/acme/training/wms/outbound/ShipmentService.java` 的取消编排。
- 运行 Docker 验证并记录真实结果。
- 在当前 Work Item 目录补齐过程文档。

### 禁止

- 修改测试、配置、公开 API、库存算法、数据库结构、事务边界或平台模块。
- 直接改库存字段、绕过 `InventoryOperations`、吞掉释放异常或扩大到其他出库状态。

## 停止条件

- 需要改变 Spec、公开契约、依赖方向、事务或批准范围。
- 目标测试、Module 回归或全量验证失败，或结果无法对应被审版本。
- UI/QA 复测、Decision 或交付信息缺失时，不得声明最终放行或合并。

## 开发负责人实施批准

APPROVED；开发负责人：用户（开发角色，本次对话）；时间：2026-09-02，本次对话具体时分未提供。
