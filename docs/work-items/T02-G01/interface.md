# Interface：调整入库分批收货规则

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T02-G01 |
| 状态 | APPROVED |
| Spec 路径/版本 | `spec.md` v1 |
| 代码起点 | `s2-t02-start`；`91af1d6fa4f4851129d660624691136d4cb27697` |
| 契约所有者/调用方 | UI/契约所有者（待指定）；课堂前端与 `training-server` 集成测试 |

## 契约范围

- 类型：HTTP / UI / Module。
- 新增、修改或保持的契约：保持现有 HTTP 路径、请求、响应封装、响应字段及 `InboundOperations.receive` 方法形状；`quantity` 改为可小于剩余量的本次实收；新增 `PARTIALLY_RECEIVED` 状态和已确认错误行为；UI 增加部分收货展示与第二批操作。
- 明确非目标：不新增剩余量响应字段、批次查询端点、任意数量输入或平台级 Interface。

## 请求或输入

| 字段/方法 | 类型 | 必填 | 约束 | 示例 |
| --- | --- | --- | --- | --- |
| `POST /api/wms/inbounds/{id}/receive` 的 `id` | Long | 是 | 已存在的入库单 ID | `42` |
| `idempotencyKey` | String | 是 | 非空；在 `RECEIVE` 操作类型内全局唯一；重放必须匹配原入库单和数量 | `inbound-42-batch-1` |
| `quantity` | long | 是 | 本次实收，必须大于 0 且不超过当前剩余量 | `4` |
| `InboundOperations.receive(id, idempotencyKey, quantity)` | Module Interface | 是 | 与 HTTP 语义一致；调用方不直接访问批次或库存 Repository | `receive(42, key, 4)` |

## 响应或输出

| 字段/返回值 | 类型 | 语义 | 示例 |
| --- | --- | --- | --- |
| `ApiResponse.success/data` | `InboundView` | 保持现有成功响应封装 | `success: true` |
| `id` | Long | 入库单 ID | `42` |
| `orderNo` | String | 入库单号 | `IN-T02-DC3EE8C1DA76` |
| `skuId` | Long | 商品编号 | `371511033704173` |
| `warehouseId` / `locationId` | Long | 仓库与库位 | `1` / `1` |
| `plannedQuantity` | long | 计划总量 | `10` |
| `receivedQuantity` | long | 请求成功后的累计实收；安全重放返回首次批次完成时的累计快照 | 首批 `4`，末批 `10` |
| `status` | enum | `CREATED`、`PARTIALLY_RECEIVED` 或 `RECEIVED` | `PARTIALLY_RECEIVED` |
| UI 待收数量 | 派生值 | `plannedQuantity - receivedQuantity`，不新增服务端字段 | `6` |

## 错误与边界行为

| 条件 | HTTP/错误码/异常 | 调用方行为 | 最终状态 |
| --- | --- | --- | --- |
| `quantity <= 0` 或请求校验失败 | HTTP 400 / `INVALID_REQUEST` | 关闭弹窗、显示错误并刷新 | 单据、批次、库存不变 |
| 本次数量超过剩余量 | HTTP 409 / `WMS_INBOUND_OVER_RECEIPT` | 显示错误并刷新真实数据 | 单据、批次、库存不变 |
| 入库单已完成且不是安全重放 | HTTP 409 / `WMS_INBOUND_STATE` | 禁止继续操作并刷新 | 数据不变 |
| 同键但入库单或数量与原批次不一致 | HTTP 409 / `WMS_IDEMPOTENCY_CONFLICT` | 显示冲突并刷新 | 数据不变 |
| 同键、同入库单、同数量安全重放 | HTTP 200 / 首次完成快照 | 按快照展示，不重复提示入账 | 库存和审计不重复 |
| 入库单不存在 | 保持现有 HTTP 409 / `WMS_INBOUND_NOT_FOUND` | 显示错误 | 数据不变 |
| 库存写入或同步审计失败 | 保持现有错误映射 | 显示错误并刷新 | 本批全部数据库变化回滚 |

## 兼容、迁移与回滚

- 旧调用方影响：现有一次提交计划总量仍成功；路径、请求和已有响应字段均不变。只识别二态枚举的调用方必须在上线前支持 `PARTIALLY_RECEIVED`。
- 兼容窗口/版本策略：同一 Interface 原地演进，不新增版本；后端与课堂前端作为同一候选版本交付。
- 迁移步骤：新增批次证据结构；为带原收货幂等键的既有 `RECEIVED` 单据建立等价成功快照；`CREATED` 单据保持无批次记录；验证后再启用分批规则。
- 回滚方式：未接收新分批数据时可回退应用和迁移；已存在分批数据时先停用入口并由数据所有者批准数据处理，不允许直接回退为不能表达多批的旧结构。

## 契约验证

| 契约条目 | 测试/检查 | 预期结果 |
| --- | --- | --- |
| 保持路径、请求和响应字段 | 既有一次收满公开 HTTP 测试 | 无调用方改造仍成功 |
| 新状态与累计语义 | 4+6 公开 HTTP 测试 | 首批 `PARTIALLY_RECEIVED/4`，末批 `RECEIVED/10` |
| 错误码与无副作用 | 输入无效、超收、完成后收货、幂等冲突测试 | HTTP/错误码匹配且数据不变 |
| 首次快照重放 | 完成第二批后重放首批键 | 返回首批 `PARTIALLY_RECEIVED/4` 快照且库存仍为 10 |
| UI 契约 | UI/QA 页面复测 | 固定 4+6、三态文案、按钮和真实数据正确 |

## 审批记录

| 角色/调用方 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| UI/契约所有者 | 待指定 | 输入已确认，正式批准待登记 | 2026-09-08 | 新状态、错误码及课堂 UI 规则已在分析阶段确认 |
| 开发负责人 | Captain Mao | APPROVED | 2026-09-08 13:59:58 CST | 以开发负责人角色批准；未附加理由或额外条件；越界时停止 |
