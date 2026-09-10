# Interface：同仓库库内移库

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T03-G01 |
| 状态 | APPROVED |
| Spec 路径/版本 | `spec.md` / v1 / APPROVED |
| 代码起点 | `s2-t03-start` / `e27b3d3cf5d87b2ffbf13e6b1b9339a1d7257858` |
| 契约所有者/调用方 | UI/契约所有者；课堂 UI、API 调用方和集成测试 |

## 契约范围

- 类型：HTTP + UI + Module Interface。
- 新增、修改或保持的契约：新增 `InventoryOperations.transfer(InventoryTransferCommand)`、`POST /api/wms/inventory/transfer` 和只读 `GET /api/wms/inventory/transfers`；启用课堂 UI 的“移库作业”入口。现有库存方法和页面行为保持不变。
- 明确非目标：跨仓调拨、占用量迁移、异步审批/撤销、批量移库、新平台 Interface、新数据表或对现有调用方的破坏性变更。

## UI 交互契约

- 页面入口为“移库作业”；操作人填写 SKU、源库位文字、目标库位文字和正整数数量后提交。
- 源/目标库位可编辑，默认分别为 `A01-01-01`、`A01-01-02`；页面将两个有效业务文字映射为内部 ID，其他文字继续提交为无效库位测试，不在前端伪造成功。
- 页面自动生成移库单号和幂等键，成功后为下一次普通操作生成新标识；页面不显示移库单号输入框。
- 成功反馈显示完整请求明细和两端余额；失败反馈保留服务端错误码和消息。
- 页面加载、成功提交和人工刷新时调用 `GET /api/wms/inventory/transfers`，显示可跨刷新恢复的已完成任务列表。

## 请求或输入

| 字段/方法 | 类型 | 必填 | 约束 | 示例 |
| --- | --- | --- | --- | --- |
| `POST /api/wms/inventory/transfer` | HTTP | 是 | `Content-Type: application/json`；操作人沿用现有 `X-Operator` 契约 | `POST /api/wms/inventory/transfer` |
| `idempotencyKey` | string | 是 | 非空，同键同载荷安全重放，同键不同载荷冲突 | `transfer-t03-001` |
| `transferNo` | string | 是 | 非空，作为业务参考号、流水参考号和审计业务 ID | `TR-T03-001` |
| `skuId` | long | 是 | 非空，源/目标使用同一 SKU | `303` |
| `warehouseId` | long | 是 | 非空，源/目标库位都必须属于该仓库 | `1` |
| `sourceLocationId` | long | 是 | 非空，存在、启用，不得等于目标库位 | `1` |
| `targetLocationId` | long | 是 | 非空，存在、启用，不得等于源库位 | `2` |
| `quantity` | long | 是 | 正整数，不超过源可用量，目标增加后不超过 `long` 上界 | `4` |
| `InventoryOperations.transfer(command)` | Module Interface | 是 | 一次调用必须原子完成两端变更；调用方不得拆分操作 | `transfer(command)` |

课堂 UI 不要求人工输入移库单号；每次普通提交自动生成新的 `transferNo` 和对应幂等键。重放与冲突契约由 API/自动化测试使用显式键验证。

HTTP 请求示例：

```json
{
  "idempotencyKey": "transfer-t03-001",
  "transferNo": "TR-T03-001",
  "skuId": 303,
  "warehouseId": 1,
  "sourceLocationId": 1,
  "targetLocationId": 2,
  "quantity": 4
}
```

## 响应或输出

| 字段/返回值 | 类型 | 语义 | 示例 |
| --- | --- | --- | --- |
| `success` | boolean | 沿用统一响应包装，成功为 `true` | `true` |
| `code` | string | 成功为 `OK`，失败见错误表 | `OK` |
| `message` | string | 统一响应消息 | `success` |
| `data.transferNo` | string | 移库业务单号 | `TR-T03-001` |
| `data.source` | balance object | 源库位首次成功后的余额快照 | `availableQuantity: 6` |
| `data.target` | balance object | 目标库位首次成功后的余额快照 | `availableQuantity: 4` |
| `source/target.skuId` | long | 库存 SKU | `303` |
| `source/target.warehouseId` | long | 所属仓库 | `1` |
| `source/target.locationId` | long | 源或目标库位 | `1` / `2` |
| `source/target.availableQuantity` | long | 操作后可用量 | `6` / `4` |
| `source/target.reservedQuantity` | long | 操作后占用量，移库不改变它 | `0` |

同键同载荷重放返回与首次成功相同的 `data`，即使后续库存已发生其他变化。

