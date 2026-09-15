# 最终交付绑定（历史重写后）

- 最终候选：`T06-G01` / `85236d0`；代码起点：`s2-t06-start` / `c8613f4`。
- 本文件中较早的候选提交号保留为历史证据；最终测试结果、构建结果和治理校验均归属于当前候选。
- 课堂测试/QA签署不构成强制门禁；采用的自动化与演练证据已明确标注。

# 02 Verification：T06-G01 / Ticket 01

## 最终候选验证（2026-09-14）

- 候选提交：`051a023`；相对固定起点 `9bc424c` 完成 InventoryCountService 实质性职责拆分与机械依赖袋清理（历史重写后的对应提交）。
- 拆分为草稿/提交、审核迁移、查询投影三个真实应用服务；统一使用 `CountAction`，并保留公共事务边界；命令支持组件仅保留共享命令行为与两项协作者。
- 定向 T06 草稿、审核、驳回/取消集成测试：17 项，0 失败。
- 代表性回归：1 项，0 失败；`./scripts/classroom-verify.sh T06`：PASS；后端 69 项，0 失败，4 项条件跳过；前端 TypeScript/Vite 生产构建成功，clean verify 通过。
- MySQL 专项仍按环境条件执行；默认课堂验证中的 MySQL 测试为条件跳过，非代码失败。

## REWORK 候选验证（2026-09-14）

- 相对候选：`640a4fd`；本轮修复创建首次结果重放、保存草稿结构化范围冲突、未知数据库约束回退、动作领域类型与依赖职责簇。
- 新增回归：创建成功后继续修改，再用原创建键重放仍返回首次 `DRAFT/version=1`；未知数据库约束不得伪装为范围冲突。
- `./scripts/classroom-verify.sh T06`：PASS。
- 自动测试：68 项，0 失败，4 项条件跳过。
- 前端 TypeScript/Vite 生产构建、clean verify 与课堂 MySQL 接缝：PASS。

## 并发语义补强（2026-09-14）

- 创建在稳定顺序锁定全部库位后，于锁内重新读取 `CREATE` 首次结果回执。
- MySQL 增加同键同参并发创建测试：两个请求均返回 200 且指向同一盘点单。
- 不同键重叠范围在锁内重新读取活动范围，因此走包含冲突单和行号的结构化冲突路径。
- `./scripts/classroom-verify.sh T06`：PASS；默认与 clean 两轮均为 68 项测试、0 失败、4 项条件跳过；前端构建及 MySQL 接缝通过。

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `b09d8e69db9403e891bdc2562d3742487d3cdc23`（`s2-t06-start`） |
| 被验证分支/提交 | 分支 `T06-G01` / 最终候选 `85236d0` |
| Spec/Standards | `spec.md` v1 / `STD-WMS-0.7-06` / 已批准 Ticket 01 |
| Docker/Java/Maven/DB 环境 | classroom 容器；Java 17.0.15；Maven；H2 默认 Profile；MySQL 8.0.46 临时容器、`mysql-verification` Profile |

## 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | Ticket 01 目标测试 | PASS | 5 tests / 5 failures；新建、查询和主数据端点均返回 404，属于预期行为 Red | `training-server/target/surefire-reports/com.acme.training.T06CountDraftIntegrationTest.txt`（后续 Green 已覆盖报告） |
| 目标测试 | `mvn -pl training-server -am -Dtest=T06CountDraftIntegrationTest test` | PASS | 5 tests / 0 failures / 0 errors | `training-server/target/surefire-reports/com.acme.training.T06CountDraftIntegrationTest.txt` |
| 前端构建 | `npm run build` | PASS | TypeScript 与 Vite 构建成功；仅保留既有的大 chunk 提示 | `training-server/src/main/resources/static/` |
| business-wms 回归 | `mvn -pl business-wms -am test` | PASS | reactor BUILD SUCCESS；上游 3 tests 全通过，business-wms 无独立测试 | Maven 控制台输出 |
| training-server 回归 | `mvn -pl training-server -am test` | PASS | 49 tests / 0 failures / 0 errors / 3 个 MySQL Profile 测试按条件跳过 | `training-server/target/surefire-reports/` |
| MySQL 活动范围并发 | `mvn -pl training-server -am -Pmysql-verification -Dspring.profiles.active=mysql-verification -Dtest=T06CountScopeMysqlConcurrencyIntegrationTest test` | PASS | 1 test / 0 failures / 0 errors；两个并发请求结果为 200/409，唯一活动范围仅 1 条 | `training-server/target/surefire-reports/com.acme.training.T06CountScopeMysqlConcurrencyIntegrationTest.txt` |
| 全量验证 | `mvn clean verify` | PASS | 49 tests / 0 failures / 0 errors / 3 个条件跳过；reactor BUILD SUCCESS | `training-server/target/surefire-reports/` |
| 项目门禁 | T05 work-item-execute、AI Governance、`git diff --check` | PASS | Skill 合约 OK；治理材料完整；无空白错误 | 命令输出 |

