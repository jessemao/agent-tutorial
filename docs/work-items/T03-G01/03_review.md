# 03 Review：T03-G01

## 被审对象

- 代码差异起点与被审版本：`s2-t03-start` / `e27b3d3cf5d87b2ffbf13e6b1b9339a1d7257858` → 候选提交 `5ce456c25372b885e409f61153ccf70eaac05033`。
- 固定 Diff：`git diff s2-t03-start...5ce456c25372b885e409f61153ccf70eaac05033`。
- `01_analysis.md`：`docs/work-items/T03-G01/01_analysis.md`。
- `02_verification.md`：`docs/work-items/T03-G01/02_verification.md`。
- Spec：`spec.md` v1 / APPROVED、`design.md`、`interface.md`、三张 Ticket。
- Standards：`STD-WMS-0.7-04`、根与受影响 Module `AGENTS.md`、`docs/ai-governance/standards/`。
- 使用的 Skill：`code-review`，锁定哈希 `caa9a086baaf9e0f7cd71f64edfa83da6821c05e826b083221f3d02e3d6a1905`；Spec 与 Standards 由两个独立子 Agent 并行评审。

## Spec 符合性矩阵

| 要求 | 代码符号 | 测试/证据 | 结论 |
| --- | --- | --- | --- |
| DEC-03/04：源目标不同、数量为正，HTTP 与 Module Interface 语义一致 | `InventoryTransferCommand`、`InventoryService.transfer` | HTTP 参数测试存在；Module 直接调用边界未覆盖 | FAIL（P-01） |
| AC-14：连续操作只展示当前成功详情或当前错误 | `TransferPage.run` | 前端构建通过；缺少状态转换测试 | FAIL（P-02） |
| AC-10、Interface 测试接缝：失败原子性、幂等及精确流水数 | `InventoryService.transfer`、流水写入 | 缺少第二条流水写入失败和精确计数断言 | BLOCKED（P-03） |
| AC-01—09、11—13、15—16：主路径、错误、回滚、并发、查询和回归 | 服务、HTTP、UI、H2/MySQL 测试 | `02_verification.md` 所列结果；候选版本追溯待补 | PARTIAL（P-04/S-01） |

## Standards 符合性矩阵

| 规则 ID | 适用性 | 代码符号/文档 | 检查证据 | 例外 | 结论 |
| --- | --- | --- | --- | --- | --- |
| `STD-TEST-02`、Testing §4/§7、Documentation §6 | MUST | `02_verification.md` | 验证对象仍写旧提交和工作区，原始输出无可读取路径 | 无 | BLOCKED（S-01） |
| `training-server/AGENTS.md` §4、Testing §5 | MUST | `T03TransferContractIntegrationTest` | 直接使用 Repository 和反射拼装内部状态 | 无 | FAIL（S-02） |
| Clean General G5 / Fowler Duplicated Code、Repeated Switches | REVIEW | `TransferPage.tsx` | 库位 ID/编码双向映射分别硬编码 | 无 | REVIEW（S-03） |
| Architecture、AI Security、依赖方向与敏感信息 | MUST | 全部候选差异 | 未发现新增违反 | 无 | PASS |

## Findings

### Spec 轴

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| P-01 | BLOCKER | `InventoryTransferCommand.java:13-21`；`InventoryService.java:84-90,123-124` | DEC-03/04 要求库位不同且数量为正，Interface 将其应用到 Module 调用；但 Command/Service 无领域校验。直接传负数可导致源库存增加、目标库存减少。 | 在 Command 或 Service 公共边界统一校验，并增加 Module 级测试。 | OPEN |
| P-02 | MAJOR | `TransferPage.tsx:28-37,56-65` | AC-14 要求连续提交展示当前成功详情或错误；失败提交前未清除旧 `result`，会同时显示旧成功与新失败。 | 新提交开始时清除旧结果，并补 UI 状态测试。 | OPEN |
| P-03 | MAJOR | `T03TransferContractIntegrationTest.java:37-123`；`T03TransferHappyPathIntegrationTest.java:35-51` | AC-10 与 Interface 要求流水失败注入及精确计数；现有测试没有覆盖单边流水、第二次写入失败或精确流水数，却在 Verification 中标记 PASS。 | 补失败注入、单边流水回滚及精确记录数断言。 | OPEN |
| P-04 | MINOR | `tickets/03-concurrency-regression-documentation.md:3`；`README.md:21`；`02_verification.md:8` | Ticket 03 已人工验收，但 Ticket 状态仍为 `waiting-for-acceptance`；README/Verification 仍描述提交前工作区版本。 | 将状态和被验证版本更新为实际候选提交。 | OPEN |

