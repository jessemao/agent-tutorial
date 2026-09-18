# 项目级 Skills 清单与来源

> 用途：记录仓库安装的 Skills、来源、适用边界和调用顺序。  
> 限制：Skill 是执行流程和检查能力，不是业务权威、人工批准或最终放行主体。

## 课程主链路

V2.0 不提供能够自动串联阶段的聚合 Skill。旧 T05 的 `work-item-start`、`work-item-discover`、`work-item-execute`、`work-item-review` 和 `work-item-decision` 不安装、不登记、不调用。

M1 不创建项目 Skill，只用 Prompt 完成 Work Item 建档。可重复动作统一在 M5 判断；只有结论为“复用”且获得对应责任人批准，才在阶段最后创建或更新项目 Skill。

以下第三方 Skills 已安装在 `.agents/skills/`，来源和哈希记录在 `skills-lock.json`：

- `grill-with-docs`：复现/澄清与问题审查入口。
- `to-spec`：把已确认决定整理为 Spec。
- `to-tickets`：第三方 Ticket 拆分能力；Spec、Design、AC 和范围明确且需要多个切片时，由 M2 调用。
- `implement`：按批准范围和测试接缝实施。
- `code-review`：分开执行 Spec 与 Standards 评审。
- `tdd`：实现阶段的测试先行子能力。
- `domain-modeling`、`grilling`：支撑术语和需求审查。
- `codebase-design`：为需求成型和 TDD 提供 Module、Interface、Seam 与深度设计词汇；它是参考层，不单独驱动流程。

新增业务需求的推荐组合为：

```text
/grill-with-docs
  → 自动调用 grilling + domain-modeling：质询需求并统一领域语言
→ /to-spec：只记录已经批准的决定
→ 项目经理核对 Spec
→ 显式调用 codebase-design：依据 Spec 确认 Interface、Seam、不变量与测试面
→ /to-tickets：在拆票条件满足后生成纵向 Tickets
→ 分角色正式批准实施范围
→ /implement：只实施当前 Ticket，由执行人完成个人验收测试
→ /code-review：分开检查 Spec 与 Standards
→ 人工 Decision 与合并
→ 人工完成 M5 复用判断并生成 reuse-decision.md
```

`codebase-design` 和 `tdd` 均为参考能力：前者不自行生成流程产物，后者不自行驱动整项实施。必须由当前阶段的主 Skill 调用，并继续服从项目 Workflow 的批准点和停止条件。

## 项目编排契约

第三方 Skill 保持上游原文和锁定哈希，不就地改写。课程中使用 `/<skill> <work-item-id>` 单行命令；Agent 先从 `docs/work-items/<work-item-id>/` 解析上下文，再将上游 Skill 当作当前阶段的子能力。上游默认与本契约冲突时，以根 `AGENTS.md`、Workflow 和本契约为准。

| 命令 | Agent 自动定位的输入 | 必须产物 | 项目级边界 |
| --- | --- | --- | --- |
| `/grill-with-docs <id>` | `README.md`、`inputs/`、任务卡、Workflow、Standards、Module 规则、API、测试和源码 | `01_analysis.md` | 自主回答仓库可证问题；只询问必须由所有者决定的问题；不生成 Spec/Design/Interface，不改代码或测试 |
| `/to-spec <id>` | `01_analysis.md` 中的代码证据和已记录的人工决定 | 按仓库模板生成当前 Work Item 的 `spec.md` | 不重新访谈、不补写未确认规则、不发布到 Issue Tracker、不生成 Design、Interface 或 Tickets、不改代码或测试；完成后等待项目经理核对业务规则 |
| `/to-tickets <id>` | 当前 Work Item 中已确认的 `spec.md`、`design.md`、AC、Standards 和修改范围 | `docs/work-items/<id>/tickets/<NN>-<slug>.md`，每张 Ticket 单独成文件 | 前置决定未关闭时停止；涉及 UI 时必须把页面入口、控件、API 接入、反馈和浏览器测试放入同一纵向 Ticket；不发布到外部 Tracker 或上游默认临时目录；使用仓库 Ticket 模板并等待 Design、Tickets 和范围批准 |
| `/implement <id>` | 已批准的 Analysis、Spec/Design/Interface、当前 Ticket，以及明确的负责人、执行人和可改/禁改范围 | 当前 Ticket 的代码与测试 Diff、实施证据和个人验收记录 | 批准记录缺失时停止；涉及 UI 时必须按 Ticket 同时实现页面、公开 API 接入和浏览器测试；只处理当前 Ticket；不自动 Review、不生成 `02`—`04`、不提交、推送或创建 MR/PR |
| `/code-review <id>` | Work Item 中的代码起点、当前候选提交、Spec/Design/Interface、Verification 和适用 Standards | `03_review.md` 的 Spec 与 Standards 两轴矩阵 | 先固定 Diff，无法唯一解析时才询问；不改代码、不生成 `04`、不自批、不推送或操作 MR/PR |

**M2 调查与 Grill 限制：**Agent 自主完成仓库能够回答的调查，读取根与 Module `AGENTS.md`、任务卡、当前 Work Item、Workflow、Standards、源码、测试、配置和架构材料；每项事实必须引用文件路径、代码符号、测试或文档证据，并在 `01_analysis.md` 中与假设、未知项、风险和能力缺口分开记录。调查与 Grill 期间只允许更新当前 Work Item 的 `01_analysis.md`，不得修改代码、测试或配置，不得提前生成后续阶段材料。

调用 `grill-with-docs` 时，Agent 必须按问题依赖关系逐轮主动提问，为每题给出建议答案，然后停止并等待对应角色回复；不得自问自答、替角色批准或把建议写成决定。每项结论记录角色原文、对象版本、负责人和时间，问题未关闭时不得推进。

