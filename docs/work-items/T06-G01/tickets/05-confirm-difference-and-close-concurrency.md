# 05：差异二次确认与并发兼容收口

**状态：** ACCEPTED（开发负责人已于 2026-09-13 验收）  
**Blocked by：** None（Tickets 03、04 已于 2026-09-13 验收通过）  
**Spec：** `docs/work-items/T06-G01/spec.md` v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06`

## 交付行为

盘点提交后若入库、出库或移库改变了账面数量，首次审核不会覆盖库存，而是返回最新逐行差异和 opaque token；审核人看见并确认新差异后可再次审核。如果确认窗口内库存再次变化，系统继续返回新的差异和 token。完成盘点与既有库存动作的 MySQL 并发验证、完整页面旅程、全部 AC 和既有业务兼容回归，形成可供后续完整开发验证的功能候选。

## 修改边界

- 允许修改：`business-wms` 的审核 preview/token、结构化盘点冲突、锁与并发缺陷修正；`training-server` 的差异确认页面、并发/端到端/兼容集成测试；只允许修正 Tickets 01—04 在本票验证中暴露且仍位于批准范围内的问题。
- 禁止修改：平台 Modules、批准 Spec/Design/Interface、现有业务契约、未关联重构、新依赖、治理规范和第三方 Skills。
- Module `AGENTS.md`：必须遵守根规则、`business-wms/AGENTS.md` 与 `training-server/AGENTS.md`。

## 验收条件

- [x] AC-10：提交后任一账面变化使首次审核返回 `WMS_COUNT_DIFFERENCE_CHANGED`、最新详情和 opaque token；库存和盘点状态不变。
- [x] AC-11：token 绑定盘点 ID/版本、规范维度及最新余额事实；事实未变时确认成功，再次变化时旧 token 失效并返回新差异/token。
- [x] AC-18：MySQL 中盘点审核与入库、出库、移库并发执行，保持统一锁序，无丢更新、部分提交、未受控死锁或库存不变式破坏。
- [x] AC-20：完整页面旅程覆盖创建、保存、提交、驳回/重开、取消、直接审核、差异确认、列表/详情和刷新恢复。
- [x] AC-20：目标测试、`business-wms` 回归、`training-server` 回归、所有适用 MySQL 专项和 `mvn clean verify` 均通过；失败/跳过不被记录为通过。
- [x] AC-20：既有入库、出库、移库和余额查询路径、字段、状态码、错误码及最终库存行为保持不变。
- [x] AC-01—AC-19 在同一候选版本上均有开发 TDD/集成证据映射；测试工程师和 QA 结论仍明确待后续独立阶段执行。
- [x] 所有结构化冲突响应由业务 Adapter 提供，平台统一响应与异常实现没有修改。

## 验证命令

```text
mvn -pl business-wms -am test
mvn -pl training-server -am test
mvn -pl training-server -am test -Pmysql-verification -Dtest=<T06 全部 MySQL 并发与约束测试>
./scripts/classroom-verify.sh T06
mvn clean verify
```

## 停止条件

- H2 与 MySQL 对唯一、锁或事务结果产生影响验收的差异且没有批准处置。
- 为通过回归需要改变既有业务、弱化测试、修改平台契约或扩大到未批准 Module。
- 任一 AC 无法通过公开 Interface/HTTP/页面 Seam 证明，或结果无法对应同一 Git 版本。
- 测试工程师、QA 或业务验收尚未执行时，不得把本票开发验证描述为整体交付通过。
