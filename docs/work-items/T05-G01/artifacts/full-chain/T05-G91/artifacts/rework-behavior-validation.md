# T05-G91 REWORK 行为验证

## 对象与方法

- 对象：REWORK 后的 `.agents/skills/work-item-status/SKILL.md`。
- 接缝：显式 `/work-item-status <id>` 的输出，以及查询前后的 Git 工作区状态。
- 执行者：独立 Agent；只读，不修改夹具或实现。
- 环境：隔离仓库与五个合成 Work Item 输入；本机临时路径不作为可移植证据。

## 结果

| 场景 | 结果 | 唯一下一动作 / 停止行为 |
| --- | --- | --- |
| `T05-DISCOVER`：建档已验收、无 Analysis | `DISCOVERING` | `/work-item-discover T05-DISCOVER` |
| `T05-REVIEW`：Review 有 OPEN Finding | `REVIEWING` | 仅由有权人员裁决 Finding；不进入独立测试 |
| `T05-INCOMPLETE`：声称已批准但缺 Spec/Design/批准证据 | `WORK_ITEM_STATUS_INCOMPLETE` | 无可执行入口；由责任人补齐证据 |
| `T05-BLOCKED`：必需证据位于无权读取的受限来源 | `WORK_ITEM_EVIDENCE_BLOCKED` | 无可执行入口；未读取或回显受限正文 |
| `../../AGENTS.md`：路径越界输入 | `WORK_ITEM_NOT_FOUND` | 在读取目标前拒绝；未探测或回显边界外内容 |

每项均返回符合契约的 stage/error、stop_reason、next_action 和最小 evidence。查询前后 `git status --short` 完全一致，`git diff --check` 无输出。

本结果是开发阶段的独立前向验证，不代替测试工程师的 `functional-test.md` 或 QA 审核。