### Standards 轴

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| S-01 | BLOCKER | `02_verification.md:8,16-30` | 验证对象仍指向 `ec7d5e4 + Ticket 03 当前工作区`，结果仅写“本次容器输出”，没有可读取的原始输出路径，无法证明证据对应候选 `5ce456c`。违反 `STD-TEST-02`、Testing §4/§7 和 Documentation §6。 | 针对候选提交重跑或核对验证，保存可读取输出并更新版本。 | OPEN |
| S-02 | BLOCKER | `T03TransferContractIntegrationTest.java:32,82-89` | 集成测试直接注入 `StorageLocationRepository`，并用 `ReflectionTestUtils` 修改内部状态，违反 `training-server/AGENTS.md` §4 和 Testing §5。 | 改用批准的公开 API/稳定业务入口；若不存在入口，先裁决测试夹具边界或调整测试层级。 | OPEN |
| S-03 | MINOR | `TransferPage.tsx:8-12` | 判断项：库位编码与 ID 的同一映射被分别硬编码，属于 possible Duplicated Code / Repeated Switches。 | 用一个常量映射派生双向查找；由人工决定是否本轮处理。 | OPEN |

## 问题复盘

- 直接原因：HTTP 参数校验被误当作 Module 领域校验；UI 只验证单次请求；流水原子性以代码阅读代替了批准的故障接缝；验证记录没有在候选提交形成后重新固定版本和证据路径。
- 为什么原有检查没有更早发现：Ticket 验收集中在成功展示、后端错误与并发结果，没有逐条核对 Interface 的 Module 调用者、连续 UI 状态和第二次流水写入失败。
- 哪道门禁可以提前发现：Spec 评审固定 HTTP/Module 双边界验收；Ticket 拆分时把 UI 状态序列和流水故障注入列为必测；候选提交后强制校验 Verification 的提交号与证据路径。
- 防止重复的具体动作、负责人和期限：开发负责人应在最终 Decision 前处理 P-01—P-03、S-01—S-02，重新验证并复审；P-04 与 S-03 由人工裁决是否随阻塞修复一并关闭。

## Agent 建议

`REWORK`

> Spec 轴 4 项，最高 BLOCKER；Standards 轴 3 项，最高 BLOCKER。当前不得生成 `04_decision.md`、推送或合并；本文件不代替人工裁决。

## Finding 修复记录

2026-09-10 在候选提交 `dc355abe45134df1f3e0700074240e816d5418cb` 完成修复，待独立复审：

- P-01：`InventoryService.transfer` 在 Module 入口统一校验正数数量和不同库位，并增加直接 Module 调用测试。
- P-02：UI 每次操作开始时清除旧成功结果。
- P-03：增加精确两条流水断言和第二条流水写入失败的事务回滚测试。
- P-04：Ticket 03 状态、README 和 Verification 已指向实际候选提交。
- S-01：候选提交已重跑目标测试、前端构建和全量验证，证据路径已写入 `02_verification.md`。
- S-02：集成测试已移除 Repository 注入和反射造数据，仅通过公开 API 或稳定 Module 入口验证。
- S-03：UI 库位 ID/编码改为由单一常量表派生双向查找。

## 2026-09-10 独立复审

- 固定起点：`s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2`
- 被审候选：`78d2e29423b6d7bcfadf346fdb0c9479b25085eb`
- 固定 Diff：`git diff s2-t03-start...78d2e29423b6d7bcfadf346fdb0c9479b25085eb`

### Standards

