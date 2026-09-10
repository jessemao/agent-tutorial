# Spec：统一库存业务修改入口

## 元数据

| 字段 | 内容 |
| --- | --- |
| Spec 版本 | v1 |
| 状态 | `APPROVED` |
| 代码起点 | `s2-t04-start` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| Standards ID/版本 | `STD-WMS-0.7-06` / 代码起点版本 |
| 业务/规则所有者 | 当前用户代理；本任务不改变业务规则 |
| 开发负责人 | 当前用户代理；已批准 Design/Interface，待批准本 Spec 的最终实施范围 |
| UI/契约所有者（涉及变更时） | 当前用户代理；批准 HTTP/UI 契约完全保持 |
| 测试工程师 | 待指定，不得与 QA 合并 |
| QA 人员 | 待指定，须与测试工程师不同团队/人员 |
| 业务验收人 | 待指定 |

## 问题与目标

- 当前问题：库存 Module 同时暴露 `InventoryOperations` 与 `InventoryTransferOperations` 两个公开 Interface；第二 Interface 只有一个纯透传 Adapter，而真实移库 Implementation 已位于 `InventoryService`。调用方必须记忆移库例外，模块规则、架构文档和代码事实不能统一表达库存入口。
- 目标结果：以现有 `InventoryOperations` 作为唯一公开库存业务 Interface，将已有的移库写操作和移库任务列表查询纳入该 Interface，迁移调用方并删除第二 Interface 与纯透传 Adapter。
- 成功指标：统一后只有一个公开库存业务 Seam；全部外部行为、数据与事务语义和起点一致；旧类型及调用零残留；同一保护测试、Module 回归、前端构建和全量验证通过。

## 范围与非目标

### 范围

- 在 `InventoryOperations` 增加具名 `transfer(InventoryTransferCommand)` 与 `listTransferTasks()` 方法。
- 让现有 `InventoryService` 通过统一 Interface 暴露已有同名 Implementation，不移动或改写其业务算法。
- 将 `InventoryController`、`T03TransferContractIntegrationTest` 和 `InventoryConcurrencyTest` 的第二 Interface 依赖迁移到 `InventoryOperations`。
- 搜索确认零旧调用后删除 `InventoryTransferOperations` 与 `LegacyInventoryTransferService`。
- 同步 `docs/architecture.md`，使其准确描述完成后的单一库存业务 Interface。
- 按 Expand、Migrate、Contract 分票实施，每票保持同一行为测试通过并可独立回退。

### 非目标

- 改变 HTTP 路径、请求/响应字段、状态码、错误码、错误优先级、UI 字段或操作流程。
- 改变收货、预占、释放、出库、取消、盘点、余额查询或移库业务规则。
- 改变移库事务边界、锁顺序、幂等、双流水、审计、任务投影或失败最终状态。
- 新增查询 Interface、通用命令总线、万能 Command、公共框架、Adapter、Module 或依赖。
- 修改 `platform-*`、数据库结构、Repository、Maven/npm 依赖或跨 Module 依赖。
- 顺手清理 `InventoryService` 内部算法、命令类型或任务列表实现。

## 业务规则与设计决定

| ID | 决定 | 理由 | 所有者 |
| --- | --- | --- | --- |
| DEC-01 | 选择候选 A：消除移库写操作的第二公开 Seam 与纯透传 Adapter | 第二 Interface 只有一个生产调用方和一个无规则的透传 Adapter，无法通过 deletion test | 当前用户代理决策角色 |
| DEC-02 | `transfer` 与 `listTransferTasks` 一并纳入 `InventoryOperations` | 人工明确同意查询归属；避免新建只有一个 Adapter/调用方的假设 Seam | 当前用户代理决策角色 |
| DEC-03 | 采用具名方法，不使用 `execute/change` 通用命令 | 保持 Java 类型、领域动作可发现性和最小调用方 Diff，避免把复杂性转移到指令/结果包装 | 当前用户代理开发负责人 |
| DEC-04 | `InventoryService` 保持唯一生产 Adapter，现有移库 Implementation 不搬迁 | 真实事务、锁、幂等、余额、流水和审计已集中，重构只删除浅间接层 | 当前用户代理开发负责人 |
| DEC-05 | 按 Expand → Migrate → Contract 实施 | 允许每阶段独立验证和逆序回退，避免先删除旧入口造成大 Diff | 当前用户代理开发负责人 |
| DEC-06 | 外部行为、数据、事务、平台和依赖全部保持 | 本任务是结构重构，不是需求或契约调整 | 相关所有者待最终确认 |

