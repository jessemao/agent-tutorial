# Design：同仓库库内移库

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T03-G01 |
| 状态 | APPROVED |
| Spec 路径/版本 | `spec.md` / v1 / APPROVED |
| 代码起点 | `s2-t03-start` / `e27b3d3cf5d87b2ffbf13e6b1b9339a1d7257858` |
| Standards ID/版本 | `STD-WMS-0.7-04` / 当前代码版本 |
| 所有者 | `business-wms`：仓储事业部；`training-server`：课程维护团队；HTTP/UI 契约：UI/契约所有者 |

## 问题与设计目标

- 要解决的能力缺口：当前 `InventoryOperations` 和 `/api/wms/inventory` 没有一个能原子改变两个库位余额的移库接缝。
- 必须保持的行为：现有入库、预占、释放、出库、盘点和余额查询契约不变；可用量与占用量不得为负；库存变更继续统一经过 `InventoryOperations`。
- 非目标：跨仓调拨、占用库存移动、移库单独立状态机、库位停用/删除流程、新数据表、平台契约修改、新 Module 或新依赖。

## 现状与约束

- 相关 Module、类/方法和调用方：`training-server` 课堂 UI 通过 HTTP 调用 `business-wms` 的 `InventoryController`；Controller 只调用 `InventoryOperations`；`InventoryService` 统一协调库位、余额、流水、幂等和审计能力，移库流水通过可注入的 `TransferMovementRecorder` 持久化。
- 现有数据、事务、并发和异常语义：余额以 SKU + 仓库 + 库位唯一；库位和余额 Repository 均支持写锁；流水以操作类型 + 幂等键唯一；`PlatformException` 统一映射为 HTTP 409，Bean Validation/参数错误映射为 400 / `INVALID_REQUEST`。
- 不可修改的边界：`platform-contracts`、`platform-web-starter`、数据库结构、Maven/npm 依赖、Module 依赖方向及现有公开行为。

## 方案

- 责任和依赖方向：
  - 在现有深 Module `InventoryOperations` 增加唯一 `transfer(InventoryTransferCommand)` Interface，返回 `InventoryTransferView`。调用方无需了解锁、幂等、余额创建、流水和审计实现。
  - `InventoryController` 只把经校验的 `InventoryTransferRequest` 转换为领域命令；不访问 Repository，不编排两次库存变更。
  - 已完成任务查询通过 `InventoryOperations.listTransferTasks` 将同一幂等键的 `TRANSFER_OUT`/`TRANSFER_IN` 流水配对为只读视图，不建立移库表或状态机。
  - `training-server` 只增加课堂 UI Adapter 和通过 HTTP Interface 的集成测试，不承载业务判断。
- 数据与状态变化：
  - 从源余额的可用量扣减 `quantity`，目标余额的可用量增加同一数量；两端占用量不变。
  - 目标 SKU 余额不存在时，在目标库位锁保护下以零余额创建；源余额不存在则失败。
  - 成功后写入 `TRANSFER_OUT` 和 `TRANSFER_IN` 两条流水，共用幂等键与移库单号，分别保存源/目标操作后余额快照。
- 事务、幂等、锁顺序与并发：
  1. `transfer` 是单一 Spring 事务入口。
  2. 先按库位 ID 升序锁定两个库位，再校验存在、启用和同仓。
  3. 在库位锁内读取 `TRANSFER_OUT`/`TRANSFER_IN` 幂等流水。两条都存在且载荷匹配时，返回首次快照；只有一条或载荷不同时，返回 `WMS_IDEMPOTENCY_CONFLICT`。
  4. 新请求按库位 ID 升序查询并锁定已存在余额；库位锁同时保护目标空余额的创建。
  5. 校验源可用量与目标数量上界后，修改两端、保存余额、写入双流水，最后调用一次 `AuditRecorder.record("TRANSFER", "INVENTORY", transferNo)`。
  6. 任一异常使数据库事务回滚。同库位在进入事务编排前即被请求/命令校验拒绝。
- 异常与失败后最终状态：复用 `interface.md` 列出的现有错误码。除安全重放返回首次快照外，所有失败都保持两端余额、流水和业务审计次数不变。
- 可观测性与回滚：成功操作通过双流水、两端余额响应、任务列表和单次审计可追溯。功能回滚为删除移库 UI/HTTP/Module Interface 及其实现；不回滚已成功的业务数据，如需反向业务操作必须另行定义。
  - 现有课堂 `AuditPublisher` 是同步日志 Adapter，不是事务性数据库审计表。在不修改平台和数据库的已确认边界下，可保证已知业务失败/安全重放不调用审计，但不能声称日志与数据库提交具有原子回滚。

## 备选方案与取舍

| 方案 | 优点 | 代价/风险 | 结论 |
| --- | --- | --- | --- |
| A. 扩展 `InventoryOperations.transfer` | Interface 只增一个原子能力；锁、余额、幂等、流水与审计保持高本地性；Controller 和测试共用同一 Seam | `InventoryService` 内部编排增加 | 选择；在最小 Interface 后隐藏全部复杂度 |
| B. 新建平行 `InventoryTransferOperations` | 移库表面上独立 | 分裂库存写入 Seam，重复锁/幂等/流水知识，与 Module 规则冲突 | 排除 |
| C. 由 Controller/UI 组合两次单库位操作 | 无需新的领域 Interface | 两次调用无法原子提交，并把锁、回滚和幂等负担推给调用方 | 禁止 |
| D. 新建移库单表与状态机 | 可支持审批、执行与取消等未来流程 | 当前没有状态流转需求，需改数据库并扩大 Interface | 排除；当真实出现异步审批/撤销需求时再增加 |

## 测试接缝

| 设计决定 | 验证方式 | 失败信号 |
| --- | --- | --- |
| 单一原子 Interface | `training-server` 通过 `POST /api/wms/inventory/transfer` 验证响应与两端最终余额 | 接口不存在、任一端数量不符或出现部分成功 |
| 只移可用量 | 预先保留占用量，移库后断言两端占用量不变 | 占用量被迁移或负库存 |
| 幂等快照 | 同键同载荷重放且在两次请求之间改变余额 | 重复改库存、重放返回当前余额或新增流水/审计 |
| 失败原子回滚 | 库存不足、目标溢出和流水写入失败后查询两端余额及记录数 | 任一余额、流水或审计出现部分结果 |
| 固定锁顺序 | 反向并发移库，在 H2 做受控竞争回归，按课程环境补充 MySQL Profile 验证 | 死锁/超时、数量总和或非负不变式破坏 |
| UI Adapter 不承载规则 | 前端生产构建 + 页面通过公开 HTTP Interface 复测成功/失败反馈 | UI 自行改数据、错误语义与后端不一致或页面无法完成移库 |

## 待裁决项与批准

| 事项 | 所有者 | 结论 | 时间 |
| --- | --- | --- | --- |
| Q1—Q9 业务与 HTTP/UI 契约 | 业务所有者、UI/契约所有者 | APPROVED（见 `01_analysis.md` 人工原话） | 2026-09-08 |
| Q10—Q14 事务、锁、实施边界、验收与文档冲突处理 | 开发负责人、UI/契约所有者、架构所有者 | APPROVED（见 `01_analysis.md` 人工原话） | 2026-09-08 |
| 本 `design.md` 的完整设计、审计 Adapter 限制与实施边界 | 开发负责人 | APPROVED；人工原话：“approved” | 2026-09-08 21:48:05 +0800 |
