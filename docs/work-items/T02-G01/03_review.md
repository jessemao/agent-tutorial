# 03 Review：T02-G01

## 被审对象

- 代码差异起点与被审版本：`91af1d6fa4f4851129d660624691136d4cb27697` → findings 修复提交 `a1501c3e24e723b63fb67ea0595eda2412d36ca7`。
- `01_analysis.md`：`docs/work-items/T02-G01/01_analysis.md`
- `02_verification.md`：`docs/work-items/T02-G01/02_verification.md`
- Spec/Standards：`spec.md` v1、`design.md`、`interface.md`、`STD-WMS-0.7-03`、根及受影响 Module `AGENTS.md`、`docs/ai-governance/standards/`。
- 使用的 Skills 与版本：`code-review` 锁定哈希 `caa9a086...`；`clean-*` 来源提交 `1b6b3cc1264b8fbe921c65002d05a3bf90ede178`；两轴由独立子 Agent 并行检查。

## Spec 符合性矩阵

| 要求 | 代码符号 | 测试 | 真实结果 | 证据 | 结论 |
| --- | --- | --- | --- | --- | --- |
| AC-01—04、09—10：4+6、超收、输入边界、一次收满、完成后拒绝 | `InboundOrder.receive`、`InboundService.receive` | T02 acceptance/baseline | H2 目标与回归通过 | `02_verification.md:27-34` | PASS |
| AC-05：安全重放返回首次快照 | `InboundService.receive` | 串行及并发同键重放 | 取得单据锁后锁定复核批次，并发同键返回同一 200 快照 | P-01 | PASS |
| 旧 `RECEIVED` 数据启用前建立批次证据 | `InboundReceiptMigration` | `InboundReceiptMigrationTest` | 应用就绪前幂等回填缺失批次 | P-02 | PASS |
| AC-08：MySQL 最后数量并发争抢 | `findLockedById` | MySQL 8 Profile | MySQL 8.0.46 / `REPEATABLE-READ` 专项测试通过 | P-03 / S-01 | PASS |
| AC-11—12：UI 4+6、关弹窗、刷新真实数据 | `useInboundScenario`、`InboundPage` | 构建；错误路径复核；UI/QA 复测 | 刷新失败也关闭弹窗并显示错误；独立 UI/QA 复测仍待提供 | P-04 | PASS（代码） |

## Standards 符合性矩阵

| 规则 ID | 适用性 | 代码符号 | 检查证据 | 例外 | 结论 |
| --- | --- | --- | --- | --- | --- |
| Testing §4—6：MySQL 语义必须专项验证，必需场景跳过时阻断 | `MUST` | `02_verification.md` | 独立 MySQL 8 / `REPEATABLE-READ` 环境断言与并发测试均通过 | 无 | PASS |
| Testing §3；clean-tests T1/T6：幂等和回滚证据完整 | `MUST` | T02 acceptance/rollback tests | 新增并发重放、审计不重复、库存失败回滚断言 | 无 | PASS |
| clean-names N4：名称必须准确表达行为 | `REVIEW` | `receiveNextBatch`、`onReceiveNextBatch` | 名称现与多批次行为一致 | 无 | PASS |
| clean-general G5 / Fowler Duplicated Code | `REVIEW` | `PersistenceConstraints.hasName` | 两个服务复用同一最小约束名识别机制 | 无 | PASS |
| Architecture §3—4；AI Security §1—7 | `MUST` | 全部被审差异 | 依赖方向、Controller 边界、库存入口、外部操作和敏感信息检查未见违规 | 无 | PASS |

## Findings

### Spec 轴

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| P-01 | BLOCKER | `InboundService.java` | 根因已修复：取得单据锁后通过锁定查询复核批次；并发同键重放测试由 409 红灯转为 200 绿灯。 | 已落实。 | RESOLVED |
| P-02 | BLOCKER | `InboundReceiptMigration.java` | 新增应用就绪前的幂等回填及测试，为旧 `RECEIVED` 单据建立批次证据。 | 已落实。 | RESOLVED |
| P-03 | MAJOR | `T02InboundMysqlConcurrencyIntegrationTest.java` | 隔离 MySQL 8.0.46、`REPEATABLE-READ` 环境中完成终批并发争抢测试，最终累计和库存均不超过计划数。 | 已落实。 | RESOLVED |
| P-04 | MAJOR | `useInboundScenario.ts`；`InboundPage.tsx` | 刷新失败进入可见错误状态，弹窗关闭由 `finally` 保证，不再依赖刷新成功。 | 已落实。 | RESOLVED |

### Standards 轴

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| S-01 | BLOCKER | `02_verification.md` | 已补齐真实 MySQL 8 / `REPEATABLE-READ` 证据；常规全量中的环境守卫跳过不再替代专项结果。 | 已落实。 | RESOLVED |
| S-02 | MAJOR | T02 rollback tests | 新增并发重放不重复审计断言，以及库存操作失败后的单据、批次、流水和库存共同回滚测试。 | 已落实。 | RESOLVED |
| S-03 | MINOR | `useInboundScenario.ts`；`InboundPage.tsx` | 已改为 `receiveNextBatch` / `onReceiveNextBatch`。 | 已落实。 | RESOLVED |
| S-04 | MINOR | `PersistenceConstraints.java` | 将异常链与约束名识别提取为单一最小技术工具，两个服务共用。 | 已落实。 | RESOLVED |

## 问题复盘

- 直接原因：实施只验证了串行 H2 主路径，没有把“并发重放”、“启用前迁移”、“刷新再失败”和全部失败接缝落成验收证据。
- 为什么原有检查没有更早发现：目标测试把幂等验证限于提交完成后的串行重放，全量脚本又不启动 MySQL 专项环境。
- 哪道门禁可以提前发现：在红灯阶段将 AC-05/AC-08 拆成并发同键重放和最后数量争抢两个 MySQL 用例；对 AC-07/AC-12 逐个故障点列表。
- 防止重复的具体动作、负责人和期限：实施负责人在下次候选评审前修复 P-01/P-02/P-04 与 S-02/S-03，补齐 P-03/S-01 证据；Standards 所有者在同一裁决点处理 S-04 是否接受。

## Agent 建议

`READY_FOR_REVIEW`

> 原评审的 Spec 轴 4 项与 Standards 轴 4 项均已处理并获得自动化证据；独立 UI/QA 页面复测仍按 `02_verification.md` 保持待提供。本文件不改变 `04_decision.md` 中既有人工结论。