## 用户旅程与交互决定

- 进入与前置数据：沿用起点的课堂首页、入库、出库、库存和移库页面及现有数据。
- 一次操作的完整步骤：所有页面操作、HTTP 请求和结果查看步骤保持起点行为；本任务不新增用户步骤。
- 字段显示、可编辑/只读/可选择状态及允许值：全部保持，不因 Module Interface 迁移而改变。
- 业务文字与内部标识映射：库位文字与内部 ID 的现有映射保持。
- 系统生成标识、连续操作、重试与冲突：移库单号、幂等键生成、安全重放和冲突拒绝保持。
- 成功、失败及上一次有效结果的页面行为：成功详情、服务端错误、余额与既有结果的保留行为保持。
- 列表、查询、筛选、刷新或重新进入后的可见性：移库任务列表及库存查询的内容、排序、筛选和刷新恢复保持。
- 所依赖通用能力的归属：业务和 UI 能力属于代码起点；统一 Module Interface 属于本任务；平台、数据库和新通用能力不在范围内。

## 接口、数据与事务

- API/UI 契约：`POST /api/wms/inventory/transfer`、`GET /api/wms/inventory/transfers` 及其他现有 API/UI 全部保持。内部 Module Interface 按已批准 `interface.md` 增加两个具名方法。
- 数据与状态：无表、实体、字段、状态或数据迁移；任务列表继续从既有双流水只读投影。
- 事务、幂等和并发：`InventoryService.transfer` 保持 Spring 事务入口；按库位 ID 升序锁定，原子更新两端余额，按现有顺序检查幂等并写双流水和一次审计。安全重放、冲突及失败回滚保持。
- Module 与所有权：`business-wms` 仓储事业部拥有库存 Interface 与 Implementation；`training-server` 仅保留公开行为测试；平台 Module 只被依赖、不修改。

## 验收条件

