# Code Review：T01-G01

## 评审对象

- Diff：`ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482...01412b23ec296f8a88e384d46251ec57a0f0fad8`
- Commit 列表：`01412b23ec296f8a88e384d46251ec57a0f0fad8`；当前分支为 `group-demo/t01`。
- Spec 路径/版本：N/A；边界来自 `01_review.md`、`02_impact.md` 和 `03_agent-task.md`。
- Standards ID/版本：`STD-WMS-0.7-03`。
- 测试证据：Docker 目标测试通过；Module 回归 35/35 通过、1 个 MySQL 环境测试跳过；全量 `clean verify` 同结果。

## Spec 符合性矩阵

| Spec 条目 | 代码符号 | 测试 | 真实结果 | 结论 | 证据路径 |
| --- | --- | --- | --- | --- | --- |
| RESERVED 出库单取消后释放预占库存 | `ShipmentService.cancel` | `WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory` | 库存由 `4/6` 恢复为 `10/0` | PASS | `01_review.md`、`04_verification.md` |
| 复用既有释放入口和取消幂等键 | `InventoryOperations.release` | 目标测试及重复取消回归 | 目标测试和 WmsFlow 回归通过，未新增契约 | PASS | `02_impact.md`、`04_verification.md` |
| 不改变 API、事务、库存算法和其他状态 | `ShipmentController`、`InventoryBalance` 无差异 | Module 回归、全量验证 | Diff 仅恢复一处业务编排调用 | PASS | `02_impact.md`、Git Diff |

## Standards 符合性矩阵

| 规则 ID | 适用性 | 代码符号 | 检查证据 | 例外/人工裁决 | 结论 |
| --- | --- | --- | --- | --- | --- |
| STD-ARCH-01 | MUST | `ShipmentService.cancel` → `InventoryOperations.release` | 继续使用库存唯一公开写入口；Controller 未改 | 无 | PASS |
| STD-OWN-01 | MUST | `business-wms/ShipmentService.cancel` | Diff 仅在批准的业务 Module 和方法内 | 开发负责人已批准 | PASS |
| STD-API-01 | MUST | `ShipmentController.cancel` | HTTP 路径、请求/响应、状态码和错误码未改 | 无 | PASS |
| STD-DATA-01 | MUST | `ShipmentService.cancel`、`InventoryService.release` | 保持既有 `@Transactional`；测试观察单据和库存最终状态 | 无 | PASS |
| STD-DATA-02 | MUST | `InventoryOperations.release` | 复用 RELEASE 幂等键；重复取消回归通过 | 无 | PASS |
| STD-TEST-01 | MUST | `WmsFlowIntegrationTest` | 修复前红灯，修复后目标行为绿灯 | 无 | PASS |
| STD-TEST-02 | MUST | Docker 测试命令 | 目标、回归、全量均执行 | MySQL 环境测试跳过，待有权角色裁决 | BLOCKED |
| STD-DELIVERY-01 | MUST | Work Item 固定产物 | 过程文档已建立；Decision、UI/QA 和平台记录待闭合 | 无 | BLOCKED |

## 未关闭阻塞项

1. MySQL Profile 环境测试跳过，暂无当前版本的 MySQL 专项证据。
2. UI 测试/QA 尚未提供人工复测结论。
3. `06_decision.md` 尚未由人工裁决为 `ACCEPTED`。
4. GitHub remote、讲师目标分支、提交和 PR 链接尚未确定。

## Agent 建议

READY_FOR_DECISION

> 本文件不是人工放行决定；正式裁决写入 `06_decision.md`。
