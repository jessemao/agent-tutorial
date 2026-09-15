# 最终交付绑定（历史重写后）

- 最终候选：`T06-G01` / `85236d0`；开始基线：`s2-t06-start` / `c8613f4`。
- 本文件中较早的提交号均为历史证据；对应结论已由最终候选重新绑定。
- 最终 Spec/Standards 双轴结论：`PASS`；功能测试与 QA 为按企业流程记录的演练证据，不以课堂签署作为门禁。

# 03 Review：T06-G01

## 最终候选（2026-09-14）

- 被审提交：`051a023 refactor: split inventory count workflows`；固定起点：`9bc424c`（历史重写后的对应提交）。
- 变更完成 `InventoryCountService` 的真实职责拆分：草稿/提交、状态审核、查询投影各自拥有行为；删除 `CountWorkflowDependencies` 机械字段袋。
- 公共入口保留事务、幂等、版本和错误契约；新增损坏回执快照稳定失败回归；命令支持组件已收窄为共享命令行为，不再透传查询职责。
- 自动验证：`classroom-verify.sh T06` PASS；69 项测试零失败、4 项条件跳过；前端构建与 clean verify PASS。

## 最终独立复评结论（2026-09-14）

- Spec 轴：PASS，无未关闭 finding；既有 AC/DEC 与错误、幂等、并发契约保持不变。
- Standards 轴：PASS，无未关闭 finding；`STD-02` 已关闭，`CountCommandSupport` 不再是机械依赖袋，构造器依赖均不超过三项。
- 综合建议：PASS（开发 Review 完成）；仍按流程转入独立功能测试、QA 与业务 Decision，不把开发审查当作业务验收。

## 第三轮 REWORK 修复待复评

- 目标：关闭 `SPEC-01` 与 `SPEC-06` 的权威并发路径。
- 实现：创建按库位 ID 稳定加锁，并在获得锁后重新检查 `CREATE` 回执；同键同参安全重放，不同键重叠范围返回结构化冲突。
- 新增 MySQL 并发同键创建用例；完整 T06 验证 PASS。
- Standards 架构项 `STD-02`、`SMELL-01`～`03` 未在此功能修复中宣告关闭，仍需实质性职责拆分。

## 第二轮 REWORK 复评：候选 `9268885`

- 修复范围：`640a4fd...9268885`；提交为 `9268885 fix: close remaining T06 review gaps`。
- 自动验证：`./scripts/classroom-verify.sh T06` PASS；68 项测试零失败、4 项条件跳过，前端构建、clean verify 与 MySQL 接缝通过。
- Spec 轴：`SPEC-02` 的顺序/跨状态首次结果已关闭；`SPEC-01` 仍 OPEN，并新增并发同键同参创建的 AC-15 缺口。
- Standards 轴：`STD-01` 已关闭；`STD-02` 与 `SMELL-01`～`03` 仍 OPEN。
- 综合建议仍为 `REWORK`。

### 第二轮复评明细

| Finding | 结论 | 证据 / 后续要求 |
| --- | --- | --- |
| SPEC-01 | OPEN | 普通创建/保存预检查已有结构化 current/lineIndexes，但数据库唯一约束竞态仍只返回 code/message；需让权威竞态路径也能定位冲突单与明细 |
| SPEC-02 | RESOLVED | CREATE 已写入动作回执不可变快照；跨状态重放测试验证仍返回首次 `DRAFT/version=1` |
| SPEC-06 | OPEN / P1 | 并发同键同参创建可能双双越过初查，失败方被映射为幂等冲突；需在锁内重查并安全重放首次结果，增加 MySQL 并发测试 |
| STD-01 | RESOLVED | 未知约束 `uk_count_no` 回归测试证明异常原样上抛，不再伪装为范围冲突 |
| STD-02 | OPEN | 新增的依赖类只暴露字段，属于机械依赖袋，未移动行为，违反禁止用透传 Helper 掩盖耦合的规则 |
| SMELL-01 | OPEN | `InventoryCountService` 仍同时承担创建、查询、草稿、提交、迁移、审核、幂等、映射与审计 |
| SMELL-02 | OPEN（部分改善） | REJECT/REOPEN/CANCEL 已类型化；CREATE/SAVE/SUBMIT/APPROVE 仍用字符串表达同一动作概念 |
| SMELL-03 | OPEN | 关键事务、校验、异常与持久化路径仍有大量密集单行代码 |