| ID | Given | When | Then | 开发 TDD 接缝 | 独立功能测试方法 / 负责人 | QA 追溯证据 | 业务验收范围 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AC-01 | 起点存在两个库存公开 Interface | 完成 Contract 阶段 | `InventoryOperations` 包含 `transfer` 与 `listTransferTasks`；旧 Interface、Adapter 和调用方搜索为零 | 编译、Bean 注入检查、调用方搜索 / 开发 | 固定候选版本通过公开 Interface 执行移库与列表查询 / 测试工程师待指定 | 核对 Spec、Diff、零引用证据和版本一致性 / QA 待指定 | 不检查内部类名，仅确认业务入口可用 |
| AC-02 | 起点的入库、预占、释放、出库、取消、盘点与余额查询可用 | 重构后运行同一保护测试 | 返回、错误、状态、余额、流水和审计与起点一致 | Inventory Regression、WMS Flow、Module/全量回归 / 开发 | 使用独立数据复测既有功能与异常 / 测试工程师待指定 | 追溯保持项、测试覆盖和原始证据 / QA 待指定 | 正常业务流程无变化 |
| AC-03 | 起点支持正常及全量移库 | 经统一 Interface 或现有 HTTP 执行 | 源减、目标增、占用不变、总量守恒，返回相同两端快照 | T03 Happy Path / 开发 | 页面和 API 独立执行正常/全量移库 / 测试工程师待指定 | 核对前后映射和测试版本 / QA 待指定 | 正常移库结果不变 |
| AC-04 | 起点定义非法请求、库位、源余额、不足与溢出错误 | 经统一 Interface 执行失败场景 | 错误码、HTTP 状态、优先级和最终数据均保持 | T03 Contract/Atomicity / 开发 | 独立覆盖各失败输入并核对余额、流水 / 测试工程师待指定 | 核对错误矩阵、原始响应和缺陷重测 / QA 待指定 | 只抽查可理解的页面失败反馈 |
| AC-05 | 已存在首次移库或冲突/单边流水场景 | 重放同载荷或提交冲突载荷 | 首次快照、安全重放、冲突拒绝、记录次数和数据保持 | T03 Contract/Atomicity / 开发 | API 独立验证重放与冲突 / 测试工程师待指定 | 核对证据真实性和版本 / QA 待指定 | 不要求业务人员手工构造幂等键 |
| AC-06 | 两端有可移动库存 | 并发执行反向移库 | 无死锁/超时泄漏，余额非负且总量守恒 | `InventoryConcurrencyTest`，必要时 MySQL Profile / 开发 | 在批准环境执行并发专项 / 测试工程师待指定 | 核对环境、隔离级别、命令与结果 / QA 待指定 | 不要求业务人员执行并发测试 |
| AC-07 | 已存在成功移库流水 | 查询、刷新或重新进入任务列表 | 列表字段、内容、排序和恢复行为不变，不新增任务表 | `GET /transfers` 与列表断言 / 开发 | 页面/API 查询与刷新复测 / 测试工程师待指定 | 核对列表证据与数据来源 / QA 待指定 | 任务列表可见且可刷新 |
| AC-08 | 每个迁移 Ticket 均从固定起点执行 | 完成 Expand、Migrate、Contract | 每步同一保护测试全绿、范围受控、可逆；最终前端构建、健康检查和 `mvn clean verify` 通过 | T04 baseline、Module、前端、全量 / 开发 | 在固定候选版本执行需求级回归 / 测试工程师待指定 | 核对阶段证据、Review Findings 和例外 / QA 待指定 | 最终业务流程整体可用 |

## 风险与回滚

- 风险：统一 Interface 后可能出现 Spring Bean 注入歧义或事务代理路径变化；迁移遗漏直接测试调用方；错误删除任务查询；为追求统一而改写业务算法；文档与完成后代码再次漂移。
- 回滚方式：每个 Ticket 固定提交并逆序回退 Contract、Migrate、Expand；恢复旧 Interface/Adapter 和调用方即可。由于不修改数据库、Implementation 或外部契约，无数据迁移和数据回滚。

## 审批记录

| 角色 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| 候选与查询归属决策 | 当前用户代理 | APPROVED | 2026-09-10 | 人工原话：“1. 选A”“2. 同意纳入” |
| Design/Interface | 当前用户代理开发负责人 | APPROVED | 2026-09-10 | 人工原话：“批准”；允许形成本 Spec，不代表本 Spec 已获实施批准 |
| Spec 与最终实施范围 | 当前用户代理开发负责人及 UI/契约所有者 | APPROVED | 2026-09-10 | 人工原话：“批准。/to-tickets T04-G01”；批准 v1 AC、可改/禁改范围、保持契约和进入 Tickets |

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| `spec.md` v1 的 AC、范围及 `ae996fe5` 实施边界 | 开发负责人 | 当前用户代理 | CONFIRMED | APPROVED；2026-09-10；人工原话：“批准。/to-tickets T04-G01” |
| HTTP/UI 保持项，无契约变化 | UI/契约所有者 | 当前用户代理 | CONFIRMED | APPROVED；2026-09-10；仅批准完全保持，不授权契约变化 |
| 独立功能测试计划和固定候选版本 | 测试工程师 | 待指定 | PENDING | 代码评审后执行 |
| QA 审核对象与团队独立性 | QA 人员 | 待指定 | PENDING | 独立测试后执行 |
| 业务验收对象与剩余风险 | 业务所有者 | 待指定 | PENDING | QA 审核后执行 |

仅记录本阶段实际证据及有权人员的原文结论；开发验证、独立测试、QA 和业务结论不得互相代替。
