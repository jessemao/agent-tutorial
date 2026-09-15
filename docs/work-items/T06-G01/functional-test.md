# 独立功能测试：T06-G01

> **AI 委派 / 非人工记录**：本记录由测试 Agent 受委派执行，属于非人工事实记录。它不冒充具名测试工程师；QA 与业务验收仍分别记录，但课堂不要求额外签署。

## 测试对象与责任

- 测试责任方 / 所属团队或流程：AI 委派测试 Agent（非人工；按课堂建议执行，未要求具名签署）
- 候选提交 / Spec 版本 / 环境 / 开始结束时间：`dcd5eb2` / `spec.md` v1 / H2 默认 Profile 与临时隔离 MySQL 8.0；2026-09-15（+0800，容器日志为 UTC）
- 开发交接：`02_verification.md`；代码评审：`03_review.md`
- 执行原则：只通过公开 MockMvc/API 和前端构建验证；不调用 Repository，不直接改业务数据库制造成功，不查看教师卡。

## 独立测试设计与执行

| 用例 ID | AC / 风险 | 前置条件与数据 | 页面或 API 步骤 / 请求 | 预期 | 实际结果 | 原始证据 | 缺陷 ID |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FT-H2-01 | AC-01/02/03/15/16 创建、查询、主数据、活动范围、创建幂等 | H2 默认演示数据；有效/无效、多明细和无余额维度 | 通过 MockMvc 创建、详情/列表/筛选查询，重复键和冲突范围重放 | 生成 DRAFT、业务值完整；整单冲突；安全重放不重复建单 | 通过，6/6 | `training-server/target/surefire-reports/com.acme.training.T06CountDraftIntegrationTest.txt` | 无 |
| FT-H2-02 | AC-04/05/06/15/16 保存、提交、版本与草稿回滚 | H2 DRAFT；部分明细、null/0、陈旧版本 | 公开保存与提交入口，重复键、异参和不完整提交 | 原子保存；null 与 0 区分；提交后只读；冲突保持服务端状态 | 通过，4/4 | `training-server/target/surefire-reports/com.acme.training.T06CountRecordAndSubmitIntegrationTest.txt` | 无 |
| FT-H2-03 | AC-12/13/14/15/17 驳回、重开、取消与生命周期 | H2 SUBMITTED/DRAFT；原因为空/非空及终态 | 公开状态转换入口执行 REJECT、REOPEN、CANCEL 和重复动作 | 原因必填；历史留痕；取消释放范围且不可恢复；非法状态稳定失败 | 通过，4/4 | `training-server/target/surefire-reports/com.acme.training.T06CountRejectCancelIntegrationTest.txt` | 无 |
| FT-H2-04 | AC-07/08/09/11/14/15/19 审核、自审、正负零差异、回滚、审计 | H2 已提交盘点；库存余额与占用量；双明细故障 | 公开审核、库存查询和重复审核入口 | 独立审核；物理总量校准；低于占用时整单回滚；不重复调整；错误/审计稳定 | 通过，7/7 | `training-server/target/surefire-reports/com.acme.training.T06CountApprovalIntegrationTest.txt` | 无 |
| FT-H2-05 | AC-10/11/18/19 差异预览、token 与确认窗口 | H2 已提交盘点；审核前库存变化 | 首次审核获取最新差异/token，再以旧/新 token 确认 | 首次不改库存；未变化可确认；再次变化使旧 token 失效并返回新差异 | 通过，2/2 | `training-server/target/surefire-reports/com.acme.training.T06CountConfirmationIntegrationTest.txt` | 无 |
| FT-H2-06 | AC-20 兼容回归 | 既有 T01—T05 库存流程 | 执行库存回归、收货/出库/移库公开入口 | 既有库存行为不回归 | 通过，2/2 | `training-server/target/surefire-reports/com.acme.training.InventoryRegressionTest.txt` | 无 |
| FT-UI-01 | AC-01/04/06/10/16/20 页面契约与构建 | 当前课堂前端 | 执行 `npm run build`（TypeScript + Vite） | 页面编译成功，盘点入口及前端契约未阻断构建 | 通过 | 前端容器构建输出；产物位于 `training-server/src/main/resources/static/` | 无 |
| FT-MYSQL-01 | AC-02/15：同幂等键并发创建 | MySQL 8.0、`mysql-verification` Profile、两个并发公开创建请求 | `T06CountApprovalMysqlConcurrencyIntegrationTest#concurrentIdenticalCreateSafelyReplaysOneDraft` | 两请求均 200，返回同一 DRAFT/版本，列表仅一单 | 通过；两请求均 200 且安全重放同一结果 | `training-server/target/surefire-reports/com.acme.training.T06CountApprovalMysqlConcurrencyIntegrationTest.txt` | 已关闭 FT-T06-001 |
| FT-MYSQL-02 | AC-02/18：活动范围并发与冲突证据 | MySQL 8.0、两个不同幂等键争抢同一 SKU/仓库/库位 | `T06CountScopeMysqlConcurrencyIntegrationTest#uniqueActiveScopeAllowsAtMostOneConcurrentDraft` | 200/409 在超时内完成；409 含冲突单号/行号；最终仅一条活动范围 | 通过；在超时内返回 200/409，409 含冲突单号/行号，最终仅一条活动范围 | `training-server/target/surefire-reports/com.acme.training.T06CountScopeMysqlConcurrencyIntegrationTest.txt` | 已关闭 FT-T06-002 |
| FT-MYSQL-03 | MySQL 环境/隔离级别 | MySQL 8.0 临时数据库 | `MysqlVerificationEnvironmentTest#verificationUsesRealMysqlEightAndRepeatableRead` | MySQL 8.0、Repeatable Read、隔离 catalog | 通过，1/1 | `training-server/target/surefire-reports/com.acme.training.MysqlVerificationEnvironmentTest.txt` | 无 |

