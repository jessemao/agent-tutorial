# 02 Verification：T04-G01

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `s2-t04-start` / `ae996fe51322f76dd4605de66c6a47cd84ee230b` |
| 被验证分支/提交 | `T04-G01` / 候选提交 `f49448795a114b32d242442f15c66db2da66c27e`；2026-09-10 21:09—21:14 +0800 在该提交上重新执行最终验证 |
| Spec/Standards | `spec.md` v1 / APPROVED；`STD-WMS-0.7-06` / 起点版本 |
| Docker/Java/Maven/DB 环境 | `compose.classroom.yml` 的 `classroom` 服务；Eclipse Temurin Java 17.0.15；Maven 3.9.9；默认 H2 测试环境 |

## 测试执行

### Ticket 01：扩展统一库存 Interface（2026-09-10）

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 实施前行为基线 | `./scripts/classroom-test.sh T04 baseline` | PASS | 35 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:21—20:22 +0800 |
| 红灯或替代验证 | 核对 `InventoryOperations` 与 `InventoryService` 的既有方法签名 | PASS | Implementation 已预先存在同签名方法，单独增加 Interface 声明不会自然编译失败；采用同组基线前后对照和结构 Diff，不伪造 Red | `InventoryOperations.java`、`InventoryService.java` 及本记录 |
| 目标结构检查 | `git diff -- business-wms/src/main/java/com/acme/training/wms/inventory/InventoryOperations.java business-wms/src/main/java/com/acme/training/wms/inventory/InventoryService.java` | PASS | 仅新增 2 个 Interface 方法与 2 个 `@Override`；共 6 行新增，无方法体变化 | 当前工作树 Diff |
| Module 回归 | `docker compose -f compose.classroom.yml exec -T classroom /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl business-wms -am test` | PASS | Reactor 4 个 Module 成功；3 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:23 +0800 |
| T04 保护回归 | `./scripts/classroom-test.sh T04 baseline` | PASS | 35 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:23—20:24 +0800 |
| 全量验证 | `mvn clean verify` | N/A | 按批准的 Ticket 顺序留到 Ticket 03 最终收缩阶段执行；Ticket 01 要求为 Module 与 T04 baseline | `tickets/03-contract-legacy-transfer-seam.md` |

测试日志中出现的唯一键和字段长度 SQL ERROR 是并发测试主动触发并断言的失败场景；对应测试均通过，不是构建失败。

### Ticket 02：迁移库存调用方（2026-09-10）

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 红灯或替代验证 | 核对三个调用方在迁移前均依赖旧 Interface | PASS | 本 Ticket 只把依赖类型切换到 Ticket 01 已扩展的 Seam，不改变行为，无法构造不篡改需求的自然 Red；使用同一公开行为基线和源码结构搜索验证 | 当前工作树 Diff 与本记录 |
| 调用方结构检查 | `git diff -- InventoryController.java T03TransferContractIntegrationTest.java InventoryConcurrencyTest.java` | PASS | 3 个文件共 7 行新增、19 行删除；仅移除旧 Interface 注入并改用既有 `InventoryOperations` 变量，断言、数据与业务实现未变 | 当前工作树 Diff |
| 旧 Seam 消费方搜索 | `rg -n "InventoryTransferOperations|LegacyInventoryTransferService" business-wms training-server` | PASS | Java 源码仅剩 `InventoryTransferOperations` 定义和 `LegacyInventoryTransferService` Adapter；另有 `business-wms/AGENTS.md` 的治理说明，Controller 与测试无消费方 | 当前实施会话原始输出 |
| T04 保护回归 | `./scripts/classroom-test.sh T04 baseline` | PASS | 35 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:38 +0800 |
| Diff 健康检查 | `git diff --check` | PASS | 无空白或补丁格式错误 | 当前实施会话原始输出 |