### 下一轮修复边界

- 将 `InventoryCountService` 变为最多依赖三个真实应用服务的薄入口；分别把草稿/提交、状态迁移、审核协调与查询投影行为移动到有行为的组件，删除纯依赖袋。
- 用统一 `CountAction` 覆盖全部动作及回执/审计键。
- 在稳定锁内执行创建幂等二次检查，并为同键同参、同键异参和不同键范围冲突补 MySQL 并发断言。
- 展开关键单行逻辑并按业务意图命名；补损坏快照的稳定失败测试。

## REWORK 复评：候选 `640a4fd`

- 复评范围：`83c8753661c0630f6ce719c43010ef874896b163...640a4fd`。
- 修复提交：`640a4fd fix: resolve T06 review findings`。
- 自动验证：`./scripts/classroom-verify.sh T06` PASS；67 项测试零失败、4 项条件跳过，前端生产构建与 clean verify 均通过。
- Spec 独立复评：`SPEC-03`、`SPEC-04`、`SPEC-05` 的实现缺口已关闭；`SPEC-01`、`SPEC-02` 仍 OPEN。
- Standards 独立复评：仍不通过；`STD-01` 缺缺陷邻域回归测试，`STD-02` 未实质拆分职责，`SMELL-01`～`03` 仍存在。
- 综合建议：`REWORK`；不得进入独立功能测试、QA 或 Decision。

### 复评 Finding 状态

| Finding | 复评证据 | 结论 |
| --- | --- | --- |
| SPEC-01 | 创建预检查已返回 current/lineIndexes；保存草稿冲突仍抛普通异常，MySQL 唯一约束竞态仍无结构化 data | OPEN |
| SPEC-02 | 动作回执已有不可变快照；创建接口重放仍从当前聚合重建视图，且无跨状态重放测试 | OPEN |
| SPEC-03 | 页面已接入单号、仓库、状态筛选和受限分页 | RESOLVED（浏览器级自动化证据仍缺） |
| SPEC-04 | 差异确认响应及页面改用 SKU/库位业务 code/name | RESOLVED |
| SPEC-05 | 实现与测试统一为 `WMS_COUNT_INVALID_STATE` | RESOLVED |
| STD-01 | 未知数据库约束已重新抛出，不再伪装；但未增加 handler 未知约束回退测试 | OPEN |
| STD-02 | 12 参数构造器改为 12 个字段注入，只规避参数计数，未拆分职责/依赖簇 | OPEN |
| SMELL-01～03 | 服务职责发散、动作裸字符串分派、密集单行代码仍存在 | OPEN |

### 复评新增测试阻塞

- 增加创建/动作在后续状态变化后的首次结果重放测试，以及损坏快照的明确失败测试。
- 增加未知数据库约束不得映射为范围冲突的异常处理器测试。
- 增加保存草稿范围冲突与 MySQL 唯一约束竞态的结构化响应断言。
- 为 AC-20 增加可执行的浏览器/页面旅程证据，而不只依赖 TypeScript 构建。

## 被审对象

- 唯一起点：`b09d8e69db9403e891bdc2562d3742487d3cdc23`。
- 候选提交：`83c8753661c0630f6ce719c43010ef874896b163`；merge-base 与起点一致。
- 三点 Diff：`git diff b09d8e69db9403e891bdc2562d3742487d3cdc23...83c8753661c0630f6ce719c43010ef874896b163`。
- 提交列表：`83c8753 feat: complete T06 inventory counting workflow`。
- 实施依据：`01_analysis.md`、`spec.md` v1、`design.md`、`interface.md`、Tickets 01—05、`02_verification.md`。
- Review 方法：Spec 与 Standards 两条独立审查轴并行执行，主审复核证据后合并；未查看教师卡。
- Skills：项目 `work-item-review` v`T05-G01-v1.1`（source commit `d76cfb2`）；本地 `code-review` Skill（当前工作站版本）。
- Standards：`STD-WMS-0.7-06`，并应用 Architecture、Requirements & Design、Clean Code、Testing、Documentation、AI Security 与 Fowler 判断性气味基线。