## 专项与回归

- H2 需求级公开入口复核：26/26 通过（创建/查询、保存/提交、驳回/重开/取消、审核/回滚、差异确认和既有库存回归）。
- MySQL 专项：6 项执行，6 项通过；环境检查、同幂等键重放、活动范围冲突及审核与收货/出库/移库竞争路径均通过。
- 两项并发缺陷已在 `dcd5eb2` 修复，并在全新隔离 MySQL 8.0 实例中重测关闭；测试库使用临时 tmpfs，未触及既有数据库。
- 回滚/事务：H2 已验证第二行失败不留下第一行库存变化、盘点批准或成功状态；MySQL 并发专项未发现新的数据不变式破坏。
- 兼容：`InventoryRegressionTest` 2/2 通过；前端 TypeScript/Vite 构建通过。未执行浏览器驱动的手工页面旅程，因此页面视觉/浏览器运行时仍是限制。
- 本次独立复测未重复执行浏览器驱动的手工页面旅程；前端 TypeScript/Vite 构建和课堂 clean verify 通过，页面运行时仍建议由真实测试人员补充。
- 未向外部缺陷系统写入；`FT-T06-001`、`FT-T06-002` 在本次修复后重测关闭，保留在本记录中作为历史发现。

## 测试工程师结论

**PASS（AI 委派 / 非人工签署）**

理由：H2 需求级流程与兼容回归通过；修复后 MySQL 8.0 专项 6/6 通过，同幂等键安全重放、活动范围结构化冲突和超时要求均满足。浏览器级手工旅程仍需真实测试人员补充确认。

本结论仅代表测试 Agent 的事实记录；QA 审核、业务验收和最终放行分别记录。本记录不构成人工签字，也不阻止企业按自有流程继续推进。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| 独立功能测试结果 / `dcd5eb2` | 测试工程师 | AI 委派测试 Agent（非人工；正式负责人待指定） | CONFIRMED（非人工记录） | 2026-09-15；PASS；H2 26/26、MySQL 6/6 通过；本文件 FT-MYSQL-01/02 已重测 |
| FT-T06-001 同幂等键并发创建 | QA / 开发负责人 | 待指定 | CONFIRMED（非人工复测） | `dcd5eb2` 修复；MySQL 两请求均 200 且同一 DRAFT |
| FT-T06-002 活动范围并发冲突 | QA / 开发负责人 | 待指定 | CONFIRMED（非人工复测） | `dcd5eb2` 修复；MySQL 200/409 结构化响应且无超时 |
| QA 质量审核 | QA/质量责任方或企业流程 | 按企业流程指定或 N/A | CONFIRMED | 课堂建议之外的企业流程可不要求签署；演练版 QA 记录见 `qa-review.md` |