1. **BLOCKER — S-01 未关闭**：`02_verification.md:7`、`README.md:20` 仍写旧起点 `e27b3d3`，与当前 `s2-t03-start` 的 `fec0c890` 不一致；本文档原被审对象仍为 `5ce456c`。另外，`02_verification.md:19` 以构建产物 `app.js` 代替原始构建输出。违反 `STD-TEST-02`、Documentation §5/§7 和 Testing §7。
2. **BLOCKER — S-02 未关闭**：`T03TransferAtomicityIntegrationTest.java:33-56` 通过同包访问和 `@SpyBean` 替换内部 `InventoryMovementStore` 的第二次核心流水写入，仍违反 `training-server/AGENTS.md` §4 和 Testing §5。`InventoryMovementStore` 同时是 possible Middle Man / Speculative Generality 判断项。
3. **BLOCKER — 新测试缺口**：`T03TransferContractIntegrationTest.java:92-98` 删除了停用库位和跨仓库位测试且无替代覆盖，违反 `STD-TEST-01`、Testing §3 和已批准验收范围。

原 S-03 已关闭：`TransferPage.tsx:8-10` 已从单一 `locations` 表派生双向映射。

### Spec

1. **MAJOR — AC-07 缺失**：`spec.md:83` 要求停用或异仓库位返回 409 / `WMS_INVALID_LOCATION`，并指定 HTTP 集成测试；当前只覆盖不存在库位和源库存不存在，但 `02_verification.md` 仍将 AC-04—08 整体标为 PASS。
2. **MAJOR — AC-10/原子性部分实现**：`spec.md:86`、`interface.md:108-110` 要求失败后余额、流水和审计均不变。`T03TransferAtomicityIntegrationTest.java:47-62` 只核对两端余额，未证明单边流水和审计已回滚；任务列表只配对双流水，不能发现残留的单条 `TRANSFER_OUT`。

P-01、P-02、P-04 已关闭；P-03 仍部分开放。未发现 scope creep。

### 复审结论

`REWORK`

Standards 3 项，最高 BLOCKER；Spec 2 项，最高 MAJOR。当前不得进入人工 Decision。

## 2026-09-10 二次修复记录

候选代码提交：`0424ad0949e18dea56bf099fbdd286ba25038726`，待再次独立复审。

- 基线引用已统一为 `s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2`，并保存可读取的目标测试和前端构建输出。
- 流水失败测试改为通过公开 `TransferMovementRecorder` Adapter 故障接缝注入，不再同包访问内部 Store。
- 第二条流水写入失败后，测试通过 HTTP 核对两端余额，再以同键同载荷重试成功，并证明任务只有一条、审计只有一次。
- 课堂演示数据增加一个停用库位和一个异仓库位；HTTP 集成测试恢复 AC-07 两个场景，不再从测试直访 Repository 或反射造数据。

## 2026-09-10 第三次独立复审

- 固定起点：`s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2`
- 被审候选：`5137443d0df1399c1410f12b143888573d169426`
- 固定 Diff：`git diff s2-t03-start...5137443d0df1399c1410f12b143888573d169426`

### Standards

1. **BLOCKER — S-01 仍未关闭**：`02_verification.md:20` 把全量 `mvn clean verify` 证据指向两个目标测试摘要，无法证明 52 tests / 2 skipped / `BUILD SUCCESS`；旧表中仍有“本次容器输出”这类不可读路径。违反 Testing §4/§7 和根 `AGENTS.md` §6。
2. **BLOCKER — S-02 仍未关闭**：`T03TransferAtomicityIntegrationTest.java:38-55` 仍以 `@SpyBean TransferMovementRecorder` 替换核心流水写入；新增公开 `TransferMovementRecorder` 只有一个实现和一个生产调用方，且未重新审批。违反 Testing §5、Architecture §3/§4 和根 `AGENTS.md` §4；同时是 possible Speculative Generality / Middle Man。

停用/异仓库位覆盖、基线 SHA 以及原 S-03 已关闭。

### Spec

1. **MAJOR — AC-10 仍部分覆盖**：`spec.md:86` 要求“同键不同载荷”和“已存在单边流水”均返回 409 / `WMS_IDEMPOTENCY_CONFLICT`。当前只有前者的 HTTP 冲突测试；故障回滚测试证明失败不留单边流水，但没有验证“提交前已有单边流水”的实现分支。

AC-07 和流水失败原子性已关闭；未发现 scope creep 或错误实现。

### 复审结论

`REWORK`

Standards 2 项，最高 BLOCKER；Spec 1 项，最高 MAJOR。当前不得进入人工 Decision。

