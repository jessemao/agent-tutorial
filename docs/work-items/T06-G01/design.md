# Design：库存盘点与差异调整

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | `T06-G01` |
| 状态 | `APPROVED` |
| Spec 路径/版本 | `spec.md` v1 / `APPROVED`；本设计只依据已确认的 `01_analysis.md` Q1—Q21 |
| 代码起点 | `b09d8e69db9403e891bdc2562d3742487d3cdc23`（`s2-t06-start` / 分支 `T06-G01`） |
| Standards ID/版本 | `STD-WMS-0.7-06` |
| 所有者 | 仓储事业部；HTTP/UI 契约由 UI/契约所有者；平台能力由技术中台；实施范围由开发负责人批准 |

## 问题与设计目标

- 要解决的能力缺口：当前没有盘点聚合、生命周期、盘点 HTTP/UI、批量库存校准、活动范围冲突控制或盘点查询；现有正数增量命令不能表达物理实盘总量和负差异。
- 必须保持的行为：现有入库、出库、移库和余额查询契约不变；所有库存变化仍只经过 `InventoryOperations`；可用量/占用量不为负；状态、余额、流水与成功审计不部分提交。
- 设计目标：用小而清晰的盘点 Interface 隐藏生命周期、幂等、版本、活动范围、账面快照、差异确认、库存批量锁定和原子调整，使 HTTP Controller、课堂页面和测试通过同一 Seam 使用能力。
- 非目标：完整鉴权/RBAC、附件、物理删除、自动归档、跨仓盘点、库存冻结、反审、通用工作流引擎、通用任意库存调整、Redis/MQ/分布式事务、修改平台 Module。

## 现状与约束

- `InventoryOperations` 是现有库存唯一外部 Seam，Implementation 位于 `InventoryService`；入库、出库、移库均为真实调用方。
- 现有库存写入以库位悲观锁序列化，并用余额锁、库存流水、平台幂等判断和审计完成事务内变化。
- JPA/MySQL/H2 是 **local-substitutable** 依赖；H2 可做快速测试，但唯一约束、锁和隔离行为必须补 MySQL 验证。
- `CurrentOperator`、`AuditRecorder`、`IdempotencyGuard` 是既有平台 Interface，已有生产/测试 Adapter；直接复用且不修改 `platform-contracts` 或 `platform-web-starter`。
- `InventoryOperations` 是既有 in-process Module Interface，不再包一层新的 port；盘点 Module 只作为调用方。
- HTTP Controller 与课堂页面是盘点 Module 的 Adapter，不持有 Repository、不计算差异、不组织事务。
- 代码起点没有 SKU 主数据实体/Repository/业务编码名称来源。已确认的“显示 SKU 编码/名称并验证 SKU”无法凭现状实现；推荐在 `business-wms/masterdata` 增加最小 SKU 主数据，并由 `training-server` 增加演示 SKU。该范围必须由仓储事业部、课程维护团队和开发负责人批准。
- 起点架构文档提前出现但源码不存在的 `adjustFromCount` 不作为既有 Interface；本设计使用经比较后提出的新候选名 `reconcileCount`，仍待批准。

## 方案

### 1. Module、Interface 与 Seam

新增 `business-wms` 内的盘点 Module：

```text
HTTP/UI Adapter
  → InventoryCountOperations Interface
    → InventoryCountService Implementation
      ├─ 盘点聚合、状态、版本、活动范围与动作幂等
      ├─ 主数据、查询投影与审计
      └─ InventoryOperations.reconcileCount(...) Interface
           → InventoryService Implementation
             └─ 稳定锁、最新账面、批量校准、流水与库存审计
```

外部 Interface 采用六个高杠杆入口：

```java
public interface InventoryCountOperations {
    InventoryCountView create(CreateInventoryCount command);
    InventoryCountView saveDraft(Long countId, SaveInventoryCountDraft command);
    InventoryCountView transition(Long countId, InventoryCountTransition command);
    InventoryCountApprovalResult approve(Long countId, ApproveInventoryCount command);
    InventoryCountView get(Long countId);
    InventoryCountPage list(InventoryCountQuery query);
}
```

- `transition` 只容纳形状相近的 `SUBMIT | REOPEN | REJECT | CANCEL`；创建、草稿全量保存和审核因输入、结果与并发协议明显不同而独立。
- 每个写命令包含 `idempotencyKey`；创建之外的动作包含 `expectedVersion`。操作者不由请求体传入，由 Implementation 调用 `CurrentOperator.requiredOperatorId()`。
- `saveDraft` 用完整明细替换而不是 add/update/delete 三套浅入口，隐藏编辑顺序并使重试输入可规范化。
- 每个成功动作返回完整、可刷新恢复的 `InventoryCountView`；页面只替换服务端模型，不在本地拼装领域状态。

