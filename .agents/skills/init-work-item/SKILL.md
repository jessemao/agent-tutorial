---
name: init-work-item
description: 在 training-wms 中安全初始化课程 Work Item，并按任务卡归档页面、API、日志和截图等原始证据。仅用于建档阶段，不执行分析、设计或代码修改。
---

# 初始化 Work Item

把重复的建档规则收进 Skill。调用者只提供本次任务的变量事实，不再复制整段流程 Prompt。

## 输入

至少取得：Work Item ID、任务类型和任务卡要求的事实。截图等附件由用户直接随消息上传；不要要求用户寻找附件的本机路径。

缺少关键事实、附件无法读取、附件含敏感信息、存在多张无法对应的附件，或目标目录已经存在且不能确认属于当前小组时，停止并报告，不得猜测、覆盖或伪造。

## 执行

1. 先遵守根 `AGENTS.md` 的读取顺序、权限和停止条件；根据 Work Item ID 前缀定位唯一的学员任务卡。
2. 检查 `docs/work-items/<work-item-id>/`。目录冲突时停止；没有冲突才创建目录和 `inputs/`。
3. 使用 `docs/ai-governance/templates/work-item.md` 创建 `README.md`：填写真实任务类型、`DISCOVERING`、当前 Git 分支和提交、代码起点、任务卡及输入路径。未知所有者、批准、结论和 N/A 理由保持待人工确认，不得虚构。
4. 使用 `docs/ai-governance/templates/input-evidence.md` 记录用户提供的事实。输入文件名、截图文件名和必须记录的业务字段以任务卡为准；明确区分预期、实际、事实、未知项，并注明原始证据不是根因或方案。
5. 将可安全使用的附件复制到同一 Work Item 的 `inputs/`，在证据文档中使用相对路径引用。不得修改图片内容来掩盖敏感信息；需要脱敏时停止并交人工处理。
6. 核对目录、Git 版本、业务标识、数量、状态、错误码和附件互相一致。

## 建档边界

本 Skill 只允许创建或更新 `README.md` 和 `inputs/`。不得生成 `01_analysis.md`、Spec、Design、Interface、测试或代码修改，也不得提前给出根因和实现方案。

完成后停止，只报告创建的文件、Git 版本、缺失信息和冲突，等待人工验收建档结果。
