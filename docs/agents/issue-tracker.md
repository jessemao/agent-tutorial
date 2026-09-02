# Issue tracker：本地 Markdown

> 用途：在断网课堂中使用仓库内 Markdown 保存 Work Item、Spec 和 Tickets。  
> 限制：`docs/work-items/` 是课程任务过程目录，不是企业正式需求系统；不得写入真实甲方数据、账号、密钥或未经批准的业务结论。

本课程的需求规格、实施任务和交付证据统一保存在 `docs/work-items/<work-item-id>/`，不依赖课堂网络、GitHub 账号或外部 Issue 权限。

## 目录约定

- 一个任务一个目录：`docs/work-items/<work-item-id>/`。
- 任务入口：`docs/work-items/<work-item-id>/README.md`。
- 已批准 Spec：`docs/work-items/<work-item-id>/spec.md`。
- 实施任务：`docs/work-items/<work-item-id>/tickets/<NN>-<slug>.md`。
- Ticket 按依赖顺序从 `01` 编号，一张 Ticket 一个文件。
- 状态、版本和批准使用对应 Template 的固定字段，不自创第二套格式。
- 补充讨论必须整理成可裁决的事实、待确认项或 Decision，不保存无结论的完整对话。

## Skill操作约定

Spec、Ticket 和交付评审须引用任务使用的 Standards ID。标准入口为 `STANDARDS.md`；Spec 批准记录直接保存文件路径/版本、批准人和时间。`/implement` 同时遵守 Spec 与适用标准，`/code-review` 不得仅完成 Spec 轴。

- `/to-spec`所说的“发布到Issue Tracker”，表示写入对应`spec.md`。
- `/to-tickets`为每张任务创建独立文件，并用`Blocked by:`记录依赖。
- `/implement`只领取未阻塞、经人工批准且引用明确Spec 版本的Ticket。
- `/code-review` 优先从提交信息、Ticket 和当前 Work Item 定位已批准 Spec；原始材料作为决策来源保留，交付直接对照已批准 Spec。

课堂任务不得写入真实甲方Issue系统；课程结束后，如企业决定接入GitHub、GitLab、Jira或其他平台，再更新本文件并重新验证流程。

## 停止条件

- Spec 未批准、版本不明或与任务卡冲突。
- Ticket 没有验收条件、修改边界、验证命令或明确依赖。
- 任务需要外部系统权限、跨团队审批或真实客户数据。

不得通过修改状态字段绕过批准；Agent 只能记录人工决定，不能自行把任务置为已批准或已验收。