MySQL 验证首次因课堂 Maven 离线仓库没有 MySQL 驱动而失败；切换到课堂工作区内可写的临时 Maven 仓库后成功下载依赖。随后首次真实并发运行暴露唯一约束异常未映射为 409，补充 Adapter 冲突映射后复测通过。临时 MySQL 容器已删除。

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01 | `InventoryCountService#create`、`InventoryCountController` | 多明细创建与详情查询 | 单号、DRAFT、业务文字和快照均符合 | PASS |
| AC-02（校验/原子性） | `validateShape`、主数据校验、`ActiveCountScope` | 重复、失效库位、活动冲突 | 返回 400/409；列表没有部分草稿 | PASS |
| AC-02（并发） | `uk_active_count_scope`、`InventoryCountExceptionHandler` | MySQL 并发测试 | 最多一个成功且数据库仅一条活动范围 | PASS |
| AC-03 | `InventoryOperations#getBalance` 快照 | SKU-304 / 库位 2 | 无余额维度快照为 0 | PASS |
| AC-15 | `uk_count_idempotency`、请求指纹 | 同键同参/异参 | 重试返回首次结果；异参 409；仅一张单 | PASS |
| AC-16 | `InventoryCountService#list/get` | 列表、状态筛选、详情刷新 | 稳定倒序、筛选分页与详情可恢复 | PASS |
| 页面 | `CountPage` | TypeScript/Vite 构建 | 业务值选择、多明细、新建、列表、刷新；内部键自动生成 | PASS |

## 开发交接

- 开发人员 / Ticket / TDD 红绿版本 / 执行时间：Codex / Ticket 01 / 同一未提交工作树 / 2026-09-13。
- 开发负责人确认：用户于 2026-09-13 回复“验收没问题”，Ticket 01 验收通过。
- 独立测试入口：`functional-test.md`（测试工程师在对应阶段生成）。
- QA 审核入口：`qa-review.md`（QA 在对应阶段生成）。
- 业务验收入口：`04_decision.md`（业务人员作出决定后生成）。

本文件仅记录开发完成当前 Ticket 的 TDD 与必要回归，不证明独立功能测试、QA 或业务验收通过。

## 未覆盖与剩余风险

- Ticket 01 不包含提交、驳回、取消、纠错、审核和库存调整；这些行为仍由 Tickets 02—05 阻塞管理。
- MySQL 驱动仅在验证 Profile 下使用；默认全量验证会按既有条件跳过 3 个 MySQL 专项测试，Ticket 01 的专项测试已另行真实执行通过。
- 前端构建存在既有 Ant Design chunk 大小提示，不影响本票功能。

## 结论

TICKET_01_ACCEPTED（仅记录开发负责人对 Ticket 01 的验收；未进入独立 Review）。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| Ticket 01 当前未提交工作树及本验证记录 | 开发负责人 | 用户（具名信息未登记） | CONFIRMED | 2026-09-13；用户原话：“验收没问题” |
| 独立功能测试、QA、业务验收 | 测试工程师 / QA / 业务所有者 | 待指定 | PENDING | 本阶段未执行 |

## Ticket 02：录入并提交盘点结果

