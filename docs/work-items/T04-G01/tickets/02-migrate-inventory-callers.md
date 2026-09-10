# 02：迁移库存调用方到统一 Interface

**状态：** accepted  
**Blocked by：** None（Ticket 01 已于 2026-09-10 由开发负责人验收）  
**Spec：** `docs/work-items/T04-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06` / 代码起点版本

## 交付行为

Inventory Controller 和直接模块测试通过唯一的 `InventoryOperations` Seam 执行移库和查询任务列表；现有 HTTP/UI、其他库存调用方和业务结果保持不变。旧类型仍暂时保留作为回滚点，但不再有生产或测试调用方。

## 修改边界

- 允许修改：Inventory Controller 的构造注入与两处委托；`T03TransferContractIntegrationTest`、`InventoryConcurrencyTest` 的 Interface 注入和调用；本 Ticket 验证证据。
- 禁止修改：`InventoryService` 业务 Implementation、Inbound/Shipment/Stocktake、HTTP 路径/DTO/响应、UI、旧 Interface/Adapter 的删除、测试断言与场景、数据库、平台、依赖和后续 Contract 内容。
- Module `AGENTS.md`：读取根、`business-wms/AGENTS.md` 与 `training-server/AGENTS.md`；测试必须通过公开业务 Seam，不直接改数据库来迎合结构。

## 验收条件

- [x] Inventory Controller 只注入一个 `InventoryOperations`，移库和任务列表委托给它。
- [x] 两个直接测试调用方改用 `InventoryOperations`，不删除、弱化或改写现有断言。
- [x] Inbound/Shipment/Stocktake 及其他现有调用方没有无关 Diff。
- [x] 搜索确认 `InventoryTransferOperations` 只剩定义和 Legacy Adapter 实现，没有生产/测试消费方。
- [x] T04 baseline 继续保持 0 failures、0 errors，HTTP/错误/事务/并发行为不变。
- [x] 可将三个调用方恢复到旧 Interface 并独立回退本 Ticket。

## 验证命令

```text
./scripts/classroom-test.sh T04 baseline
rg -n "InventoryTransferOperations|LegacyInventoryTransferService" business-wms training-server
git diff --check
```

## 停止条件

- 统一注入导致 Bean 歧义、事务代理路径变化或 HTTP 行为变化。
- 需要修改业务算法、测试数据/断言、其他业务调用方或批准范围外文件。
- Ticket 01 未验收，或迁移后仍有未识别的实际调用方。

## 人工验收

- 结论：ACCEPTED
- 时间：2026-09-10 20:43:11 +0800
- 开发负责人原话：“确认，进入 tickets 3”