### Ticket 03：收缩遗留移库 Seam（2026-09-10）

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 删除前结构证据 | `rg -n "InventoryTransferOperations|LegacyInventoryTransferService" business-wms/src training-server/src/test --glob '*.java'` | PASS | 仅命中旧 Interface 自身和旧 Adapter 的定义/实现；无生产或测试调用方 | 当前实施会话原始输出 |
| 红灯或替代验证 | 同一 T04 公开行为保护集 + 删除后零引用搜索 | PASS | 本 Ticket 删除零调用死代码并同步架构事实，不新增行为；以结构搜索和重构前后相同测试证明目标，不伪造 Red | 当前工作树、T04 baseline 与本记录 |
| 删除后结构证据 | 同一 Java 源码搜索 | PASS | 退出码 1 且输出为空，证明旧 Interface/Adapter 在 Java 生产与测试源码中为零 | 当前实施会话原始输出 |
| T04 保护回归 | `./scripts/classroom-test.sh T04 baseline` | PASS | 35 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:50:56 +0800 |
| 受影响 Module 回归 | `docker compose -f compose.classroom.yml exec -T classroom /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl business-wms -am test` | PASS | Reactor 4 个 Module 成功；3 tests，0 failures、0 errors、0 skipped；`BUILD SUCCESS` | 当前实施会话原始输出，2026-09-10 20:51:24 +0800 |
| 前端生产构建 | `docker compose -f compose.classroom.yml exec -T frontend npm run build` | PASS | TypeScript 与 Vite 构建成功；3973 modules transformed；仅有既存大 chunk 警告 | 当前实施会话原始输出，2026-09-10 20:51 +0800 |
| 项目全量验证 | `docker compose -f compose.classroom.yml exec -T classroom /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify` | PASS | 55 tests，0 failures、0 errors、2 skipped；`BUILD SUCCESS`；其中 2 项为未启用 `mysql-verification` Profile 时按条件跳过的环境专项测试 | 当前实施会话原始输出与 Surefire XML，2026-09-10 20:53:37 +0800 |
| 健康检查 | `curl -fsS http://localhost:8080/actuator/health` | PASS | `{"status":"UP"}` | 当前实施会话原始输出，2026-09-10 20:53 +0800 |
| 范围与 Diff 检查 | 起点 Diff、源码搜索、`git diff --check` | PASS | 无 POM、数据库、HTTP DTO/路径、业务方法体或测试断言变化；架构文档补齐现行任务列表方法 | 当前工作树 Diff |

### 固定候选复验与持久证据（2026-09-10）

| 检查 | 候选版本 | 结果 | 持久证据 |
| --- | --- | --- | --- |
| 生产与测试目录相对候选无 Diff；旧 Seam Java 零引用 | `f49448795a114b32d242442f15c66db2da66c27e` | PASS | `artifacts/candidate-and-zero-reference-f494487.log` |
| T04 baseline | 同上 | 35 tests，0 failures、0 errors、0 skipped；BUILD SUCCESS | `artifacts/t04-baseline-f494487.log` |
| business-wms Module 回归 | 同上 | 3 tests，0 failures、0 errors、0 skipped；BUILD SUCCESS | `artifacts/module-regression-f494487.log` |
| 前端生产构建 | 同上 | 3973 modules transformed；构建成功；既存大 chunk 警告 | `artifacts/frontend-build-f494487.log` |
| 项目全量验证 | 同上 | 合计 55 tests，0 failures、0 errors、2 个 MySQL Profile 条件跳过；BUILD SUCCESS | `artifacts/mvn-clean-verify-f494487.log` |
| 服务健康检查 | 同上 | `{"status":"UP"}` | `artifacts/health-f494487.log` |