库存 Module 的 Interface 只新增一个批量入口：

```java
InventoryCountReconciliation reconcileCount(InventoryCountReconciliationCommand command);
```

它隐藏所有明细的主数据复核、库位/余额稳定锁定、最新总量读取、确认令牌核对、预占保护、溢出校验、缺失零余额创建、非零差异流水和最终余额。禁止暴露 Repository、锁、余额版本或逐行 `adjust` 方法。

### 2. 数据模型

建议新增以下持久化对象，均归 `business-wms` 所有：

| 对象 | 责任 | 关键数据/约束 |
| --- | --- | --- |
| `InventoryCount` | 盘点单聚合根 | `id`、系统生成 `countNo`、`warehouseId`、状态、乐观 `version`、纠错原单、各阶段操作者/时间/原因/备注、最近审核失败分类；`countNo` 唯一 |
| `InventoryCountLine` | 聚合内明细与快照 | `countId`、`skuId`、`locationId`、创建/提交/审核前总量、nullable 实盘量、差异说明、审核差异及调整后可用/占用量；同一单库存维度唯一 |
| `ActiveCountScope` | 并发下强制“一维度一个活动盘点” | `countId + skuId + warehouseId + locationId`；库存维度全局唯一；仅活动状态保留，批准/取消时删除 |
| `CountActionReceipt` | 动作幂等与首次结果 | `actionType + idempotencyKey` 唯一、目标单、规范化请求指纹、结果类型/版本、不可变结果快照；同键异参冲突 |
| `CountApprovalPreview` | 差异变化后的确认事实 | 盘点版本、规范排序后的库存维度与审核前可用/占用/余额版本、opaque token、创建人/时间；新库存变化使其失效 |
| `Sku`（最小主数据候选） | SKU 有效性与业务显示 | `id`、唯一 `code`、`name`、`enabled`；只提供选择/校验所需最小字段，不建立商品中心 |

- `countNo` 推荐由已持久化的数据库 ID 格式化为 `IC-%08d`，系统生成且无时钟/随机碰撞；创建事务内完成并加唯一约束。
- `CountActionReceipt` 的“首次结果快照”是领域视图快照，不引用 HTTP DTO；避免安全重放因后续状态变化返回另一业务结果。
- 审核差异为有符号数 `actualTotal - approvalBookTotal`；现有 `InventoryMovement.quantity` 可承载有符号差异，但必须新增盘点专属构造/匹配逻辑，零差异不写流水。
- 不使用条件唯一索引表示活动状态；显式 `ActiveCountScope` 在 H2/MySQL 上语义一致、冲突对象可追溯。

### 3. 状态与行为

```text
DRAFT ──submit──> SUBMITTED ──approve──> APPROVED
  │                    │
  ├─cancel─────────────┼─cancel────────> CANCELLED
  │                    └─reject────────> REJECTED ──reopen──> DRAFT
  └─saveDraft──> DRAFT
```

- `DRAFT` 允许部分录入和完整替换明细；`SUBMIT` 要求所有实盘量均已填写。
- `SUBMITTED` 只允许审核、驳回或取消；`REJECTED` 只允许重新编辑；`APPROVED`/`CANCELLED` 终态不可变。
- `DRAFT`、`SUBMITTED`、`REJECTED` 保留 `ActiveCountScope`；`APPROVED`、`CANCELLED` 删除范围占用。
- 驳回/取消原因必填；差异说明及其他备注选填；批准后纠错创建新单，可选引用原批准单。
- 所有写动作校验非空当前操作者；审核人不得等于创建人或最后提交人。本次不实现角色授权。

### 4. 审核、事务与并发

审核成功事务的固定顺序：