### 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `T06CountRecordAndSubmitIntegrationTest` | PASS | 4 tests / 4 failures；保存入口返回 405，属于预期行为 Red | 后续 Green 已覆盖 Surefire 报告；原始控制台已观察 |
| 目标测试 | `mvn -pl training-server -am -Dtest=T06CountRecordAndSubmitIntegrationTest test` | PASS | 4 tests / 0 failures / 0 errors | `training-server/target/surefire-reports/com.acme.training.T06CountRecordAndSubmitIntegrationTest.txt` |
| 前端构建 | `npm run build` | PASS | TypeScript 与 Vite 构建成功；仅既有大 chunk 提示 | `training-server/src/main/resources/static/` |
| business-wms 回归 | `mvn -pl business-wms -am test` | PASS | reactor BUILD SUCCESS；上游 3 tests 通过 | Maven 控制台输出 |
| training-server 回归 | `mvn -pl training-server -am test` | PASS | 53 tests / 0 failures / 0 errors / 3 个 MySQL 条件测试跳过 | `training-server/target/surefire-reports/` |
| 全量验证 | `mvn clean verify` | PASS | 53 tests / 0 failures / 0 errors / 3 个条件测试跳过；BUILD SUCCESS | `training-server/target/surefire-reports/` |

### 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-04 | `saveDraft`、乐观 `version`、全量明细替换 | nullable/0、保存刷新、陈旧版本 | 版本递增；null 与 0 保持；冲突携服务端当前版本且不覆盖 | PASS |
| AC-05 | `submit` 完整性检查、`CountConflictException` | 未填写明细提交 | 返回逐行索引并保持 DRAFT | PASS |
| AC-06 | 提交快照、提交人/时间、SUBMITTED | 完整提交及提交后保存 | 快照正确；状态只读 | PASS |
| AC-15 | `CountActionReceipt` | 保存/提交同键重放、异参和新键重复状态动作 | 同参安全重放；异参/非法状态稳定冲突 | PASS |
| AC-16/UI | `CountPage`、保存/提交 Adapter | 构建、列表/详情响应 | 页面自动管理版本/幂等键，保留表单并显示服务端版本 | PASS |

### Ticket 02 交接

- 结论：`TICKET_02_ACCEPTED`。
- 开发负责人验收：CONFIRMED / 2026-09-13；用户原话：“验证完成，进入 Ticket 03”。
- 未进入 Ticket 04、独立 Review、功能测试、QA 或业务交付决定。

## Ticket 04：原子审核并校准库存

### 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `T06CountApprovalIntegrationTest` | PASS | 初始 4 tests / 4 failures；3 个审核请求 404；预占夹具因缺操作者 409，修正夹具后进入产品实现 | 后续 Green 已覆盖 Surefire 报告；原始控制台已观察 |
| 目标测试 | `mvn -pl training-server -am -Dtest=T06CountApprovalIntegrationTest test` | PASS | 最终 7 tests / 0 failures / 0 errors | `training-server/target/surefire-reports/com.acme.training.T06CountApprovalIntegrationTest.txt` |
| 多明细回滚 | 同一目标测试的 `failureOnSecondLineRollsBackEarlierAdjustment` | PASS | 第二行低于占用时，第一行余额、第二行余额和盘点状态均未部分提交 | 同上 |
| MySQL 并发 | `-Pmysql-verification -Dspring.profiles.active=mysql-verification -Dtest=T06CountApprovalMysqlConcurrencyIntegrationTest test` | PASS | MySQL 8.0；1 test / 0 failures / 0 errors；审核与收货串行化且无丢更新 | `training-server/target/surefire-reports/com.acme.training.T06CountApprovalMysqlConcurrencyIntegrationTest.txt`（后续默认 clean 覆盖为条件跳过报告；实跑控制台已观察） |
| 前端构建 | `npm run build` | PASS | TypeScript 与 Vite 构建成功；仅既有大 chunk 提示 | `training-server/src/main/resources/static/` |
| business-wms 回归 | `mvn -pl business-wms -am test` | PASS | reactor BUILD SUCCESS；上游 3 tests 通过 | Maven 控制台输出 |
| training-server 回归 | `mvn -pl training-server -am test` | PASS | 64 tests / 0 failures / 0 errors / 4 个 MySQL 条件测试跳过（新增最终回滚用例前） | `training-server/target/surefire-reports/` |
| 最终全量验证 | `mvn clean verify` | PASS | 65 tests / 0 failures / 0 errors / 4 个 MySQL 条件测试跳过；BUILD SUCCESS | `training-server/target/surefire-reports/` |
| 项目门禁 | 执行 Skill、AI Governance、`git diff --check` | PASS | Skill 合约 OK；治理材料完整；无空白错误 | 命令输出 |