日志索引、执行窗口与 SHA-256 见 `artifacts/README.md`。上述复验取代各 Ticket 中“当前实施会话原始输出”作为固定候选的最终交付证据；早期记录仅保留分阶段实施历史。

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| Ticket 01-AC-01：统一 Interface 暴露移库与任务查询 | `InventoryOperations.transfer`、`InventoryOperations.listTransferTasks` | 目标结构 Diff + Module 编译 | 方法签名与批准的 `interface.md` 一致 | PASS |
| Ticket 01-AC-02：现有实现显式满足新增契约且方法体不变 | `InventoryService.transfer`、`InventoryService.listTransferTasks` | 目标结构 Diff + T04 baseline | 只增加 `@Override`，事务注解、方法体与调用顺序未变 | PASS |
| Ticket 01-AC-03：保留旧 Interface、Adapter 与调用方 | `InventoryTransferOperations`、`LegacyInventoryTransferService`、`InventoryController`、两个直接测试调用方 | Java 源码搜索 | 旧类型和现有调用仍存在，未执行迁移或删除 | PASS |
| Ticket 01-AC-04：35 项保护测试保持 | T04 baseline 六组公开行为测试 | 实施前与实施后运行同一命令 | 前后均 35 tests，0 failures、0 errors、0 skipped | PASS |
| Ticket 01-AC-05：可独立回退 | 本 Ticket 6 行新增 | `git diff --check` 与 Diff 核对 | 删除两个声明和两个 `@Override` 即可回退，无数据处理 | PASS |
| Ticket 02-AC-01：Controller 只使用统一 Interface | `InventoryController` | 结构 Diff + T04 baseline | 构造器只注入 `InventoryOperations`，移库与列表均委托给它 | PASS |
| Ticket 02-AC-02：直接测试调用方迁移且断言不变 | `T03TransferContractIntegrationTest`、`InventoryConcurrencyTest` | 结构 Diff + T04 baseline | 两个测试类复用已有 `InventoryOperations` 注入，测试场景与断言无变化 | PASS |
| Ticket 02-AC-03：没有无关业务 Diff | Inbound/Shipment/Stocktake 及其他调用方 | 限定 Diff 核对 | 本 Ticket 仅修改指定 Controller 和两个测试类 | PASS |
| Ticket 02-AC-04：旧 Seam 无消费方 | `InventoryTransferOperations`、`LegacyInventoryTransferService` | Java 源码搜索 | 只剩定义和 Adapter，留作 Ticket 03 收缩及回滚点 | PASS |
| Ticket 02-AC-05：公开行为保持 | T04 baseline 六组保护测试 | 迁移后运行规定命令 | 35 tests 全部通过 | PASS |
| Ticket 02-AC-06：可独立回退 | 三个调用方迁移 Diff | Diff 核对 | 可仅恢复旧 Interface 注入及调用，不涉及数据迁移 | PASS |
| AC-01：只保留统一库存公开 Interface | `InventoryOperations`、已删除旧 Interface/Adapter | 删除前后搜索 + 编译/启动 | `transfer` 与 `listTransferTasks` 均由唯一公开 Seam 暴露，旧类型零引用并删除 | PASS |
| AC-02：其他库存能力保持 | Inbound/Shipment/Stocktake/余额入口 | T04 baseline + 全量验证 | 公开回归全部通过，相关业务调用方无 Diff | PASS |
| AC-03：正常及全量移库保持 | `InventoryOperations.transfer`、HTTP 移库入口 | T03 Happy Path / T04 baseline | 源减、目标增、占用与总量行为测试保持通过 | PASS |
| AC-04：失败错误及最终状态保持 | 移库失败路径 | T03 Contract/Atomicity / T04 baseline | 非法请求、库位、源余额、不足、溢出及回滚测试保持通过 | PASS |
| AC-05：幂等重放与冲突保持 | 移库幂等路径 | T03 Contract/Atomicity / T04 baseline | 首次快照、同载荷重放、冲突及记录数断言保持通过 | PASS |
| AC-06：并发不变式保持 | 反向移库并发路径 | `InventoryConcurrencyTest` / T04 baseline | 相关并发测试通过；默认 H2，MySQL 专项留待对应 Profile 独立执行 | PASS |
| AC-07：任务列表保持 | `listTransferTasks`、`GET /transfers` | T03 Contract / T04 baseline | 列表内容、字段和排序断言保持通过，未新增任务表 | PASS |
| AC-08：三阶段可逆且最终验证通过 | Ticket 01—03 总 Diff | 分阶段证据 + Module/前端/全量/健康检查 | 各阶段范围受控；最终构建与健康检查通过，无数据迁移 | PASS |

