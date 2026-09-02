# 项目级 Skills 清单与来源

> 用途：记录仓库安装的 Skills、来源、适用边界和调用顺序。  
> 限制：Skill 是执行流程和检查能力，不是业务权威、人工批准或最终放行主体。

## 课程主链路

以下 Skills 已安装在 `.agents/skills/`，来源和哈希记录在 `skills-lock.json`：

- `grill-with-docs`：复现/澄清与问题审查入口。
- `to-spec`：把已确认决定整理为 Spec。
- `to-tickets`：把大规格拆成可验证切片。
- `implement`：按批准范围和测试接缝实施。
- `code-review`：分开执行 Spec 与 Standards 评审。
- `tdd`：实现阶段的测试先行子能力。
- `domain-modeling`、`grilling`：支撑术语和需求审查。

## Clean Code 专项 Skills

来源：[ertugrul-dmr/clean-code-skills](https://github.com/ertugrul-dmr/clean-code-skills)，固定提交 `1b6b3cc1264b8fbe921c65002d05a3bf90ede178`。

安装内容：`clean-comments`、`clean-functions`、`clean-general`、`clean-names`、`clean-tests`。

上游只有 Python 和 TypeScript 语言轨道，没有 Java 主 Skill。为避免错误引入 Python/TypeScript 专属要求，本项目：

1. 不安装 `python-clean-code`、`typescript-clean-code`。
2. 不安装引用语言主 Skill 的 `boy-scout` 编排器。
3. 使用五个专项 Skill 中的语言无关规则处理 Java 代码。
4. 由 `docs/ai-governance/standards/clean-code.md` 负责选择适用 Skill 和规定人工验收入口。
5. 不修改下载的 `SKILL.md`；更新时重新下载并更新锁文件。

## 使用顺序

```text
AGENTS.md
→ Module AGENTS.md
→ workflow.md
→ standards/clean-code.md
→ 适用的 Clean Code Skills
→ 03_review.md Standards 矩阵
→ 人工裁决
```

Skill 输出是检查建议和证据，不是人工批准。

## 独立测试与 QA 交接

测试与 QA 按企业流程选择是否分别生成 `functional-test.md`、`qa-review.md`；未采用时记录 N/A、替代证据和风险，不以缺少课堂签署阻塞后续流程。业务负责人最终验收，测试与 QA 职责保持分离。

## 使用限制

- 只有 `skills-lock.json` 和来源哈希匹配的项目级 Skill 才能作为课程固定能力。
- Skill 缺失、版本不明、读取失败或与 Spec/Standards 冲突时必须停止，不得临时改写 Prompt 假装同一流程。
- 不得让 `/to-spec` 自行补齐未确认业务语义，也不得让 `/implement` 扩大人工批准范围。
- 项目内执行 `/code-review <id>` 时，必须把固定 Diff、Spec 与 Standards 两轴结论及 Findings 写入或更新 `docs/work-items/<id>/03_review.md`；只在对话中返回评审结果不算完成。
- `/code-review` 不能用 PASS 代替人工 Decision，不得生成 `04_decision.md` 或代替人作出裁决。
- 下载的 Clean Code Skills 不得就地修改；Java 只采用能从当前代码证实的语言无关规则。
