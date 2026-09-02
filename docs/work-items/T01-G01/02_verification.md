# 02 Verification：T01-G01

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码差异起点 | `03dd2ceda73e5bd9826b1d1a7fce824816071334` |
| 被验证分支/提交 | `main` / `1635a6ebbff0d631c21db9aeae7101b5bf50a020` |
| Spec/Standards | 无独立 Spec；依据 `01_analysis.md`；`STD-WMS-0.7-03` |
| Docker/Java/Maven/DB 环境 | `compose.classroom.yml`；Docker 课堂容器；Java 17.0.15；Maven 离线模式；H2 默认环境 |

本次代码差异仅修改 `business-wms/src/main/java/com/acme/training/wms/outbound/ShipmentService.java` 的 RESERVED 取消分支；未修改测试文件、配置、公开契约、事务边界或其他模块。

## 测试执行

| 层级 | 命令或步骤 | 结果 | 真实输出摘要 | 证据路径 |
| --- | --- | --- | --- | --- |
| 修复前失败证据 | 同一目标测试命令，在起点提交 `03dd2ceda73e5bd9826b1d1a7fce824816071334`、修复前工作树执行 | FAIL | `Tests run: 1, Failures: 1, Errors: 0, Skipped: 0`；实际 `availableQuantity=4`、`reservedQuantity=6`，`WmsFlowIntegrationTest.java:45` 期望可用量 `10` | `inputs/browser-failure.md`；本次 Docker 执行输出 |
| 目标测试 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am -Dtest=WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory -Dsurefire.failIfNoSpecifiedTests=false test` | PASS | `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`；日志出现 `RELEASE type=INVENTORY id=SO-101` | 本次 Docker 执行输出 |
| Module 回归 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp -pl training-server -am test` | PASS | `Tests run: 35, Failures: 0, Errors: 0, Skipped: 1`；跳过项为 `MysqlVerificationEnvironmentTest` 的环境守卫 | 本次 Docker 执行输出 |
| 全量验证 | `docker compose -f compose.classroom.yml run --rm classroom mvn -o -B -ntp clean verify` | PASS | `BUILD SUCCESS`；`Tests run: 35, Failures: 0, Errors: 0, Skipped: 1`；跳过项为 `MysqlVerificationEnvironmentTest` 的环境守卫 | 本次 Docker 执行输出 |

## 验收映射

| 验收条件 | 代码符号 | 测试/复测 | 结果 | 结论 |
| --- | --- | --- | --- | --- |
| AC-01：RESERVED 出库单取消后状态为 `CANCELLED`，库存可用量恢复为 `10`、预占量为 `0` | `ShipmentService.cancel` | `WmsFlowIntegrationTest.cancellingReservedShipmentReleasesInventory` | PASS | 自动化目标测试通过 |
| AC-02：取消释放经过现有库存操作并产生 RELEASE 流水，不重复改变其他出库路径 | `ShipmentService.cancel`、`InventoryService.release` | 目标测试；Module 回归；全量验证 | PASS | 目标测试日志出现 RELEASE，回归无失败 |
| AC-03：不改变公开 API、配置、事务边界和其他业务路径 | `ShipmentService.cancel` 差异 | Git 差异核对；Module 回归；全量验证 | PASS | 仅恢复一处既有调用，未修改测试文件或其他模块 |

## UI 测试/QA 复测

| 验收方 | 范围 | 结论 | 证据 | 时间 |
| --- | --- | --- | --- | --- |
| UI 测试/QA | 使用修复后的服务执行 T01 页面取消流程，确认单据状态、可用量、预占量和异常提示 | 待复测；不得由开发代填 PASS | 待 UI 测试/QA 提供 | 待提供 |

## 未覆盖与剩余风险

- MySQL 专项环境测试因课堂环境守卫跳过；当前结果只证明 Docker H2 环境行为，是否补跑或接受例外待有权角色决定。
- UI 页面复测尚未执行或尚未提供证据，自动化接口测试不能替代页面验收。
- 验证命令在提交前的工作树执行；提交 `1635a6ebbff0d631c21db9aeae7101b5bf50a020` 只包含同一代码差异和过程文档，提交后已核对差异范围，仍需在后续 MR/PR 中以该提交作为被审版本。
- 本修复不自动清理历史已残留的错误库存余额；历史数据恢复需另行批准。

## 结论

`READY_FOR_REVIEW`，但在 UI 测试/QA 复测结论和 MySQL 跳过项裁决补齐前，不得声明最终交付通过。
