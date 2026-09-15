# Interface：库存盘点与差异调整

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T06-G01` |
| 状态 | `APPROVED` |
| Spec 路径/版本 | `spec.md` v1 / `APPROVED`；来源为已确认 `01_analysis.md` 与已批准 `design.md` |
| 代码起点 | `b09d8e69db9403e891bdc2562d3742487d3cdc23` |
| 契约所有者/调用方 | UI/契约所有者、仓储事业部；调用方为课堂页面、HTTP Controller、集成测试 |

## 契约范围

- 类型：UI / HTTP / Module。
- 新增：盘点列表、详情、创建、草稿保存、提交、重新编辑、驳回、取消、审核与差异二次确认；`InventoryCountOperations`；`InventoryOperations.reconcileCount`。
- 保持：现有入库、出库、移库、余额查询路径、字段、状态码和错误码。
- 明确非目标：用户输入单号/幂等键/内部 ID、完整权限系统、附件、删除、反审、跨仓单据、修改平台统一响应。

## UI 交互契约

- 页面入口：新增“库存盘点”；默认进入按创建时间倒序的列表，可按盘点单号、仓库、状态筛选。
- 新建：选择一个仓库，再通过 SKU 编码/名称和库位编码/名称添加一条或多条明细；不显示内部 ID；系统生成盘点单号和创建幂等键。
- 草稿：允许部分实盘未填写；零显示为明确的 `0`，未填写保持空；可增删明细并整表保存。
- 提交：所有明细必须填写非负整数实盘数；提交后页面只读。
- 审核：审核人与创建人、最后提交人不同；若最新账面变化，页面展示最新逐行差异并要求显式确认，确认请求使用新幂等键和服务端 opaque token。
- 驳回/取消：必须输入原因；驳回后可“重新编辑”返回草稿；取消不可恢复但可复制新单。
- 成功：用服务端返回的完整详情刷新页面；批准结果显示创建/提交快照、审核前账面、实盘、差异及调整后可用/占用。
- 失败：保留当前表单输入，同时展示服务端有效版本、结构化错误明细和可执行下一步；不得仅依赖 message 解析。
- 刷新/重进：通过列表和详情恢复所有已保存状态；本地未保存输入不承诺恢复。

## Module Interface

### `InventoryCountOperations`

| 方法 | 输入 | 输出 | Interface 语义 |
| --- | --- | --- | --- |
| `create` | `CreateInventoryCount` | `InventoryCountView` | 原子校验全部维度、占用活动范围、生成单号并返回草稿 |
| `saveDraft` | ID + `SaveInventoryCountDraft` | `InventoryCountView` | 按期望版本完整替换草稿明细/实盘；不可部分保存 |
| `transition` | ID + `InventoryCountTransition` | `InventoryCountView` | 执行 `SUBMIT/REOPEN/REJECT/CANCEL` 及其状态、原因和幂等规则 |
| `approve` | ID + `ApproveInventoryCount` | `InventoryCountApprovalResult` | 原子审核；返回 `APPROVED` 或结构化 `CONFIRMATION_REQUIRED` |
| `get` | ID | `InventoryCountView` | 返回刷新可恢复的完整详情 |
| `list` | `InventoryCountQuery` | `InventoryCountPage` | 固定排序、筛选、上限分页 |

### `InventoryOperations` 扩展

| 方法 | 输入 | 输出 | Interface 语义 |
| --- | --- | --- | --- |
| `reconcileCount` | 盘点单号、动作幂等键、同仓规范排序明细、实盘总量、可选确认 token | 每行最新账面/差异/调整后余额或 confirmation-required 事实 | 一个事务内锁定、校验和调整整批；保留占用量；任一错误全批失败；零差异不写流水 |

## HTTP 契约候选

| 方法与路径 | 用途 | 成功结果 |
| --- | --- | --- |
| `GET /api/wms/inventory-counts` | 列表/筛选/分页 | `ApiResponse<InventoryCountPage>` |
| `POST /api/wms/inventory-counts` | 创建草稿 | `ApiResponse<InventoryCountView>` |
| `GET /api/wms/inventory-counts/{id}` | 详情/刷新恢复 | `ApiResponse<InventoryCountView>` |
| `PUT /api/wms/inventory-counts/{id}` | 完整保存草稿 | `ApiResponse<InventoryCountView>` |
| `POST /api/wms/inventory-counts/{id}/transitions` | 提交/重新编辑/驳回/取消 | `ApiResponse<InventoryCountView>` |
| `POST /api/wms/inventory-counts/{id}/approval` | 审核或携 token 确认 | `ApiResponse<InventoryCountView>`；差异变化时见结构化 409 |
| `GET /api/wms/master-data/skus` | SKU 业务值选择 | `ApiResponse<List<SkuOption>>` |
| `GET /api/wms/master-data/warehouses` | 仓库选择 | `ApiResponse<List<WarehouseOption>>` |
| `GET /api/wms/master-data/locations?warehouseId=...` | 库位选择 | `ApiResponse<List<LocationOption>>` |

主数据查询路径是本次用户旅程依赖能力候选。若开发负责人决定拆为独立 Work Item，则本任务在没有其他批准来源前仍不能交付页面，因为禁止让用户操作内部 ID。

## 请求或输入

| 字段/方法 | 类型 | 必填 | 约束 | 示例 |
| --- | --- | --- | --- | --- |
| Header `X-Operator` | string | 所有写请求必填 | 非空；服务端取得，禁止请求体覆盖 | `counter-a` |
| `idempotencyKey` | string | 所有写请求必填 | 客户端自动生成；普通新动作使用新键，网络重试复用 | `count-save-...` |
| `expectedVersion` | long | 创建外写请求必填 | 必须等于服务端当前盘点版本 | `3` |
| `warehouseId` | long | 创建必填 | 已启用仓库；页面通过业务值选择后提交 ID | `1` |
| `lines` | array | 创建/保存必填 | 非空；同一 `skuId + locationId` 不重复；全部属于所选仓库 | — |
| `skuId` | long | 明细必填 | 有效 SKU；页面显示 code/name | `303` |
| `locationId` | long | 明细必填 | 有效库位且属于仓库；页面显示 code | `1` |
| `countedTotal` | nullable long | 草稿可空，提交前必填 | 非负整数；`null` 与 `0` 不同 | `0` |
| `varianceNote` | nullable string | 否 | 有差异时可填；长度上限由 Spec 固化 | `货架为空` |
| `action` | enum | transition 必填 | `SUBMIT/REOPEN/REJECT/CANCEL` | `SUBMIT` |
| `reason` | string | REJECT/CANCEL 必填 | trim 后非空；长度上限由 Spec 固化 | `需复盘` |
| `note` | nullable string | 否 | 创建/保存/提交/审核备注 | — |
| `confirmationToken` | nullable string | 二次确认必填 | 服务端 opaque token；调用方不解析 | `opaque...` |
| `correctionOfCountId` | nullable long | 否 | 只允许引用已批准盘点 | `12` |

普通用户不输入 `countNo`、内部版本细节、余额版本或幂等键；页面 Adapter 自动管理技术字段。

## 响应或输出

| 字段/返回值 | 类型 | 语义 | 示例 |
| --- | --- | --- | --- |
| `id/countNo/status/version` | long/string/enum/long | 盘点身份、状态和并发版本 | `12 / IC-00000012 / SUBMITTED / 3` |
| `warehouse` | business option | ID + code + name | `WH-SH / 上海培训仓` |
| `created/submitted/rejected/cancelled/approved` | actor/time/reason | 各阶段审计事实；未发生则空 | — |
| `lines[].sku/location` | business option | ID + 业务 code/name；页面不展示 ID | — |
| `creationBookTotal` | long | 创建时 `available + reserved` | `10` |
| `submittedBookTotal` | nullable long | 提交时账面总量 | `10` |
| `approvalBookTotal` | nullable long | 审核锁内最新账面总量 | `12` |
| `countedTotal` | nullable long | 实盘物理总量；零有效 | `8` |
| `difference` | nullable long | `countedTotal - approvalBookTotal` | `-4` |
| `availableAfter/reservedAfter` | nullable long | 批准后的最终余额 | `6 / 2` |
| `approvalOutcome` | enum | `APPROVED` 或 `CONFIRMATION_REQUIRED` | `CONFIRMATION_REQUIRED` |
| `confirmationToken` | nullable string | 最新差异二次确认令牌 | `opaque...` |
| `conflicts/errors` | array | 结构化库存维度、冲突单号和稳定错误码 | — |

## 错误与边界行为

| 条件 | HTTP/错误码/异常 | 调用方行为 | 最终状态 |
| --- | --- | --- | --- |
| 请求格式、空明细、负数、重复明细 | `400 / INVALID_REQUEST` | 定位字段并保留输入 | 不变 |
| 缺失操作者 | `409 / PLATFORM_OPERATOR_REQUIRED`（保持现有映射） | 要求设置操作者 | 不变 |
| 盘点不存在 | `409 / WMS_COUNT_NOT_FOUND` | 返回列表 | 不变 |
| 非法状态动作 | `409 / WMS_COUNT_INVALID_STATE` | 刷新详情 | 不变 |
| 陈旧盘点版本 | `409 / WMS_COUNT_VERSION_CONFLICT` + 当前版本 | 刷新并人工重做动作 | 不变 |
| 活动范围冲突 | `409 / WMS_COUNT_SCOPE_CONFLICT` + 冲突单号/明细 | 打开冲突单或调整范围 | 创建整单失败 |
| 提交仍有空实盘 | `409 / WMS_COUNT_INCOMPLETE` + 明细 | 补齐后重试 | 保持草稿 |
| 自己审核自己的单据 | `409 / WMS_COUNT_SELF_APPROVAL` | 更换审核人 | 保持待审核 |
| 最新账面与提交快照不同 | `409 / WMS_COUNT_DIFFERENCE_CHANGED` + 最新详情/token | 展示新差异并显式确认 | 保持待审核 |
| token 陈旧 | `409 / WMS_COUNT_CONFIRMATION_STALE` + 新详情/token | 再次确认 | 保持待审核 |
| 实盘小于占用 | `409 / WMS_COUNT_BELOW_RESERVED` + 明细 | 业务处理占用或修正实盘 | 保持待审核 |
| 主数据缺失/失效/异仓 | 现有位置错误或新增稳定主数据错误 + 明细 | 修正范围 | 整单不处理 |
| 数量溢出 | `409 / WMS_INVENTORY_OVERFLOW` + 明细 | 修正输入/数据 | 整单不处理 |
| 同幂等键不同输入 | `409 / WMS_IDEMPOTENCY_CONFLICT` | 生成新键仅用于新业务动作 | 不变 |

结构化 409 采用盘点业务本地响应，保持 `success/code/message/data` envelope，其中 `data` 可携最新盘点、token 和逐行错误；不得修改平台 `ApiResponse` 或全局异常处理。具体 Java 类型和 handler 位置在 Spec/Ticket 中固定。

## 兼容、迁移与回滚

- 旧调用方影响：仅在 `InventoryOperations` 增加方法，现有实现类同步实现；现有业务调用方法及 HTTP 路径不变。
- 兼容窗口/版本策略：新端点首次引入，无旧客户端；同一候选提交内交付页面与后端。
- 迁移步骤：新增盘点/SKU 表与演示数据；既有库存余额和流水不做数据迁移。
- 回滚方式：停止新盘点入口，移除页面/API/盘点表及 Interface 扩展；已批准形成的库存事实由业务所有者单独处置，不自动反转。

## 契约验证

| 契约条目 | 测试/检查 | 预期结果 |
| --- | --- | --- |
| 完整页面旅程 | 浏览器/页面测试 + API 集成测试 | 创建至审核、驳回/取消、列表详情和刷新均可完成 |
| 业务值与输入控制 | 页面测试/DTO 校验 | 不显示内部 ID；空与零区分；技术标识自动生成 |
| 状态与版本 | Module/MockMvc 测试 | 非法状态及陈旧版本稳定冲突 |
| 结构化确认 | MockMvc 测试 | 409 携最新详情与 opaque token；确认后成功 |
| 幂等 | 每个写端点同参/异参/新键重复 | 安全重放、冲突和状态错误符合约定 |
| 批量原子性 | 故障注入 + 最终状态查询 | 无部分余额、流水、批准状态或成功审计 |
| 并发 | MySQL Profile | 活动范围、库存变化和锁顺序保持不变式 |
| 兼容 | 现有 API 回归 | 入库、出库、移库和余额查询行为不变 |

## 审批记录

| 角色/调用方 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| 业务所有者 | 用户（本会话，具名信息未登记） | APPROVED | 2026-09-13 | 领域、状态、库存语义与 SKU 范围 |
| UI/契约所有者 | 用户（本会话，具名信息未登记） | APPROVED | 2026-09-13 | 页面旅程、HTTP、结构化冲突与业务值 |
| 技术中台所有者 | 用户（本会话，具名信息未登记） | APPROVED | 2026-09-13 | 平台保持只读、复用现有机制 |
| 课程维护团队 | 用户（本会话，具名信息未登记） | APPROVED | 2026-09-13 | SKU 演示数据与课堂页面范围 |
| 开发负责人 | 用户（本会话，具名信息未登记） | APPROVED | 2026-09-13 | Interface、事务、数据、测试接缝与修改范围；仅批准进入 `to-spec`，尚未批准实施 |

批准原文（对象：本文件及 `design.md`，代码起点 `b09d8e69db9403e891bdc2562d3742487d3cdc23`）：

> 批准 T06-G01 的 design.md、interface.md、SKU 主数据扩围、推荐修改边界和测试接缝；我代表业务、UI/契约、技术中台、课程维护与开发负责人批准该版本进入 to-spec。
