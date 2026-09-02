# 04 Verification：T01-G01

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482` |
| 被审提交/工作树状态 | `group-demo/t01`；`01412b23ec296f8a88e384d46251ec57a0f0fad8` |
| Spec 路径/版本 | N/A；规则和公开契约未变化 |
| Standards ID/版本 | `STD-WMS-0.7-03` |
| 环境 | Docker `training-wms-classroom:0.7`，`linux/amd64`，H2，Java 17，Maven 离线缓存 |

## 测试执行

| 层级 | 命令 | 通过/失败/跳过 | 真实结果 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前目标测试 | 目标测试命令，修复前工作树 | 失败 | 1 个测试失败：实际 `4/6`，期望 `10/0` | `01_review.md` |
| 目标测试 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am -Dtest=WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory -Dsurefire.failIfNoSpecifiedTests=false test` | PASS | 1 个测试，0 失败、0 错误、0 跳过；日志含 `RELEASE type=INVENTORY` | 本轮 Docker 输出 |
| Module 回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am test` | PASS（含环境跳过） | 35 个测试，0 失败、0 错误、1 个 MySQL 环境测试跳过 | 本轮 Docker 输出 |
| 全量验证 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp clean verify` | PASS（含环境跳过） | 35 个测试，0 失败、0 错误、1 个 MySQL 环境测试跳过；构建成功 | 本轮 Docker 输出 |

## 验收映射

| 验收条件 | 代码符号 | 测试 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01 取消 RESERVED 单据后为 `CANCELLED` | `ShipmentService.cancel` | 目标测试 | 接口成功返回 `CANCELLED` | PASS |
| AC-02 取消后库存为 `10/0` | `ShipmentService.cancel` → `InventoryOperations.release` | 目标测试 | 目标测试通过，释放日志已出现 | PASS |
| AC-03 不改变 API 和库存算法 | `ShipmentController`、`InventoryBalance.release` 无差异 | Module 回归、全量验证 | 差异仅为取消编排调用恢复 | PASS |

## Standards 结果

- 架构检查：PASS；库存变化仍通过 `InventoryOperations`，Controller 不访问 Repository，未新增依赖。
- Clean Code Skills 及版本：N/A；仅恢复既有一行调用，无新增结构问题。
- 测试检查：PASS；目标测试先红后绿，回归和全量验证无失败。
- 文档检查：待补充 PR、UI/QA 复测和 Decision 平台证据。
- 未关闭阻塞项：UI/QA 复测、正式 Decision、讲师目标分支、GitHub remote/PR 信息。

## 未覆盖与剩余风险

- MySQL Profile 环境测试跳过，未获得 MySQL 专项运行证据。
- 页面复测尚未由 UI 测试/QA 出具；自动化测试不能替代人工验收。
- 已创建本地提交 `01412b23ec296f8a88e384d46251ec57a0f0fad8`；尚未推送分支或创建 GitHub PR。

## UI 测试/QA 复测验收

| 验收方 | 复测范围 | 结论 | 证据路径 | 时间 |
| --- | --- | --- | --- | --- |
| UI 测试/QA | 新建验证单据后取消，确认 `CANCELLED`、可用 `10`、预占 `0`；检查接口和回归结果 | 待复测 | 待提供 | 待提供 |

## 结论

READY_FOR_REVIEW
