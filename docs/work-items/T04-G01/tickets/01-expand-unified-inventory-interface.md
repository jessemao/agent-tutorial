# 01：扩展统一库存 Interface

**状态：** accepted  
**Blocked by：** None（可立即开始）  
**Spec：** `docs/work-items/T04-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06` / 代码起点版本

## 交付行为

调用方可通过现有 `InventoryOperations` Interface 获得与起点完全相同的移库和移库任务查询能力，同时旧移库 Interface 暂时保留，使当前生产调用方和全部外部行为继续可用。此 Ticket 只扩展稳定 Seam，不迁移调用方、不删除旧类型。

## 修改边界

- 允许修改：`business-wms` 库存 Interface；现有库存 Implementation 仅增加 Interface 覆盖关系；本 Ticket 必需的编译/行为测试证据。
- 禁止修改：Controller、Inbound/Shipment/Stocktake 调用方、旧移库 Interface/Adapter、业务算法、HTTP/UI、错误码、数据库、Repository、事务/锁/幂等/流水/审计、`platform-*`、依赖和后续 Ticket 内容。
- Module `AGENTS.md`：实施前读取根及 `business-wms/AGENTS.md`；仅使用其中 T04 受控遗留例外完成过渡。

## 验收条件

- [ ] `InventoryOperations` 以具名方法暴露 `transfer` 和 `listTransferTasks`，签名与已批准 Interface 一致。
- [ ] `InventoryService` 继续承载原有 Implementation，并显式满足新增 Interface 方法；方法体、事务注解和调用顺序不改变。
- [ ] 旧 `InventoryTransferOperations` 与 `LegacyInventoryTransferService` 仍存在，现有调用方未迁移。
- [ ] T04 baseline 的 35 项保护测试仍为 0 failures、0 errors；实际数量以命令输出记录。
- [ ] 本 Ticket 可通过移除新增 Interface 声明和覆盖标记独立回退，无数据处理。

## 验证命令

```text
./scripts/classroom-test.sh T04 baseline
git diff --check
```

## 停止条件

- 需要修改现有移库 Implementation、外部契约或任何业务规则。
- 新增方法导致 Spring Bean 注入冲突、编译失败或保护测试变化。
- 为减少方法数需要引入通用 Command、结果包装、新 Interface 或 Adapter。

## 人工验收

- 开发负责人：当前用户代理
- 结论：ACCEPTED
- 时间：2026-09-10 20:30:58 +0800
- 人工原话：“验收完成”
- 适用对象：基于 `ae996fe5` 的 Ticket 01 未提交工作树及 `02_verification.md` 中记录的验证结果