| 被访谈角色 | 必须完成的动作 | 重点问题 |
| --- | --- | --- |
| 项目经理 / 业务所有者 | 决定业务规则、确认 AC、业务验收范围、非目标和未决事项负责人 | 盘点范围与维度、实盘/预留、状态、差异、驳回/取消、异常、权限和用户路径 |
| 开发负责人 | 裁决设计方向、风险接受、测试接缝、修改范围和停止条件；把业务问题退回项目经理 | Module/Interface、数据归属、事务、幂等、锁与并发、审计、错误、回滚和跨 Module 影响 |
| 测试工程师 | 确认每条 AC 的验证方式、数据环境、预期结果、风险覆盖和不可测试项；把接缝不足退回开发负责人 | 正常、异常、边界、状态、幂等、重复审批、回滚、并发、权限、审计和回归 |

### M2 第三方 Skill 输出映射

| 第三方 Skill | 上游默认行为 | 本项目接受的保存位置与内容 | 是否可直接作为交付 |
| --- | --- | --- | --- |
| `grill-with-docs` / `grilling` / `domain-modeling` | 对话质询，并可能维护根级术语表或 ADR | 事实、假设、未知项、风险和已确认决定写入当前 Work Item 的 `01_analysis.md`；未经单独批准不新增根级术语表或 ADR | 否，必须归档到 `01_analysis.md` |
| `to-spec` | 使用通用模板并发布到 Issue Tracker | 按仓库 Spec 模板写入当前 Work Item 的 `spec.md`；审批、用户旅程、事务、验收映射等项目字段必须完整 | 否，必须转换为项目模板且不得对外发布 |
| `codebase-design` | 提供 Module、Interface、Seam、Adapter 等设计词汇，不规定文件输出 | 依据已确认 Spec，把设计决定按仓库模板写入当前 Work Item 的 `design.md`，复杂契约按需写入 `interface.md` | 否，它只是设计参考能力 |
| `to-tickets` | 发布到外部 Tracker 或上游默认本地临时目录 | 一张 Ticket 一个文件，写入当前 Work Item 的 `tickets/`；必须包含 Spec/Standards 版本、纵向行为、依赖、修改边界、AC、验证命令和停止条件 | 否，只有项目路径和模板通过门禁后才算完成 |

以上映射是调用前契约，不是事后搬运规则。调用第三方 Skill 时必须同时提供 Work Item ID、允许读取的已确认材料、项目输出路径、仓库模板、禁止的外部发布和停止条件。Skill 若准备使用上游默认路径或通用模板，应在写入前停止；不得先生成错误产物再复制到 Work Item。

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

## Skill 沉淀时机

M1 只完成 `README.md` 与 `inputs/` 建档，不创建 Skill。小组先完成 M1—M4，再在 M5 用跨任务证据检查触发、输入、步骤、输出、停止条件和所有权。只有人工结论为“复用”时，才在 M5 最后创建或更新项目 Skill，并记录资产路径、批准人、使用边界和验证结果。

## 使用限制

- 第三方固定能力必须与 `skills-lock.json` 和来源哈希匹配；M5 新建的项目 Skill 由复用决定、阶段门禁和人工核对验收，不伪装成第三方锁定能力。
- Skill 缺失、版本不明、读取失败或与 Spec/Standards 冲突时必须停止，不得临时改写 Prompt 假装同一流程。
- 不得让 `/to-spec` 自行补齐未确认业务语义，也不得让 `/implement` 扩大人工批准范围。
- 项目内执行 `/code-review <id>` 时，必须把固定 Diff、Spec 与 Standards 两轴结论及 Findings 写入或更新 `docs/work-items/<id>/03_review.md`；只在对话中返回评审结果不算完成。
- `/code-review` 不能用 PASS 代替人工 Decision，不得生成 `04_decision.md` 或代替人作出裁决。
- 下载的 Clean Code Skills 不得就地修改；Java 只采用能从当前代码证实的语言无关规则。

## 独立测试与 QA 交接

`/implement` 只生成开发 TDD 与任务回归证据；`/code-review` 只给固定 Diff 的 Spec / Standards 代码评审建议。测试与 QA 按企业流程选择是否分别生成 `functional-test.md`、`qa-review.md`，未采用时记录 N/A、替代证据和风险；业务负责人最终验收。保持第三方 Skills 原文，所有分阶段限制在项目治理层执行。

## V2.0 M1—M5 使用边界

| 阶段 | 可调用能力 | 不得替代 |
| --- | --- | --- |
| M1 | 不创建 Skill；用 Prompt 建立 `README.md` 与 `inputs/` | 角色确认、边界确认、字段人工核对和基线复验 |
| M2 | 第三方 `grill-with-docs`、`grilling`、`domain-modeling`、`to-spec`、`codebase-design`；前置决定明确后可调用 `to-tickets` | 业务决定、Spec/Design/Tickets 人工批准；第三方默认保存位置和模板不直接算交付 |
| M3 | `implement` | 当前 Ticket 的负责人/执行人、实施证据和执行人个人验收测试 |
| M4 | `code-review`、适用 `clean-*` | 固定候选、真实测试、Finding 裁决、独立验收和 Decision |
| M5 | 结论为复用且获批后，创建或更新项目 Skill/资产 | 跨任务证据、真实调用方、所有权和 `reuse-decision.md` |

M5 可以得出“暂缓”或“不复用”。只有 `reuse-decision.md` 证明能力跨任务稳定、五项契约和所有权完整，并取得对应所有者批准后，才在 M5 最后创建或更新 Skill、模板或其他复用资产。
