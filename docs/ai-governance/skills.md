# 项目级 Skills 清单与来源

> 用途：记录仓库安装的 Skills、来源、适用边界和调用顺序。  
> 限制：Skill 是执行流程和检查能力，不是业务权威、人工批准或最终放行主体。

## 课程主链路

以下 Skills 已安装在 `.agents/skills/`，来源和哈希记录在 `skills-lock.json`：

- `init-work-item`：项目自建的建档入口；读取任务卡和治理模板，检查目录冲突，生成 `README.md` 与基础输入证据文档，归档聊天附件后停止。
- `grill-with-docs`：复现/澄清与问题审查入口。
- `to-spec`：把已确认决定整理为 Spec。
- `to-tickets`：把大规格拆成可验证切片。
- `implement`：按批准范围和测试接缝实施。
- `code-review`：分开执行 Spec 与 Standards 评审。
- `tdd`：实现阶段的测试先行子能力。
- `domain-modeling`、`grilling`：支撑术语和需求审查。
- `codebase-design`：为需求成型和 TDD 提供 Module、Interface、Seam 与深度设计词汇；它是参考层，不单独驱动流程。

新增业务需求的推荐组合为：

```text
/grill-with-docs
  → 自动调用 grilling + domain-modeling：质询需求并统一领域语言
→ 显式调用 codebase-design：确认 Interface、Seam、不变量与测试面
→ 人工批准业务和设计决定
→ /to-spec：只记录已经批准的决定
→ /to-tickets：拆成可独立验证的纵向切片
→ /implement：内部按已批准 Seam 应用 tdd
→ /code-review：分开检查 Spec 与 Standards
→ 人工 Decision 与合并
```

`codebase-design` 和 `tdd` 均为参考能力：前者不自行生成流程产物，后者不自行驱动整项实施。必须由当前阶段的主 Skill 调用，并继续服从项目 Workflow 的批准点和停止条件。

## 项目编排契约

第三方 Skill 保持上游原文和锁定哈希，不就地改写。课程中使用 `/<skill> <work-item-id>` 单行命令；Agent 先从 `docs/work-items/<work-item-id>/` 解析上下文，再将上游 Skill 当作当前阶段的子能力。上游默认与本契约冲突时，以根 `AGENTS.md`、Workflow 和本契约为准。

| 命令 | Agent 自动定位的输入 | 必须产物 | 项目级边界 |
| --- | --- | --- | --- |
| `/grill-with-docs <id>` | `README.md`、`inputs/`、任务卡、Workflow、Standards、Module 规则、API、测试和源码 | `01_analysis.md` | 自主回答仓库可证问题；只询问必须由所有者决定的问题；不生成 Spec/Design/Interface，不改代码或测试 |
| `/to-spec <id>` | `01_analysis.md` 中的代码证据和已记录的人工决定 | 按任务类型生成 `spec.md`、`design.md`、`interface.md`；Tickets 按裁剪规则处理 | 不重新访谈、不补写未确认规则、不发布到 Issue Tracker、不改代码或测试；完成后等待范围批准 |
| `/implement <id>` | 已批准的 Analysis、Spec/Design/Interface 或 Tickets，以及明确的可改/禁改范围 | 最小代码与测试 Diff、红绿证据、`02_verification.md` | 批准记录缺失时停止；按批准接缝调用 `tdd`；不自动 Review、不生成 `03`/`04`、不提交、推送或创建 MR/PR |
| `/code-review <id>` | Work Item 中的代码起点、当前候选提交、Spec/Design/Interface、Verification 和适用 Standards | `03_review.md` 的 Spec 与 Standards 两轴矩阵 | 先固定 Diff，无法唯一解析时才询问；不改代码、不生成 `04`、不自批、不推送或操作 MR/PR |

`/implement` 的上游 Skill 默认“完成后 Review 并提交”、`/to-spec` 的上游 Skill 默认“发布到 Issue Tracker”在本项目中明确禁用；这些动作必须等待 Workflow 对应阶段和人工授权。

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

## 为什么建档使用 Skill

学员只提交 Work Item ID、任务类型和页面观察等变量事实。目录规则、模板选择、Git 信息采集、附件归档、停止条件和输出检查由 `init-work-item` 统一执行。不要在每张任务卡中复制同一段流程 Prompt；规则变化时只更新 Skill 和模板，避免不同学员、Agent 或任务产生格式漂移。

## 使用限制

- 只有 `skills-lock.json` 和来源哈希匹配的项目级 Skill 才能作为课程固定能力。
- Skill 缺失、版本不明、读取失败或与 Spec/Standards 冲突时必须停止，不得临时改写 Prompt 假装同一流程。
- 不得让 `/to-spec` 自行补齐未确认业务语义，也不得让 `/implement` 扩大人工批准范围。
- 项目内执行 `/code-review <id>` 时，必须把固定 Diff、Spec 与 Standards 两轴结论及 Findings 写入或更新 `docs/work-items/<id>/03_review.md`；只在对话中返回评审结果不算完成。
- `/code-review` 不能用 PASS 代替人工 Decision，不得生成 `04_decision.md` 或代替人作出裁决。
- 下载的 Clean Code Skills 不得就地修改；Java 只采用能从当前代码证实的语言无关规则。

## 独立测试与 QA 交接

`/implement` 只生成开发 TDD 与任务回归证据；`/code-review` 只给固定 Diff 的 Spec / Standards 代码评审建议。测试与 QA 按企业流程选择是否分别生成 `functional-test.md`、`qa-review.md`，未采用时记录 N/A、替代证据和风险；业务负责人最终验收。保持第三方 Skills 原文，所有分阶段限制在项目治理层执行。

## T05 项目 Skill 产品化约束

T05 提炼的 `work-item-start`、`work-item-discover`、`work-item-execute`、`work-item-review`、`work-item-decision` 必须创建在项目 `.agents/skills/`，不得修改或覆盖第三方 Skills。五个入口各用一张纵向 Ticket 实施，按 `start → discover → execute → review → decision` 顺序逐个生成和验证；不能用 Tickets N/A 或一次性生成五个目录绕过独立验收。

每个项目 Skill 必须显式声明输入、读取顺序、允许写入、固定产物、停止条件、禁止动作、下一入口和验收方式，并在 `agents/openai.yaml` 中关闭隐式调用。来源、内容哈希和文件清单必须登记到 `skills-lock.json`。每个 Skill 完成后运行 `./scripts/validate-t05-skills.sh <skill-name>`，完整链路结束后运行 `./scripts/classroom-verify.sh T05`；门禁只验证确定性结构，不能替代代码 Review、测试工程师功能测试、QA 审查或业务 Decision。