### 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-07 | `InventoryCountService#approve`、`CurrentOperator` | 缺失身份、自审 | 409；盘点和余额不变 | PASS |
| AC-08 | `InventoryOperations#reconcileCount`、`InventoryBalance#reconcilePhysicalTotal` | 正差异、预占保持、批准快照 | 实盘成为物理总量；占用不变；返回审核前后事实 | PASS |
| AC-08/11 | 零差异分支、缺失余额创建 | SKU-304 / 库位 2 / 实盘 0 | 批准成功且余额为 0；零差异不写流水 | PASS |
| AC-09 | 批量事务、稳定排序、`CountApprovalFailureRecorder` | 第二行失败、低于占用 | 整批回滚并保持 SUBMITTED；独立短事务只保存稳定错误码 | PASS |
| AC-15 | `CountActionReceipt` | 同键重放、新键重复 | 首次结果稳定；库存不重复；新键返回非法状态 | PASS |
| AC-19 | 审核字段、平台审计、稳定失败字段 | 响应、日志与错误路径 | 操作者/时间/对象可追溯，无堆栈写入领域数据 | PASS |
| 并发/锁序 | 库位升序、维度排序、悲观余额锁 | MySQL 审核与收货竞争 | 两种合法串行结果之一，无丢更新 | PASS |
| 页面 | `CountPage`、审核 Adapter | TypeScript/Vite 构建 | 待审核提供独立审核入口；批准后展示审核人与库存快照 | PASS |

### Ticket 04 交接

- 结论：`TICKET_04_ACCEPTED`。
- 开发负责人验收：CONFIRMED / 2026-09-13；用户原话：“验收 ticket 04 完成。进入 Ticket 05”。
- Ticket 05 的 opaque token、结构化最新差异与二次确认未在本票实现。
- 未进入 Ticket 05、独立 Review、功能测试、QA 或业务交付决定。

## Ticket 05：差异二次确认与并发兼容收口

### 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `T06CountConfirmationIntegrationTest` | PASS | 初始 2 tests / 2 failures；响应尚无结构化当前值、最新差异和 token，属于预期 Red | 后续 Green 已覆盖 Surefire 报告；原始控制台已观察 |
| 差异确认目标测试 | `-Dtest=T06CountConfirmationIntegrationTest` | PASS | 2 tests / 0 failures / 0 errors；首次变化、旧 token 失效、新 token 确认均通过 | `training-server/target/surefire-reports/com.acme.training.T06CountConfirmationIntegrationTest.txt` |
| MySQL 并发专项 | `T06CountApprovalMysqlConcurrencyIntegrationTest,T06CountScopeMysqlConcurrencyIntegrationTest` | PASS | MySQL 8.0；4 tests / 0 failures / 0 errors；覆盖审核与入库、出库、移库及活动范围竞争 | MySQL 专项控制台实跑证据；默认 clean 会将条件测试记录为跳过 |
| 前端构建 | `npm run build` | PASS | TypeScript 与 Vite 构建成功；仅既有大 chunk 提示 | `training-server/src/main/resources/static/` |
| business-wms 回归 | `mvn -pl business-wms -am test` | PASS | reactor BUILD SUCCESS；上游 3 tests 通过 | Maven 控制台输出 |
| 最终全量验证 | `mvn clean verify` | PASS | 67 tests / 0 failures / 0 errors / 4 个 MySQL 条件测试跳过；BUILD SUCCESS | `training-server/target/surefire-reports/` |
| 项目门禁 | `./scripts/classroom-verify.sh T06`、`git diff --check` | PASS | Skills/AI 治理结构检查通过；课堂验证通过；无空白错误 | 命令输出 |

