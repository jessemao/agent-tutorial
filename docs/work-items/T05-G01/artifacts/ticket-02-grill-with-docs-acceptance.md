# Ticket 02：`grill-with-docs` 问答关口验收

## 结论

**PASS**。使用真正的 T04 起点 `s2-t04-start` 建立隔离 Git 环境，验证 `work-item-discover` 在业务重构中先调查仓库事实，再通过三轮 `grill-with-docs` 人工问答收敛范围，最后单独取得共同理解确认。确认前没有进入 `codebase-design`、Spec 或实施。

## 验收环境

| 字段 | 内容 |
| --- | --- |
| 隔离仓库 | `/tmp/t05-ticket02-grill-t04.eAPGzL/repo` |
| 分支 | `T04-DISCOVER-ACCEPTANCE` |
| 代码起点 | `bfa04129ae6a134e3d57cc85eacc87306597d0ec`（`s2-t04-start`） |
| 临时 Work Item | `T04-G84` |
| 行为记录 | `docs/work-items/T04-G84/01_analysis.md` |

## 仓库自主调查

Agent 在提问前确认：

- `InventoryOperations` 尚未包含移库和任务查询。
- `InventoryTransferOperations` 单独暴露两个操作。
- `LegacyInventoryTransferService` 只是向 `InventoryService` 透传。
- `InventoryController`、`InventoryConcurrencyTest` 和 `T03TransferContractIntegrationTest` 是真实调用方。
- Module 规则把该路径定义为 T04 受控遗留例外，并要求在行为不变前提下消除。

以上事实没有要求用户查找或提供。

## 三轮人工问答

### Round 1

1. 遗留 Interface/Adapter 是否属于必须保留的兼容契约。
2. 外部行为不变是否覆盖 HTTP、错误、库存、事务、幂等、流水、并发和任务查询。

人工原话：`全部同意推荐`。

### Round 2

3. 目标 `InventoryOperations` 是否同时承载移库与任务查询。
4. 是否迁移仓库检出的全部真实调用方。
5. 是否采用先固定行为、再迁移、最后删除零调用遗留类型的顺序及提交回退方式。

人工原话：`全部同意推荐`。

### Round 3

6. 是否采用限定的允许/禁止修改边界。
7. 是否要求同一起点前后执行相同目标、Module 和全量验证且不得弱化测试。
8. 架构文档是否只同步结构事实。

人工原话：`全部同意推荐`。

### 共同理解确认

Agent 汇总全部范围后单独询问是否已形成完整共同理解。人工原话：`同意`。

## 阶段行为

- 问题未关闭时：保持 `WORK_ITEM_SCOPE_APPROVAL_REQUIRED` 或 `WORK_ITEM_SHARED_UNDERSTANDING_CONFIRMATION_REQUIRED`。
- 共同理解确认后：切换为 `WORK_ITEM_DESIGN_ENTRY_READY`。
- 唯一下一入口：`/codebase-design T04-G84`。
- 未生成 Spec、Tickets、开发验证、Review 或 Decision；未修改业务代码和测试。

该验证证明仓库调查、人工范围质询和设计是三个有顺序的步骤；`codebase-design` 不再替代 `grill-with-docs`。
