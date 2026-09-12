# Ticket 05：完整链路验收环境

## 状态

完整链路已在隔离仓库执行并由人验收为 ACCEPTED；详细收口结果见 `ticket-05-full-chain-result.md`。

## 固定环境

| 字段 | 内容 |
| --- | --- |
| 临时仓库 | `/tmp/t05-full-chain-acceptance.HMpYcT/repo` |
| 操作说明 | `/tmp/t05-full-chain-acceptance.HMpYcT/ACCEPTANCE.md` |
| 分支 | `T05-FULL-CHAIN-ACCEPTANCE` |
| 基线提交 | `bfba7413ed5fc95a34b8cd0110fbc4da48b01795` |
| 临时 Work Item | `T05-G91`；初始目录不存在 |
| 初始 Git 状态 | clean |
| 五 Skill 门禁 | PASS |
| AI Governance 门禁 | PASS |

## 验收场景

目标是在隔离仓库新增一个项目级、显式调用、只读的 `work-item-status` Skill，用于报告指定 Work Item 的当前状态、Git/对象版本、未决事项和唯一合法下一入口。实现细节、输出字段与错误边界在初始输入中保持未知，必须经 `work-item-discover`、真实 `grill-with-docs` 问答和人工共同理解确认后才能进入设计。

链路依次验证：

1. `work-item-start` 只建档。
2. `work-item-discover` 自主调查后执行多轮 `grill-with-docs`。
3. 专业设计/Spec 与人工范围批准。
4. `work-item-execute` 只完成当前切片并停在验收点。
5. 人工授权形成固定候选后，`work-item-review` 执行双轴评审并落盘。
6. T05 不修改业务代码，独立业务功能测试与 QA 经人工裁剪为 N/A；保留 Skill 开发验证、双轴 Review 和业务/治理验收。
7. `work-item-decision` 保真记录人工决定，并停在有权限的人合并前。

同时抽查缺批准的 execute、漂移候选的 review、缺 QA 的 decision，以及 ACCEPTED 后不自动合并。

临时目录可能由系统清理；实际执行完成后，应将各阶段日志和最终文件清单复制为本 Work Item 的持久 artifacts。