## Spec 符合性矩阵

| Spec / Interface 条目 | 实现与证据 | 结论 |
| --- | --- | --- |
| AC-01、AC-03—AC-14、AC-17—AC-19 | 盘点聚合、状态迁移、审核校准、审计及对应 H2/MySQL 验证 | PASS（不覆盖下列明确缺口） |
| AC-02；Interface 错误契约 | 活动范围冲突仅返回通用 code/message，未携带冲突单号与维度 | FAIL：SPEC-01 |
| AC-15；DEC-17/18；Design 首次结果快照 | `CountActionReceipt` 只保存结果版本；重放重新读取当前盘点视图 | FAIL：SPEC-02 |
| AC-16、AC-20；Interface 页面契约 | 后端支持筛选分页；页面固定无参加载第一页、关闭分页且无筛选控件 | FAIL：SPEC-03 |
| Interface 业务值展示 | 普通详情使用 code/name；差异确认响应和页面直接展示 SKU/库位内部 ID | FAIL：SPEC-04 |
| Interface 稳定错误码 | 契约为 `WMS_COUNT_INVALID_STATE`，实现和测试固化为 `WMS_COUNT_INVALID_STATUS` | FAIL：SPEC-05 |

## Standards 符合性矩阵

| 规则 | 证据 | 例外 | 结论 |
| --- | --- | --- | --- |
| STD-ERR-01；异常不得伪装 | Count 专用异常处理器把除创建幂等约束外的所有数据库完整性异常映射为范围冲突 | 无 | FAIL：STD-01 |
| Clean Functions F1：函数最多 3 个参数 | `InventoryCountService` 构造器含 12 个依赖参数 | 无已登记 Java 例外 | FAIL：STD-02 |
| Architecture / Module ownership | 变更集中于 `business-wms`、`training-server` 与批准的 SKU 主数据扩围，未改平台契约模块 | 不适用 | PASS |
| Testing / Documentation / AI Security | 已有 H2、MySQL、前端构建及课程门禁证据；教师材料未读取 | 版本证据问题见未覆盖风险 | PASS（有风险项） |
| Fowler 判断性气味基线 | 服务同时承载生命周期、查询、幂等、主数据、审核协调和证据记录；动作以裸字符串分派；大量单行逻辑 | 判断性气味，不自动等同缺陷 | REVIEW：SMELL-01～03 |

## Findings

