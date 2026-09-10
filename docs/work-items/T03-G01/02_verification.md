# 02 Verification：T03-G01

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2` |
| 被验证分支/提交 | `T03-G01` / `f95105bf4e70e111b91f6e12803d5e79fb12fc2b` |
| Spec/Standards | `spec.md` v1 / APPROVED；`STD-WMS-0.7-04` |
| Docker/Java/Maven/DB 环境 | `compose.classroom.yml`；Java 17.0.15；H2 默认环境；MySQL 8 临时内网容器 + `mysql-verification` Profile；前端 Vite 7.3.6 |

## 测试执行

### Review findings 修复验证（2026-09-10）

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 移库目标回归 | `mvn -pl training-server -am -Dtest=T03TransferContractIntegrationTest,T03TransferAtomicityIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test` | PASS | 8 tests，0 failures/errors/skips；覆盖停用/异仓库位、幂等载荷冲突和末端失败事务回滚 | `evidence/full-verify-20260910.log`（包含同组测试的全量重跑） |
| 前端构建 | `docker compose -f compose.classroom.yml run --rm frontend npm run build` | PASS | TypeScript 和 Vite 构建成功；仅既存 chunk 体积警告 | `evidence/frontend-build-20260910.log` |
| 全量回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -l /workspace/docs/work-items/T03-G01/evidence/full-verify-20260910.log clean verify` | PASS | 52 tests，0 failures/errors，2 skipped；`BUILD SUCCESS` | `evidence/full-verify-20260910.log` |

### 历史实施记录（不作为当前候选交付证据）

