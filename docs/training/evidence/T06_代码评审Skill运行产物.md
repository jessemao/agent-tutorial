# T06 代码评审 Skill 运行产物

> 评审范围：`s2-t06-start..s3-t06-answer`；已批准需求：T06 学员任务卡确认的 7 条训练规则。

## Findings

### 已修正

`[P1][SPEC] 创建盘点单未记录审计，可绕过操作人校验 — StocktakeService.create`

- 影响：盘点单是库存修正的前置单据；无操作人创建会破坏追溯链，且与现有写操作不一致。
- 证据：首版 `create` 只保存单据，没有调用 `AuditRecorder`。
- 修正：创建后记录 `CREATE/STOCKTAKE/countNo`，并新增无 `X-Operator` 时整体回滚的测试。

最终差异无剩余阻断项。

## Acceptance-criteria evidence matrix

| 验收条件 | 主要代码/测试证据 | 结论 |
| --- | --- | --- |
| 单 SKU+仓库+库位盘点 | `CreateStocktake`、`Stocktake` | 通过 |
| 实盘数为物理总量 | `adjustToCountedTotal` 以实盘总量减预占得到可用量 | 通过 |
| 创建不调整，审批才调整 | `stocktakeChangesInventoryOnlyAfterApproval` | 通过 |
| 实盘总量不得小于预占 | `stocktakeRejectsPhysicalTotalBelowReservedQuantity` | 通过 |
| 审批时校验总量和预占快照 | `stocktakeApprovalRejectsStaleInventorySnapshotAndRollsBackStatus` | 通过 |
| 审批调整安全重放/冲突 | `repeatedStocktakeApprovalRequiresSameIdempotencyKey` | 通过 |
| 流水和审计 | `InventoryMovement(COUNT_ADJUST)`、`AuditRecorder`及集成日志 | 通过 |
| 写操作需操作人 | `creatingStocktakeRequiresOperatorForAudit` | 通过 |
| 不越权改平台 | 平台两模块无T06代码差异 | 通过 |

## Tests observed or run

- Maven `clean verify`：成功。
- 仓储集成测试：16条（原11 + T06新5），0失败，0错误。
- 平台契约测试：3条，0失败，0错误。
- `git diff --check`：通过。

## Residual risks and missing evidence

- 训练版不冻结库位，以审批时快照冲突和重盘保证正确性；高频出入库场景需评估冻结或短快照周期。
- 盘点号唯一冲突仍依赖数据库约束，未单独映射为业务错误。
- 训练版未实现审批权限和生产审计持久化，已列为非目标。
- 仅针对已有库存维度完成差异调整；盘点发现新 SKU/新库位需另行定义业务流程。

## Verdict

`PASS_WITH_FOLLOW_UP`：满足T06训练验收；上线前需补权限、生产审计适配器和盘点发现新库存的流程决策。
