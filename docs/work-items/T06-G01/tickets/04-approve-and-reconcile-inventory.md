# 04：原子审核并校准库存

**状态：** ACCEPTED（开发负责人于 2026-09-13 验收完成）  
**Blocked by：** None（Ticket 02 已于 2026-09-13 验收通过）  
**Spec：** `docs/work-items/T06-G01/spec.md` v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06`

## 交付行为

与创建人和最后提交人不同的审核人，可批准账面未发生变化的待审核盘点。系统通过库存 Module 的单个批量 Interface 原子处理全部明细：实盘代表物理总量，占用量保持不变，可用量变为实盘减占用；正差异、负差异、零差异和有效无余额维度均有明确结果。任一明细非法或事务失败时，盘点、余额、流水、范围释放和成功审计都不部分提交，盘点保持待审核。

## 修改边界

- 允许修改：`business-wms` 的盘点审核用例、批量 `InventoryOperations.reconcileCount` Interface/Implementation、盘点/余额锁、盘点专属库存流水、失败分类记录及审核 HTTP Adapter；`training-server` 的审核页面、演示数据与集成/故障测试。
- 禁止修改：平台 Modules、既有库存方法及其公开语义、既有入库/出库/移库 HTTP 契约、差异二次确认行为（Ticket 05）、完整权限系统、治理规范和第三方 Skills。
- Module `AGENTS.md`：必须遵守根规则、`business-wms/AGENTS.md` 与 `training-server/AGENTS.md`。

## 验收条件

- [x] AC-07：所有盘点写请求要求非空 `X-Operator`；创建人或最后提交人审核被拒绝，库存/状态不变。
- [x] AC-08：账面未变时，正/负差异整批调整成功，保留 `reserved`，设置 `available = countedTotal - reserved`，返回审核前/后快照并标记 `APPROVED`。
- [x] AC-08/11：零差异可批准且不生成零变化流水；有效无余额维度可创建零余额并按实盘调整。
- [x] AC-09：实盘小于占用、溢出、失效主数据或任一故障使整批回滚，状态保持 `SUBMITTED`，不记录批准时间或成功审计。
- [x] AC-09：可重试失败只在独立短事务记录稳定失败分类，不保存堆栈、不掩盖原异常、不生成成功动作回执。
- [x] AC-15：审核同键同参返回首次结果，同键异参冲突，新键重复批准返回非法状态，库存/流水不重复。
- [x] AC-19：审核动作、操作者、时间、对象和成功/失败分类可追溯且无敏感内部信息。
- [x] 每次非零盘点库存变化产生且仅产生对应盘点流水；全部明细按批准锁序处理。

## 开发验证

- `T06CountApprovalIntegrationTest`：7 tests / 0 failures / 0 errors；覆盖审核成功、自审、缺失操作者、预占保护、账面变化、零差异/无余额、幂等和多明细回滚。
- `T06CountApprovalMysqlConcurrencyIntegrationTest`：MySQL 8.0 实跑 1 test / 0 failures / 0 errors；审核与收货竞争无丢更新。
- 默认全量：65 tests / 0 failures / 0 errors / 4 个 MySQL Profile 条件测试跳过；`mvn clean verify` 成功。
- 前端构建、执行 Skill 门禁、AI Governance 与 `git diff --check` 全部通过。
- 最新差异 token 生成与二次确认仍严格留在 Ticket 05；本票只拒绝账面已变化的首次审核。

## 验证命令

```text
mvn -pl business-wms -am test
mvn -pl training-server -am test
mvn -pl training-server -am test -Pmysql-verification -Dtest=<本票盘点审核原子性测试>
mvn clean verify
```

## 停止条件

- 批量审核必须由盘点调用方逐行拼装库存动作，或需要绕过 `InventoryOperations`。
- 需要修改平台幂等、审计、操作者或统一错误契约。
- 锁顺序与现有库存动作形成反向获取，且无法在批准设计内消除。
- 审核失败后单据、余额、流水、范围或成功审计出现任何部分提交。
