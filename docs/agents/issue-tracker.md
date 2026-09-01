# Issue tracker：本地 Markdown

本课程的需求规格和实施任务保存在`.scratch/`，不依赖课堂网络、GitHub账号或外部Issue权限。

## 目录约定

- 一个需求一个目录：`.scratch/<feature-slug>/`。
- 规格文件：`.scratch/<feature-slug>/spec.md`。
- 已批准 Spec：`.scratch/<feature-slug>/spec.md`，记录需求、设计、验收、版本和批准记录。
- 实施任务：`.scratch/<feature-slug>/issues/<NN>-<slug>.md`。
- Ticket按依赖顺序从`01`编号，一张Ticket一个文件。
- 状态写在文件顶部的`Status:`字段。
- 补充讨论追加到文件末尾的`## Comments`。

## Skill操作约定

Spec、Ticket 和交付评审须引用任务使用的 Standards ID。标准入口为 `STANDARDS.md`；Spec 批准记录直接保存文件路径/版本、批准人和时间。`/implement` 同时遵守 Spec 与适用标准，`/code-review` 不得仅完成 Spec 轴。

- `/to-spec`所说的“发布到Issue Tracker”，表示写入对应`spec.md`。
- `/to-tickets`为每张任务创建独立文件，并用`Blocked by:`记录依赖。
- `/implement`只领取未阻塞、经人工批准且引用明确Spec 版本的Ticket。
- `/code-review` 优先从提交信息、Ticket 和 `.scratch/` 定位已批准 Spec；原始材料作为决策来源保留，交付直接对照已批准 Spec。

课堂任务不得写入真实甲方Issue系统；课程结束后，如企业决定接入GitHub、GitLab、Jira或其他平台，再更新本文件并重新验证流程。