以下条目只保留 TDD 和分 Ticket 实施轨迹；其会话输出当时未落盘，当前候选的可复核结论仅以上方证据文件为准。

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 实施前失败证据 | `docker compose -f compose.classroom.yml run --rm classroom mvn -Dtest=T03TransferHappyPathIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test` | FAIL | 2 个用例均期望 200、实际 404；库存准备请求为 200，证明 `/transfer` 缺失 | 未落盘的历史会话输出 |
| 目标测试 | 同上 | PASS | `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| 前端构建 | `docker compose -f compose.classroom.yml run --rm frontend npm run build` | PASS | TypeScript 与 Vite 构建成功；仅有既存 chunk 体积警告 | 未落盘的历史会话输出 |
| business-wms 回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -pl business-wms -am test` | PASS | `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| training-server 回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -pl training-server -am test` | PASS | `Tests run: 43, Failures: 0, Errors: 0, Skipped: 2`；跳过项是需显式 MySQL Profile 的既有守卫 | 未落盘的历史会话输出 |
| 全量验证 | `docker compose -f compose.classroom.yml run --rm classroom mvn clean verify` | PASS | `Tests run: 43, Failures: 0, Errors: 0, Skipped: 2`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| Ticket 02 失败证据 | `docker compose -f compose.classroom.yml run --rm classroom mvn -Dtest=T03TransferContractIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test` | FAIL | 同库位请求期望 400、实际 409；相同键同载荷重放期望 200、实际 409 唯一键冲突 | 未落盘的历史会话输出 |
| Ticket 02 目标测试 | 同上 | PASS | `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| Ticket 02 前端构建 | `docker compose -f compose.classroom.yml run --rm frontend npm run build` | PASS | TypeScript 与 Vite 构建成功；仅有既存 chunk 体积警告 | 未落盘的历史会话输出 |
| Ticket 02 全量验证 | `docker compose -f compose.classroom.yml run --rm classroom mvn clean verify` | PASS | `Tests run: 49, Failures: 0, Errors: 0, Skipped: 2`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| Ticket 03 H2 并发 | `docker compose -f compose.classroom.yml run --rm classroom mvn -Dtest=InventoryConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=false test` | PASS | `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`；反向并发移库无死锁，余额为 9/11、总量 20 | 未落盘的历史会话输出 |
| Ticket 03 MySQL 并发 | 在无宿主机端口的临时 MySQL 8 内网容器中，以 `mysql-verification` Profile 离线执行同一目标测试 | PASS | `MySQL8Dialect`；`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| Ticket 03 business-wms 回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -pl business-wms -am test` | PASS | Reactor 4 个 Module 均成功；`BUILD SUCCESS` | 未落盘的历史会话输出 |
| Ticket 03 前端构建 | `docker compose -f compose.classroom.yml run --rm frontend npm run build` | PASS | TypeScript 与 Vite 构建成功；仅有既存 chunk 体积警告 | 未落盘的历史会话输出 |
| Ticket 03 全量验证 | `docker compose -f compose.classroom.yml run --rm classroom mvn clean verify` | PASS | `Tests run: 50, Failures: 0, Errors: 0, Skipped: 2`；`BUILD SUCCESS` | 未落盘的历史会话输出 |

补充说明：Ticket 原命令使用不存在的 `training-wms-mvn` Compose 服务，首次执行报环境错误，未计为 Red；随后从仓库真实的 `compose.classroom.yml` 解析并使用 `classroom` 服务完成验证。

Ticket 03 的并发测试首次执行即为 GREEN：Ticket 01 已按批准设计实现库位 ID 升序加锁，因此没有为制造 Red 而破坏生产代码。MySQL 首次环境装配因普通课堂缓存缺少驱动且账号不可用而失败，不计为测试 Red；改用仓库既有离线验证镜像与 Maven 缓存后通过。临时 MySQL 容器、内网和 tmpfs 数据已删除，未保留或记录临时口令。

并行执行模块回归与全量验证时，两者共享的构建目录发生清理竞争，首次模块回归在编译阶段被打断，不计为代码失败；全量验证结束后单独重跑模块回归通过。

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01：10 移 4，目标余额自动创建 | `InventoryService.transfer`、`InventoryBalance.transferOut/transferIn` | `movesAvailableInventoryToAnEmptyTargetLocation` | PASS | HTTP 返回源 6、目标 4，两端占用量均为 0 |
| AC-02：全量移库 | `InventoryService.transfer` | `movesAllAvailableInventory` | PASS | 源可用量为 0、目标为 10 |
| AC-13：双流水与一次审计 | `InventoryService.transfer`、`InventoryMovement` | 目标测试审计断言 + 代码差异核对 | PASS | 成功路径固定写入 `TRANSFER_OUT`、`TRANSFER_IN` 各一次并调用一次 `TRANSFER` 审计 |
| AC-14：课堂 UI 自动生成单号并展示结果 | `TransferPage`、`transferInventory`、`AppShell` | TypeScript/Vite 生产构建 | PASS | 普通提交自动使用新单号；页面展示完整成功详情或后端错误 |
| AC-16：刷新后查询已完成任务 | `InventoryOperations.listTransferTasks`、`GET /api/wms/inventory/transfers`、`TransferPage` | `T03TransferHappyPathIntegrationTest` + TypeScript/Vite 生产构建 | PASS | 双流水只读生成任务列表，不新增移库任务表或状态机 |
| AC-03、AC-11：不足与溢出整体回滚 | `InventoryService.transfer`、`InventoryBalance` | `rollsBackInsufficientInventoryAndAllowsRetry`、`rollsBackTargetOverflow` | PASS | 两端余额和审计不变；失败键可再次执行，证明失败未留下幂等流水 |
| AC-04—AC-08：输入、库位和库存错误 | `InventoryTransferRequest`、`InventoryService.transfer` | Ticket 02 参数化场景 | PASS | 返回批准的 400/409 与稳定错误码 |
| AC-09：重放；AC-10：不同载荷冲突 | `InventoryMovement.matches`、`InventoryService.transfer` | `safelyReplaysTheFirstTransferSnapshot`、`rejectsAnIdempotencyKeyWithDifferentPayload` | PARTIAL | 重放与不同载荷冲突已通过；“提交前已有单边流水”无法通过公开业务入口构造，尚无合规自动化接缝 |
| AC-12：反向并发无死锁且总量守恒 | `InventoryService.transfer` 的稳定锁顺序 | `oppositeTransfersCompleteWithoutDeadlockAndPreserveTotalInventory`（H2 + MySQL） | PASS | 两个反向请求均在 10 秒内完成；余额非负，总量保持 20 |

## UI 测试/QA 复测

| 验收方 | 范围 | 结论 | 证据 | 时间 |
| --- | --- | --- | --- | --- |
| 当前用户代理 | Ticket 01：准备 10 件源库存，执行 4 件及全量移库，核对成功结果 | ACCEPTED | 用户明确“验收，进入 Tickets 2” | 2026-09-09 11:02:17 +0800 |
| 当前用户代理 | Ticket 02：失败错误、完整回滚和幂等契约 | ACCEPTED | 用户明确“现在确认Tickets 2，进入 ticket 3” | 2026-09-09 11:27:34 +0800 |
| 当前用户代理 | Ticket 03：并发、MySQL、完整回归和架构文档 | ACCEPTED | 用户原话：“Ticket 03 验收通过，同意形成候选提交并进入 code-review。” | 2026-09-10 00:02:50 +0800 |

## 未覆盖与剩余风险

- Ticket 03 的 H2/MySQL 并发验证已通过；首次即 GREEN，因此没有独立失败测试输出。
- 流水无已批准的公开查询接口；本 Ticket 通过生产代码固定的两次写入和公开结果核对，不为测试扩大契约。
- Ticket 03 已由当前用户代理人工验收；最终交付仍须经过独立双轴评审和人工 Decision。

## 结论

READY_FOR_REVIEW。Ticket 01—03 均已人工验收，允许形成候选提交并进入独立双轴评审。