| ID | 轴 | 级别 | 位置 | 问题与影响 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| SPEC-01 | Spec | P1 | `spec.md:101`；`interface.md:115`；`InventoryCountService.java:49`；`InventoryCountExceptionHandler.java:10-15` | 活动范围冲突未返回批准契约要求的冲突单号和明细，调用方无法定位并打开冲突单 | 为显式范围冲突和唯一约束竞态返回同一结构化冲突对象，并补充字段断言 | OPEN |
| SPEC-02 | Spec | P1 | `spec.md:114`；`design.md:84,89,113`；`CountActionReceipt.java:8-13`；`InventoryCountService.java:172` | 回执没有不可变结果快照，安全重放会读取盘点单当前状态；后续状态变化后不再返回首次结果 | 持久化领域结果快照或足以重建首次结果的不可变字段，并测试“首次成功后继续迁移再重放” | OPEN |
| SPEC-03 | Spec | P1 | `interface.md:22,138`；`spec.md:115`；`CountPage.tsx:12,25-26` | 课堂页面没有单号、仓库、状态筛选和分页，完整页面旅程未达到批准范围 | 接入已有查询参数与受限分页，增加页面级旅程验证 | OPEN |
| SPEC-04 | Spec | P1 | `interface.md:18,23,95,139`；`CountApprovalLineView.java:4-6`；`CountPage.tsx:30` | 差异确认只提供并显示 `skuId/locationId`，泄露内部 ID 且用户无法按业务编码/名称确认差异 | 响应补齐 SKU/库位 code/name，页面只展示业务值 | OPEN |
| SPEC-05 | Spec | P2 | `interface.md:113`；`InventoryCountService.java:142,166-167` | 非法状态错误码实现为 `WMS_COUNT_INVALID_STATUS`，与批准的 `WMS_COUNT_INVALID_STATE` 不一致 | 统一实现、测试和文档到已批准稳定错误码 | OPEN |
| STD-01 | Standards | BLOCKER | `InventoryCountExceptionHandler.java:10-15` | 除 `uk_count_idempotency` 外的数据库完整性错误一律伪装为范围冲突；本次新增表还含多个非范围唯一约束，诊断和公开错误语义会失真 | 只映射可明确识别的批准约束，其余交由通用错误处理；逐约束测试 | OPEN |
| STD-02 | Standards | BLOCKER | `InventoryCountService.java:28-30` | 构造器 12 个参数，直接违反 Clean Functions F1，且暴露服务职责发散 | 按真实变化原因拆分职责/依赖簇；避免新增纯透传层 | OPEN |
| SMELL-01 | Standards | REVIEW | `InventoryCountService.java:34-193` | possible Divergent Change：一个服务同时承担创建、查询、编辑、迁移、审核、幂等和主数据组装 | 修复阻塞项时评估按生命周期/查询/审核协调的真实边界拆分 | OPEN |
| SMELL-02 | Standards | REVIEW | `InventoryCountService.java:119-134` | possible Primitive Obsession / Repeated Switches：动作以裸字符串分派并传播到幂等与审计 | 用领域动作类型统一表达并集中解析 | OPEN |
| SMELL-03 | Standards | REVIEW | `InventoryCountService.java:170-193` | possible Obscured Intent：校验、查询、映射被压缩为密集单行，异常路径难审查 | 展开关键路径并按意图命名，不改变行为地重排 | OPEN |

## 问题复盘与防复发

- 直接原因：验证主要覆盖立即重放和后端 API，未覆盖“状态变化后的首次结果”、结构化错误负载及完整页面交互；实现阶段把稳定契约名称和 UI 业务值要求弱化成了可运行的最小形态。
- 为什么既有验证未发现：相关测试只断言错误码或即时状态；前端验证以 TypeScript 构建为主，没有可执行的筛选、分页、差异确认旅程断言；数据库异常测试未枚举新增约束。
- 防复发：把 Interface 的字段级错误契约、业务值展示、页面筛选分页以及跨状态幂等重放直接转成契约测试；数据库约束映射使用白名单并覆盖未知约束回退。

## 未覆盖风险

- `02_verification.md` 顶部仍以 Ticket 01 未提交工作树描述版本，虽后续追加 Tickets 02—05 结果，但没有把完整证据统一绑定到最终候选 `83c8753`；复评时应同步版本证据。
- AC-20 的“完整页面旅程”目前只有构建与人工开发验收记录，没有浏览器级自动化证据。
- 本候选同时包含用户另行授权的无 `rg` 门禁兼容修复；其目的明确且验证通过，但不属于五张业务 Ticket 的原始逐票范围，复评时应在版本范围中继续显式说明。

## 阶段状态与 Agent 建议

- 综合建议：`REWORK`。
- 独立功能测试：尚未开始；不得用本 Review 替代。
- QA：尚未开始，且必须由不同于测试工程师的角色执行。
- 本 Review 未裁决或关闭任何 Finding，未生成 Decision，未推送、合并、发布或打标签。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 状态 | 下一动作 |
| --- | --- | --- | --- |
| SPEC-01～05、STD-01～02、SMELL-01～03 / `83c8753` | 开发负责人、业务/UI/契约所有者、技术中台 | PENDING | 人工裁决后进入修复；修复完成需形成新候选并执行独立双轴复评 |
| T06-G01 功能测试与 QA | 测试工程师、独立 QA | BLOCKED | Findings 关闭并复评通过后再分别进入对应阶段 |
| T06-G01 最终决定 | 业务验收人、交付负责人 | BLOCKED | Review、功能测试、QA 均具备结论后才能进入 Decision |
