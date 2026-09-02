# 07 Delivery：T01-G01

## 最终交付

- 分支/提交：`group-demo/t01`；代码提交 `01412b23ec296f8a88e384d46251ec57a0f0fad8`，交付文档提交 `8604c70`（完整哈希见 Git 历史）。
- MR/PR 链接：待创建；当前仓库未发现 Git remote。
- 目标分支：讲师指定目标分支待提供；不得猜测。
- MR/PR 审核与检查结果：尚未创建；需关联 `T01-G01`，列出代码差异、验证结果和未解决风险。
- Diff 起点与被审版本：`ef7e5899ebb818dd82d1a87c6c0b58a10cb7e482...01412b23ec296f8a88e384d46251ec57a0f0fad8`。
- 已批准 Spec 路径/版本：N/A；边界由 `01_review.md`、`02_impact.md`、`03_agent-task.md` 和开发负责人批准记录定义。
- Standards ID/版本：`STD-WMS-0.7-03`。
- 已批准例外 ID（无则写无）：无；MySQL 跳过尚未形成批准例外。
- Decision 路径与结论：`06_decision.md`；`WAITING_FOR_DELIVERY_DECISION`。

## 测试与评审证据

- 目标测试：Docker 通过，`WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory` 1/1，0 失败。
- Module 回归：Docker 通过，35 个测试，0 失败、0 错误、1 个 MySQL 环境测试跳过。
- 全量验证：Docker `clean verify` 通过，35 个测试，0 失败、0 错误、1 个 MySQL 环境测试跳过。
- Code Review：`code-review.md`；Spec 轴 PASS，Standards 轴受 MySQL 跳过和交付证据阻塞，建议 `READY_FOR_DECISION`。
- Clean Code Skills 与版本：N/A；本次仅恢复既有调用，不引入新结构。
- UI 测试/QA 复测：待复测；自动化测试不能代替页面验收。

## 部署、演示与回滚

- 启动/演示步骤：由讲师在 Docker 课堂环境启动当前分支；使用新验证单据执行取消，刷新库存明细确认 `CANCELLED / 10 / 0`，不使用修复前历史异常单据作为唯一证据。
- 回滚点与命令：代码回滚点为 `01412b23ec296f8a88e384d46251ec57a0f0fad8`；有权限人员批准后使用 `git revert 01412b23ec296f8a88e384d46251ec57a0f0fad8`，不重写共享历史。交付文档提交 `8604c70` 不包含业务代码。
- 数据恢复：测试使用临时 H2 容器；网页验收需重置/重建课堂场景，修复不会自动纠正历史库存残留。

## 剩余风险与后续动作

| 风险/动作 | 负责人 | 截止时间 | 状态 |
| --- | --- | --- | --- |
| UI 测试/QA 复测页面、接口和回归结果 | UI 测试/QA | 待提供 | OPEN |
| 补跑 MySQL Profile 或记录有权角色批准的环境例外 | 开发负责人/交付负责人 | 待提供 | OPEN |
| 提供讲师目标分支、配置 remote、创建并审核 GitHub PR | 课程讲师/交付负责人 | 待提供 | OPEN |
| 完成 `06_decision.md` 正式裁决 | 课程讲师/交付负责人 | 待提供 | OPEN |

## 最终确认

- 放行人/角色：待课程讲师/交付负责人确认。
- 结论：BLOCKED
- 时间：待确认。
