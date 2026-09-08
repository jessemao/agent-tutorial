# 02 Verification：T02-G01

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `91af1d6fa4f4851129d660624691136d4cb27697` |
| 被验证分支/提交 | `T02-G01` / findings 修复提交 `a1501c3e24e723b63fb67ea0595eda2412d36ca7` |
| Spec/Standards | `spec.md` v1；`STD-WMS-0.7-03` |
| Docker/Java/Maven/DB 环境 | `compose.classroom.yml`；Java 17.0.15；Maven 3.9.9；H2 默认环境；MySQL 8.0.46 / `REPEATABLE-READ` 专项环境 |

## 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | 在起点行为上运行 T02 目标测试 | FAIL | 3 个用例失败；首批 4 件实际返回 HTTP 409 / `WMS_INBOUND_FULL_RECEIPT_REQUIRED` | `inputs/input-evidence.md`；`inputs/partial-receipt-rejected.png`；本次 Docker 输出 |
| findings 定向测试 | Maven 定向执行并发重放、迁移、审计与库存失败回滚 | PASS | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`；`BUILD SUCCESS` | 本次 Docker 输出 |
| MySQL 专项验证 | MySQL Profile 执行环境断言与终批并发测试 | PASS | MySQL 8.0.46、`REPEATABLE-READ`；`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` | `MysqlVerificationEnvironmentTest`；`T02InboundMysqlConcurrencyIntegrationTest`；本次 Docker 输出 |
| 前端构建 | `docker compose -f compose.classroom.yml exec -T frontend npm run build` | PASS | TypeScript 检查与 Vite 构建成功；仅有已知 chunk 体积警告 | 本次 Docker 输出 |
| Module 回归 | `./scripts/classroom-test.sh T02 module` | PASS | 无失败；T02 目标、回滚和旧调用基线均通过 | 本次 Docker 输出 |
| 全量验证 | `./scripts/classroom-test.sh T02 all` 及 clean package | PASS | 两轮均为 `Tests run: 47, Failures: 0, Errors: 0, Skipped: 2`；`BUILD SUCCESS`；跳过项仅为需显式 MySQL Profile 的守卫测试，已在上一行独立通过 | 本次 Docker 输出 |
| 健康检查 | `curl -fsS http://localhost:8080/actuator/health` | PASS | 验证期间课堂后端停留在旧编译失败状态，重启并完成启动后返回 HTTP 200 | 本次 Docker 输出 |

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01—02：计划 10 按 4+6 累计入库 | `InboundOrder.receive`、`InboundService.receive` | `receivesTenAsFourThenSixAndSafelyReplaysTheFirstBatch` | PASS | 累计、三态和库存符合 Spec |
| AC-03：超收整体拒绝 | `InboundOrder.receive` | `rejectsOverReceiptAndIdempotencyConflictWithoutChangingTotals` | PASS | HTTP 409 / `WMS_INBOUND_OVER_RECEIPT`，数据不变 |
| AC-04：零数和负数拒绝 | `ReceiveInboundRequest` | `rejectsInvalidQuantitiesAndNewReceiptsAfterCompletion` | PASS | HTTP 400 / `INVALID_REQUEST` |
| AC-05—06：首次快照重放及全局键冲突 | `InboundReceipt`、`InboundService.replay` | 串行与并发同键安全重放、同单不同数量和跨单同键用例 | PASS | 取得单据锁后以锁定查询复核批次；重放返回首次快照且库存、批次与审计不重复；冲突返回 `WMS_IDEMPOTENCY_CONFLICT` |
| AC-07：审计/库存失败共同回滚 | `InboundService.receive` | `auditFailureRollsBackTheBatchAndInventory`；`T02InboundInventoryRollbackIntegrationTest` | PASS | 两个失败接缝均保持单据 `CREATED/0`、库存 0，事务内批次和流水随之回滚 |
| AC-08：MySQL 最后数量并发争抢 | `InboundOrderRepository.findLockedById` | `T02InboundMysqlConcurrencyIntegrationTest` | PASS | MySQL 8.0.46 / `REPEATABLE-READ` 下两个终批请求仅一个成功，另一个 409；最终累计与库存均为 10 |
| AC-09：旧调用一次收满 | `InboundService.receive` | `fullPlannedQuantityIsReceivedAndImmediatelyAvailable` | PASS | `RECEIVED/10`，库存 10 |
| AC-10：完成后新键拒绝 | `InboundOrder.receive` | `rejectsInvalidQuantitiesAndNewReceiptsAfterCompletion` | PASS | HTTP 409 / `WMS_INBOUND_STATE`，库存不变 |
| AC-11—12：课堂 UI 两批操作和真实刷新 | `useInboundScenario`、`InboundPage` | TypeScript/Vite 构建；错误路径代码复核；UI/QA 页面复测 | BLOCKED | 弹窗关闭已放入 `finally`，刷新失败会显示错误，构建通过；尚未指定 UI/QA 验收方，不由开发代填 PASS |

## UI 测试/QA 复测

| 验收方 | 范围 | 结论 | 证据 | 时间 |
| --- | --- | --- | --- | --- |
| UI 测试/QA | 页面依次登记 4 与 6，核对三态、0/4/10 库存、完成后禁用、失败后真实刷新 | BLOCKED：验收方待指定 | 待 UI/QA 提供 | 待提供 |

## 未覆盖与剩余风险

- MySQL 专项验证使用本次临时隔离容器执行；常规全量命令不会自动启动该环境，因此其中的 Profile 守卫仍会跳过。
- 旧 `RECEIVED` 数据会在应用对外就绪前由 `InboundReceiptMigration` 幂等回填批次证据，并有迁移测试；大数据量上线前仍需评估启动耗时。
- UI/QA 页面复测未完成，构建通过不代替人工页面验收。
- 本次 findings 修复已固定为提交 `a1501c3e24e723b63fb67ea0595eda2412d36ca7`。

## 结论

`READY_FOR_REVIEW`。Spec/Standards findings 的自动化与 MySQL 证据已补齐；独立 UI/QA 页面复测仍待提供。