1. 根据动作类型和幂等键读取/锁定 `CountActionReceipt`；安全重放直接返回首次结果，同键异参冲突。
2. 悲观锁盘点单并核对 `expectedVersion`、状态、操作者分离和主数据。
3. 按 `(locationId, skuId)` 规范排序明细；先按 `locationId` 升序锁定全部库位，再按相同顺序锁定/创建余额，保持与现有库存“库位先于余额”的顺序一致。
4. 读取最新 `available + reserved`。若不同于提交快照且没有有效确认令牌，不改变库存和单据状态，生成最新 `CountApprovalPreview` 并返回 `CONFIRMATION_REQUIRED`。
5. 有令牌时在锁内核对盘点 ID/版本、规范维度及各余额事实；过期则生成新 preview，仍不调整。
6. 校验每条 `actualTotal >= reserved` 且算术安全；任一失败使整单不调整。
7. 对所有非零差异设定 `available = actualTotal - reserved`，保持 `reserved` 不变；每条生成一笔 `COUNT_ADJUSTMENT` 流水。
8. 写入审核前/后快照、将盘点标记为 `APPROVED`、删除活动范围、保存幂等首次结果并记录成功审计；整个步骤在同一事务提交。

盘点聚合先锁、库位后锁不会与现有库存动作形成反向等待，因为现有动作不获取盘点锁；所有库存动作仍以库位锁为共同序列化点。多盘点明细和移库均按库位 ID 升序，余额在库位锁后按 `(locationId, skuId)` 排序。

审核失败记录必须与回滚语义分开：

- 余额、流水、`APPROVED`、范围释放和成功审计属于内层审核事务，任何异常全量回滚。
- 外层盘点 Facade 捕获已分类的可重试失败后，通过独立 `REQUIRES_NEW` 短事务只更新“最近失败分类/时间”，状态保持 `SUBMITTED`，随后重新抛出原业务异常。
- 差异变化不是部分失败：单独短事务持久化 preview/token 与冲突审计，然后返回结构化 `CONFIRMATION_REQUIRED`；不得把失败动作写成已成功幂等结果。
- 不保存内部堆栈或任意异常文本；只保存稳定失败类别，公开错误由 Interface 定义。

### 5. 幂等与版本

- 所有写动作的幂等作用域为 `actionType + idempotencyKey`；请求指纹包含目标 ID、期望版本和规范化业务载荷。
- 同键同指纹返回首次领域结果；同键不同指纹返回 `WMS_IDEMPOTENCY_CONFLICT`。
- 使用新键重复执行已完成状态动作返回 `WMS_COUNT_INVALID_STATE`，不再次改变库存。
- 创建之外均要求 `expectedVersion`；不匹配返回 `WMS_COUNT_VERSION_CONFLICT` 和当前版本。
- 普通连续操作必须使用新键；页面仅在网络重试同一动作时复用原键。
- 确认令牌是 opaque token，绑定盘点 ID、盘点版本、规范维度及锁内读取的余额事实；调用方不得构造或解析。

### 6. 查询、可观测性与回滚

- 列表默认 `createdAt DESC, id DESC`，支持盘点单号、仓库、状态筛选和有上限的分页。
- 详情/动作结果统一返回业务编码/名称、状态版本、所有阶段人员/时间/原因，以及每条创建快照、提交快照、审核前账面、实盘、差异和调整后可用/占用量。
- 每个业务动作记录 `COUNT_CREATE`、`COUNT_SAVE`、`COUNT_SUBMIT`、`COUNT_REOPEN`、`COUNT_REJECT`、`COUNT_CANCEL`、`COUNT_APPROVE` 或 `COUNT_APPROVAL_CONFLICT` 审计；成功审核的单据/余额/流水/成功审计同事务。
- Schema 回滚移除盘点专属表和最小 SKU 表；代码回滚移除盘点调用方和 `reconcileCount`，不迁移或改写现有入出库/移库数据。
- 已经批准的盘点库存变化属于业务事实，不能仅靠回滚代码自动反转；上线回退前必须停止新盘点并由业务所有者决定已发生调整的处置。本课程首版不实现自动补偿。

## 备选方案与取舍