`GET /api/wms/inventory/transfers` 返回由成对移库流水生成的已完成任务列表，字段包括 `transferNo`、`skuId`、`warehouseId`、`sourceLocationId`、`targetLocationId`、`quantity` 和 `createdAt`；不创建独立移库任务表。

## 错误与边界行为

| 条件 | HTTP/错误码/异常 | 调用方行为 | 最终状态 |
| --- | --- | --- | --- |
| 必填字段缺失、`quantity <= 0`、源/目标库位相同 | 400 / `INVALID_REQUEST` | 修正请求，不重用错误载荷 | 余额、流水、审计不变 |
| 源或目标库位不存在 | 409 / `WMS_LOCATION_NOT_FOUND` | 刷新库位数据后重新选择 | 全部回滚/不变 |
| 库位停用或不属于请求仓库 | 409 / `WMS_INVALID_LOCATION` | 更正仓库/库位 | 全部回滚/不变 |
| 源 SKU 余额不存在 | 409 / `WMS_INVENTORY_NOT_FOUND` | 确认 SKU 和源库位 | 全部回滚/不变 |
| 源可用量小于移库数量 | 409 / `WMS_INSUFFICIENT_AVAILABLE` | 降低数量或补充库存 | 全部回滚/不变 |
| 目标可用量 + 数量超出 `long` 上界 | 409 / `WMS_INVENTORY_OVERFLOW` | 停止请求并人工核对数据 | 全部回滚/不变 |
| 同幂等键的载荷不同，或只存在一条移库流水 | 409 / `WMS_IDEMPOTENCY_CONFLICT` | 不得盲目重试，应使用新键或核对原请求 | 余额、已有流水、审计不变 |
| 同幂等键且载荷相同 | 200 / `OK` | 将结果作为首次成功结果处理 | 返回首次快照，不再改余额或新增记录 |
| 反向并发移库 | 正常成功或按当时余额返回上述业务错误 | 可按业务结果重新发起新请求 | 不死锁，两端数量不变式成立 |

错误优先级：请求格式/同库位校验先于事务；事务内先锁定并校验库位，再检查幂等快照，然后校验源余额和数量。

## 兼容、迁移与回滚

- 旧调用方影响：只新增 Module/HTTP/UI 能力，不改变现有字段、路径和结果。
- 兼容窗口/版本策略：当前课堂版本直接增加新入口，无旧移库调用方或废弃窗口。
- 迁移步骤：无数据迁移；课堂 UI 从禁用菜单切换为可用页面，调用新 HTTP Interface。
- 回滚方式：回退本 Work Item 的 UI、Controller、Module Interface、Implementation 和测试 Diff；不删除、改写已有库存业务记录。

## 契约验证

| 契约条目 | 测试/检查 | 预期结果 |
| --- | --- | --- |
| 正常及全量移库 | HTTP 集成测试，再查询两端余额 | 200，源减目标增，占用不变，允许源可用归零 |
| 空目标余额 | 仅预置源余额后移库 | 自动建立目标余额并返回正确结果 |
| 请求与库位边界 | 分别提交零/负数、同库位、缺失/停用/异仓库位 | 返回表中稳定状态码/错误码，数据不变 |
| 幂等 | 同键同载荷重放，另测同键不同数量/库位 | 重放返回首次快照；冲突返回 409；余额和记录次数不变 |
| 原子性与溢出 | 库存不足、目标 `Long.MAX_VALUE` 后移入、流水失败注入 | 两端余额、流水和已知审计调用均无部分成功 |
| 并发锁顺序 | 两个方向并发移库，检查 H2 并补充 MySQL Profile | 无死锁/超时泄漏，总量与非负不变式成立 |
| 流水/审计 | 统计首次成功、安全重放和失败后的记录/调用次数 | 首次成功为两条流水/一次审计，后两者不新增 |
| UI | 生产构建与 UI/QA 从菜单完成正常、失败、重放操作 | 字段、成功结果与错误反馈与 HTTP Interface 一致 |
| 原有能力 | `business-wms`、`training-server` 回归与 `mvn clean verify` | 现有契约不变且全部通过 |

## 审批记录

| 角色/调用方 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| 业务所有者 | 当前用户代理 | APPROVED | 2026-09-08 | Q1—Q5、Q9 规则已确认 |
| UI/契约所有者 | 当前用户代理 | APPROVED | 2026-09-08 | Q5—Q8、Q12 契约已确认 |
| 开发负责人 | 当前用户代理 | APPROVED | 2026-09-08 21:48:05 +0800 | 人工原话：“approved”；批准完整 Interface、审计 Adapter 限制与实施边界 |
