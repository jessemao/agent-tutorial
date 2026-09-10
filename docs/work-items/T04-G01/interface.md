# Interface：统一库存业务修改入口

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T04-G01` |
| 状态 | `APPROVED` |
| Spec 路径/版本 | `spec.md` / v1 / `APPROVED` |
| 代码起点 | `s2-t04-start` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| 契约所有者/调用方 | 仓储事业部；`InventoryController`、`InboundService`、`ShipmentService`、`StocktakeService`、公开行为测试；具名负责人待指定 |

## 契约范围

- 类型：Module Interface；HTTP 与 UI 契约只做保持，不新增或修改。
- 新增、修改或保持的契约：在现有 `InventoryOperations` 增加已有语义的 `transfer(InventoryTransferCommand)` 和 `listTransferTasks()`；其他方法及所有 HTTP/UI 行为保持。迁移后删除第二公开 `InventoryTransferOperations` Interface 和纯透传 Adapter。
- 明确非目标：不改变命令/视图类型、错误码、事务、幂等、锁、流水、审计、任务查询投影；不拆新的读 Interface，不创建通用 execute/change 命令总线。

## UI 交互契约

- 页面入口与完整操作路径：现有“移库作业”、库存查询及其他库存业务页面全部保持。
- 字段标签、业务值、默认值和可编辑/只读/可选择状态：全部保持起点行为，不因 Module Interface 重构调整。
- 系统生成标识与连续操作、重试、冲突行为：移库单号和幂等键生成、相同载荷重放、冲突载荷拒绝均保持。
- 成功与失败反馈、结果明细及保留行为：两端余额详情、服务端错误、失败后页面状态均保持。
- 列表、查询、筛选、刷新及重新进入后的行为：`GET /api/wms/inventory/transfers` 的列表、顺序和刷新恢复保持；`listTransferTasks` 只是改由统一 Module Interface 暴露。

## 请求或输入

目标 Java Interface 草案：

```java
public interface InventoryOperations {
    InventoryBalanceView receive(InventoryCommand command);
    InventoryBalanceView reserve(InventoryCommand command);
    InventoryBalanceView release(InventoryCommand command);
    InventoryBalanceView ship(InventoryCommand command);
    InventoryTransferView transfer(InventoryTransferCommand command);
    InventoryBalanceView adjustFromCount(InventoryCountAdjustmentCommand command);
    InventoryBalanceView getBalance(Long skuId, Long warehouseId, Long locationId);
    InventoryBalanceView getExistingBalance(Long skuId, Long warehouseId, Long locationId);
    List<InventoryTransferTaskView> listTransferTasks();
}
```

| 字段/方法 | 类型 | 必填 | 约束 | 示例 |
| --- | --- | --- | --- | --- |
| `transfer(command)` | Module method | 是 | 沿用 `InventoryTransferCommand`；一次调用原子完成两端变化 | `inventoryOperations.transfer(command)` |
| `listTransferTasks()` | Module method | 否 | 只读；沿用当前双流水投影和排序 | `inventoryOperations.listTransferTasks()` |
| 其他现有方法 | Module methods | 按原契约 | 签名、语义和调用方保持 | `receive`、`reserve`、`release`、`ship`、`adjustFromCount`、余额查询 |

## 响应或输出

| 字段/返回值 | 类型 | 语义 | 示例 |
| --- | --- | --- | --- |
| `transfer` 返回值 | `InventoryTransferView` | 保持首次成功或安全重放的移库单号及源/目标余额快照 | `TR-T03-001`, source 6, target 4 |
| `listTransferTasks` 返回值 | `List<InventoryTransferTaskView>` | 保持由成对移库流水生成的已完成任务列表 | 现有字段与排序不变 |
| 其他返回值 | 现有 View | 完全保持现有 Interface 契约 | 无变化 |

## 错误与边界行为

| 条件 | HTTP/错误码/异常 | 调用方行为 | 最终状态 |
| --- | --- | --- | --- |
| 数量非正或源/目标相同 | 400 / `INVALID_REQUEST` | 修正请求 | 数据不变 |
| 库位不存在 | 409 / `WMS_LOCATION_NOT_FOUND` | 刷新库位后重试 | 数据不变 |
| 库位停用或异仓 | 409 / `WMS_INVALID_LOCATION` | 更正输入 | 数据不变 |
| 源余额不存在 | 409 / `WMS_INVENTORY_NOT_FOUND` | 核对库存 | 目标不创建，数据不变 |
| 源可用量不足 | 409 / `WMS_INSUFFICIENT_AVAILABLE` | 降低数量或补库存 | 两端、流水、审计不变 |
| 目标溢出 | 409 / `WMS_INVENTORY_OVERFLOW` | 停止并核对数据 | 全部回滚 |
| 同键不同载荷或单边流水 | 409 / `WMS_IDEMPOTENCY_CONFLICT` | 使用新键或核对原请求 | 已有数据不变 |
| 同键同载荷 | 200 / `OK` | 使用首次结果 | 返回首次快照，不重复写入 |
| 反向并发移库 | 成功或现有业务错误 | 按结果处理 | 无死锁泄漏，非负且总量守恒 |

以上均为保持项，不是本重构新增的业务规则。

## 兼容、迁移与回滚

- 旧调用方影响：Inbound/Shipment/Stocktake 及现有 `InventoryOperations` 调用无需修改；Inventory Controller 和两个直接测试调用方移除第二依赖并改用统一 Interface。
- 兼容窗口/版本策略：Expand 与 Migrate 期间允许两个 Interface 在代码内短暂并存，仅用于可回退迁移；不得增加新旧双写或长期兼容层。Contract 完成后旧 Interface/Adapter 必须零引用并删除。
- 迁移步骤：Expand `InventoryOperations` → Migrate Controller/测试 → 搜索零引用 → Contract 删除旧 Interface/Adapter → 同步架构事实。
- 回滚方式：每阶段按逆序恢复调用方和旧类型；Implementation、HTTP 和数据库均未改变，无数据迁移或数据回滚。

## 契约验证

| 契约条目 | 测试/检查 | 预期结果 |
| --- | --- | --- |
| 统一 Module Interface | 搜索生产与测试调用方、检查 Spring 注入 | 只有 `InventoryOperations` 暴露库存修改和任务列表；Bean 注入唯一 |
| HTTP/UI 完全保持 | T04 baseline、前端构建、页面复测 | 路径、字段、状态、反馈和列表无变化 |
| 移库原子性与幂等 | T03 Contract/Atomicity 测试 | 错误码、首次快照、重放/冲突和回滚保持 |
| 锁顺序与并发 | `InventoryConcurrencyTest`，必要时 MySQL Profile | 无死锁/超时泄漏，库存不变式保持 |
| 双流水、审计和列表 | 公开行为断言和 `GET /transfers` | 记录次数、内容、排序及刷新恢复保持 |
| 既有库存能力 | Inventory Regression、WMS Flow、Module 与全量验证 | 收货、预占、释放、出库、取消和盘点无回归 |

## 审批记录

| 角色/调用方 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| 候选决策角色 | 当前用户代理 | APPROVED | 2026-09-10 | 人工原话：“1. 选A” |
| 查询归属决策角色 | 当前用户代理 | APPROVED | 2026-09-10 | 人工原话：“2. 同意纳入” |
| 开发负责人 | 当前用户代理 | APPROVED | 2026-09-10（Asia/Shanghai） | 人工原话：“批准”；确认具名 Interface、迁移顺序、保持行为和修改范围，允许进入 `/to-spec`，尚未批准实施 |
| UI/契约所有者 | 当前用户代理 | APPROVED | 2026-09-10 | 人工原话：“批准。/to-tickets T04-G01”；只批准 HTTP/UI 完全保持，无契约变更 |