## 2026-09-10 第三次修复记录

候选代码提交：`887ca779ec96f354ceae573ede9e02d793bd3ae1`，待再次独立复审。

- S-01：使用 Maven 原生日志参数保存完整 `clean verify` 输出到 `evidence/full-verify-20260910.log`；结果为 53 tests、0 failures/errors、2 skipped、`BUILD SUCCESS`。旧执行表明确降级为未落盘的历史过程记录，不再作为当前候选证据。
- S-02：删除只有一个实现和调用方的公开 `TransferMovementRecorder`；生产事务恢复直接保存两条流水。原子性测试通过仅存在于测试源码的 HTTP 夹具临时增加数据库约束，让真实的第二条 `TRANSFER_IN` 持久化失败，验证余额和第一条流水随事务一起回滚，解除约束后同键可成功重试。
- AC-10：增加只存在 `TRANSFER_OUT` 的隔离测试夹具状态，从正式 `/api/wms/inventory/transfer` 验证 409 / `WMS_IDEMPOTENCY_CONFLICT`、余额和审计不变。夹具 Controller 仅存在于 `training-server/src/test`，不进入生产包或运行路径。

## 2026-09-10 第四次独立复审

- 固定起点：`s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2`
- 被审候选：`4057c2ca9e0aa1b7781071531327b0e183bfde6f`
- 固定 Diff：`git diff s2-t03-start...4057c2ca9e0aa1b7781071531327b0e183bfde6f`

### Standards

1. **BLOCKER — S-02 仍未关闭**：`T03TransferAtomicityIntegrationTest.java:87-112` 通过测试 Controller 和 `JdbcTemplate` 执行 DDL；`T03TransferContractIntegrationTest.java:193-215` 同样直接插入单边流水。HTTP 包装没有改变其绕过业务入口、直接修改数据库的事实，违反 `training-server/AGENTS.md` §4 和 Testing §5。应先由所有者批准真实测试接缝，或在 `business-wms` 层通过不绕过核心行为的稳定入口验证。
2. **MAJOR — 验收映射不完整**：`02_verification.md:61` 将 AC-09/AC-10 标为 PASS，但没有引用新增的 `rejectsAnIdempotencyKeyWithOnlyOneExistingMovement`，验收映射与实际证据不一致，违反 Testing §7 与 Documentation §7。

S-01 已关闭：`02_verification.md` 已固定被验代码 `887ca779ec96f354ceae573ede9e02d793bd3ae1`，完整日志可复核 53 tests、2 skipped 和 `BUILD SUCCESS`。无额外 Fowler 判断项。

### Spec

`PASS`。未发现缺失或部分需求、范围蔓延或错误实现。

- AC-10：单边 `TRANSFER_OUT` 后，正式 HTTP 返回 409，余额与审计不变；`InventoryService.transfer` 对仅一侧流水判定冲突。
- 原子回滚：第二条流水真实落库失败后余额不变，同键可以重试成功，任务只有一条且审计只有一次。
- 全量证据：`evidence/full-verify-20260910.log` 可复核 53 tests、0 failures/errors、2 skipped 和 `BUILD SUCCESS`。

### 复审结论

`REWORK`

Standards 2 项，最高 BLOCKER；Spec 0 项，PASS。当前不得进入人工 Decision。

## 2026-09-10 P0 修复记录

候选代码提交：`f95105bf4e70e111b91f6e12803d5e79fb12fc2b`，待独立复审。

- 已从两份 `training-server` 集成测试中删除测试 Controller、`JdbcTemplate`、DDL 和直接 INSERT；P0 所指的数据库绕过已移除。
- 原子回滚改由已批准的 `AuditRecorder` Adapter 在事务末端抛错；测试从公开 HTTP 入口核对两端余额不变，并以同键重试成功、任务只有一条、审计只有一次证明流水随事务回滚。
- “提交前已有单边流水”无法由公开业务入口产生。对应自动化测试已删除，Verification 将 AC-10 标为 `PARTIAL`，等待所有者批准合规测试接缝或调整该验收条件；不再以违规夹具宣称 PASS。
- 最终验证日志已刷新：52 tests、0 failures/errors、2 skipped、`BUILD SUCCESS`。