### 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-10 | `CountApprovalPreview`、业务本地 `CountConflictException` | 提交后收货再首次审核 | 409 携最新盘点、逐行差异和 opaque token；状态与库存不变 | PASS |
| AC-11 | token 对盘点 ID/版本/规范维度/余额数量与版本的绑定 | 首次 token 确认、再次收货后旧 token、再确认 | 未变成功；再次变化返回 `WMS_COUNT_CONFIRMATION_STALE` 和新 token | PASS |
| AC-18 | 库位与余额统一排序锁、整批预检后写入 | MySQL 审核分别与入库、出库、移库竞争 | 4 tests 全绿；无丢更新、部分提交、死锁或库存不守恒 | PASS |
| AC-19 | `APPROVAL_CONFLICT` 审计、preview 创建人/时间 | 两次差异变化与最终批准 | 冲突与成功动作分开记录，不保存堆栈或伪成功回执 | PASS |
| AC-20 | `CountPage` 差异表与确认按钮、全部 T06 集成测试 | 前端构建、67 项全量回归、课堂验证 | 页面保留最新 token 并支持反复刷新差异；既有路径兼容 | PASS（开发验证） |
| 平台边界 | `InventoryCountExceptionHandler` | 结构化 409 响应 | 仅业务 Adapter 扩展；平台模块未修改 | PASS |

### Ticket 05 交接

- 结论：`TICKET_05_ACCEPTED`。
- 开发负责人验收：CONFIRMED / 2026-09-13；用户原话：“验收完毕”。
- 独立 Review、功能测试、QA、业务验收和交付决定仍待后续独立阶段，不以本节替代。

## Ticket 03：驳回、重新编辑、取消与纠错

### 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | `T06CountRejectCancelIntegrationTest` | PASS | 4 tests / 3 failures；非提交转换返回 400，属于预期行为 Red | 后续 Green 已覆盖 Surefire 报告；原始控制台已观察 |
| 目标测试 | `mvn -pl training-server -am -Dtest=T06CountRejectCancelIntegrationTest test` | PASS | 4 tests / 0 failures / 0 errors | `training-server/target/surefire-reports/com.acme.training.T06CountRejectCancelIntegrationTest.txt` |
| 前端构建 | `npm run build` | PASS | TypeScript 与 Vite 构建成功；仅既有大 chunk 提示 | `training-server/src/main/resources/static/` |
| business-wms 回归 | `mvn -pl business-wms -am test` | PASS | reactor BUILD SUCCESS；上游 3 tests 通过 | Maven 控制台输出 |
| training-server 回归 | `mvn -pl training-server -am test` | PASS | 57 tests / 0 failures / 0 errors / 3 个 MySQL 条件测试跳过 | `training-server/target/surefire-reports/` |
| 全量验证 | `mvn clean verify` | PASS | 57 tests / 0 failures / 0 errors / 3 个条件测试跳过；BUILD SUCCESS | `training-server/target/surefire-reports/` |
| 项目门禁 | 执行 Skill、AI Governance、`git diff --check` | PASS | Skill 合约 OK；治理材料完整；无空白错误 | 命令输出 |

### 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-12 | `InventoryCount#reject/reopen`、`transition` | 驳回、缺少原因、重新编辑 | 原因必填；记录人员/时间；重新编辑保留驳回历史 | PASS |
| AC-13 | `InventoryCount#cancel`、活动范围删除 | 草稿取消、同范围重建 | 取消为终态并原子释放范围，同维度可重新建单 | PASS |
| AC-14 | 聚合状态守卫 | 已取消单重新编辑 | 返回非法状态且历史不变；审核由 Ticket 04 实现 | PASS（本票范围） |
| AC-15 | `CountActionReceipt`、乐观 `version` | 同键重放、异参、陈旧版本、新键重复 | 重放稳定；冲突不覆盖现状 | PASS |
| AC-17 | `correctionOfCountId`、`correctionOfCountNo` | 编译、目标测试及全量回归 | 仅允许关联 `APPROVED` 原单并保护原单；完整 HTTP 联调等待 Ticket 04 | PASS（结构接缝） |
| 页面 | `CountPage`、转换 Adapter | TypeScript/Vite 构建 | 展示原因/人员/时间/纠错关系并提供状态允许动作 | PASS |

### Ticket 03 交接

- 结论：`TICKET_03_ACCEPTED`。
- 开发负责人验收：CONFIRMED / 2026-09-13；用户原话：“Ticket 03 验收完成。进入 ticket 04”。
- 纠错单端到端测试需要 Ticket 04 提供合法 `APPROVED` 原单；未为测试加入审核后门。
- 未进入 Ticket 04、独立 Review、功能测试、QA 或业务交付决定。