## 开发交接

- 开发人员 / Ticket / TDD 红绿版本 / 执行时间：实施 Agent；Ticket 01；起点 `ae996fe5` 与当前未提交工作树；2026-09-10 20:21—20:25 +0800。
- 开发负责人确认：ACCEPTED；2026-09-10 20:30:58 +0800；人工原话：“验收完成”；允许进入 Ticket 02。
- Ticket 02 开发人员 / 版本 / 执行时间：实施 Agent；基于起点 `ae996fe5` 的当前未提交工作树；2026-09-10 20:31—20:38 +0800。
- Ticket 02 开发负责人确认：ACCEPTED；2026-09-10 20:43:11 +0800；人工原话：“确认，进入 tickets 3”；允许进入 Ticket 03。
- Ticket 03 开发人员 / 版本 / 执行时间：实施 Agent；基于起点 `ae996fe5` 的当前未提交工作树；2026-09-10 20:44—20:54 +0800。
- Ticket 03 开发负责人确认：ACCEPTED；2026-09-10 20:54:19 +0800；人工原话：“测试完成没问题。 /code-review T04-G01”；允许固定候选提交并进入代码评审。
- 独立测试入口：`functional-test.md`（完整需求代码评审后由测试工程师在对应阶段生成）
- QA 审核入口：`qa-review.md`（独立功能测试后由不同团队 QA 生成）
- 业务验收入口：`04_decision.md`（业务人员作出决定后生成）

本文件仅记录开发完成当前 Ticket 的结构验证与必要回归，不证明独立功能测试、QA 或业务验收通过。

## 未覆盖与剩余风险

- 默认 H2 全量验证按设计跳过 2 个只在 `mysql-verification` Profile 生效的环境专项测试；不能据此声称 MySQL 并发语义已在本阶段复测。
- 前端构建有既存的单个大 chunk 警告，但构建成功；本重构没有前端源码或依赖 Diff。
- 候选代码已固定为 `f49448795a114b32d242442f15c66db2da66c27e`；复验期间生产与测试目录相对该提交零 Diff，持久日志及校验值位于 `artifacts/`。

## 结论

TICKET_03_ACCEPTED：Ticket 03 的批准范围已实施、通过规定验证并获开发负责人验收；允许固定候选提交并进入代码评审，不代表 T04-G01 整体完成或通过。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| Ticket 01 当前工作树的 Interface 扩展和行为保持证据 | 开发负责人 | 当前用户代理 | CONFIRMED | ACCEPTED；2026-09-10 20:30:58 +0800；人工原话：“验收完成” |
| Ticket 02 当前工作树的调用方迁移和行为保持证据 | 开发负责人 | 当前用户代理 | CONFIRMED | ACCEPTED；2026-09-10 20:43:11 +0800；人工原话：“确认，进入 tickets 3” |
| Ticket 03 当前工作树的旧 Seam 收缩和最终开发验证证据 | 开发负责人 | 当前用户代理 | CONFIRMED | ACCEPTED；2026-09-10 20:54:19 +0800；人工原话：“测试完成没问题。 /code-review T04-G01” |
| 完整需求独立功能测试的固定候选版本 | 测试工程师 | 待指定 | PENDING | 代码评审后执行 |
| 独立 QA 审核对象与团队 | QA 人员 | 待指定 | PENDING | 独立测试后执行 |

仅记录本阶段实际证据及有权人员的原文结论；缺席或未知负责人保留待确认，开发验证不代替独立功能测试、QA 或业务验收。
