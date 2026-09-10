# Work Items 任务过程目录

`docs/work-items/` 只保存单个需求、Bug、重构或复用任务从输入到交付的过程文档。

## 分支与目录规则

- Work Item 产物的唯一逻辑路径是当前 Git 分支根目录下的 `docs/work-items/<work-item-id>/`。
- 课程不使用 Git subtree，也不把嵌套 Git worktree 作为小组交付模型；不得把 `.worktrees/`、subtree 或其他嵌套仓库路径写入产物路径。
- 每个小组使用一个完整仓库目录和一个唯一分支，例如 `group-01/t01`；同一目录同一时刻只允许检出一个分支。
- 需要并行保留多个小组时，使用多个彼此独立的完整仓库目录；每个目录都必须记录自己的分支、起点提交和 Work Item ID。
- 提交、推送和 MR/PR 必须以分支为边界；目录位置不能替代分支隔离，也不能作为交付版本依据。

每项任务使用独立且唯一的目录：

```text
docs/work-items/<work-item-id>/
```

课堂可使用 `T01-G01`、`T02-G03` 等 ID；项目任务可使用 `WMS-2026-001` 等可追溯 ID。

开始时必须先使用 `docs/ai-governance/templates/work-item.md` 建立任务 `README.md`，然后使用其他对应模板生成所需文档。完整目录结构、可选产物、命名、状态和停止条件见 `docs/ai-governance/standards/documentation.md`。

禁止在本目录内定义项目级 Standards、Workflow 或课程通用答案；禁止将密钥、真实客户数据或未脱敏输入写入任务目录。