| 方案 | 优点 | 代价/风险 | 结论 |
| --- | --- | --- | --- |
| A：`create / act / find` 三入口 | Interface 最小，生命周期和查询复杂度高度隐藏 | Java 8 中依赖运行时 Action/Query 分派；调用方和文档较难发现合法动作 | 不选；作为最小化对照 |
| B：`apply(CountIntent) / query(CountQuery)` 两入口 | 新动作可增加类型而不扩方法；统一版本/幂等管线 | 容易演变成命令总线；Java 8 无 sealed exhaustiveness；普通调用方负担大 | 不选；扩展性超过当前真实变化需求 |
| C：每个动作一个方法 | HTTP 映射直观、类型明确 | 七个以上入口重复版本/幂等约定，状态动作形成浅接口 | 不选 |
| D：六入口混合设计 | create/save/approve 的特殊形状显式；相近状态动作合并；查询清晰；页面调用简单 | Interface 比 A/B 大，需要维护 transition action enum | **推荐**；在深度、可发现性与调用方成本间最佳 |
| 库存按明细逐条调整 | 单个方法简单 | 盘点 Module 被迫循环、锁和组织事务，可出现部分审批 | 排除 |
| `InventoryOperations.reconcileCount` 批量校准 | 一次调用隐藏锁、预占保护、差异、流水和原子性，删除后复杂度会散回调用方 | 扩展既有公开 Interface，必须做现有调用方回归 | **推荐** |
| 条件唯一索引控制活动盘点 | 表较少 | H2/MySQL 表达差异大，冲突对象难返回 | 排除 |
| `ActiveCountScope` 唯一占用 | 跨数据库语义清晰，可定位冲突单 | 多一个表和释放逻辑 | **推荐** |
| 修改平台统一失败响应以携带数据 | 所有接口统一 | 扩大平台契约和全部调用方影响 | 排除 |
| 盘点端点使用业务本地结构化冲突响应 | 不改平台，能返回最新差异/token | 新端点需明确其错误 envelope | **推荐**，待契约批准 |

## 测试接缝

| 设计决定 | 验证方式 | 失败信号 |
| --- | --- | --- |
| 盘点聚合状态与全量草稿保存 | 通过 `InventoryCountOperations` 或公开 HTTP 执行完整状态路径 | 非法状态被接受、零与未填写混淆、终态可修改 |
| 活动范围唯一 | H2 功能测试 + MySQL 两事务并发创建相交明细 | 两单均成功或出现部分明细 |
| 审核最新差异确认 | 提交后通过现有库存公开入口改变余额，再审核与携 token 重试 | 首次直接覆盖、陈旧 token 成功、调用方需传内部版本 |
| 预占保护 | 构造 `reserved > actualTotal` 的公开业务状态后审核 | 可用量为负、占用量被静默改变 |
| 多明细原子调整 | 第二条故意触发失效/溢出/失败注入 | 第一条余额/流水或单据状态已提交 |
| 幂等与版本 | 所有写动作覆盖同键同参、同键异参、新键重复和陈旧版本 | 重复改变状态/库存或冲突未被识别 |
| 自审隔离 | 创建/提交/审核使用不同或相同 `X-Operator` | 缺失身份或自审成功 |
| 零差异与缺失余额 | 有效维度无余额、零实盘、无差异审核 | 无法建零余额或生成零变化流水 |
| 锁顺序与跨业务并发 | MySQL 并发运行盘点审核与入库/出库/移库 | 死锁未受控、丢更新或不变式破坏 |
| HTTP/UI 完整旅程 | 页面/MockMvc 覆盖列表、新建、保存、提交、冲突确认、审核、驳回、取消与刷新 | 内部 ID 暴露、失败丢输入、刷新不可恢复 |
| 现有行为保持 | `business-wms`、`training-server` 回归及 `mvn clean verify` | 既有入出库/移库/API 行为变化 |

## 待裁决项与批准

| 事项 | 所有者 | 结论 | 时间 |
| --- | --- | --- | --- |
| 采用六入口 `InventoryCountOperations` 及完整领域视图 | 仓储事业部、UI/契约所有者、开发负责人 | APPROVED | 2026-09-13 |
| 新增批量 `InventoryOperations.reconcileCount` Interface | 仓储事业部、开发负责人 | APPROVED | 2026-09-13 |
| 盘点表、活动范围表、动作回执、确认快照的数据方案 | 仓储事业部、开发负责人 | APPROVED | 2026-09-13 |
| 审核事务、稳定锁顺序及独立失败记录事务 | 仓储事业部、开发负责人 | APPROVED | 2026-09-13 |
| 在 `business-wms` 增加最小 SKU 主数据、在 `training-server` 增加演示数据 | 仓储事业部、课程维护团队、开发负责人 | APPROVED | 2026-09-13 |
| 盘点专属结构化 HTTP 冲突响应且平台 Module 保持只读 | UI/契约所有者、技术中台、开发负责人 | APPROVED | 2026-09-13 |
| 允许修改/禁止修改范围与测试接缝 | 开发负责人 | APPROVED | 2026-09-13 |

批准原文（对象：本文件及 `interface.md`，代码起点 `b09d8e69db9403e891bdc2562d3742487d3cdc23`）：

> 批准 T06-G01 的 design.md、interface.md、SKU 主数据扩围、推荐修改边界和测试接缝；我代表业务、UI/契约、技术中台、课程维护与开发负责人批准该版本进入 to-spec。
