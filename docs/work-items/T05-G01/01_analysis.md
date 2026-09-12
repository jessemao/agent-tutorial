# 01 Analysis：T05-G01

## 元数据

| 字段 | 内容 |
| --- | --- |
| 任务类型 | 组件或 Skill 复用 |
| Agent/工具 | Codex；只读仓库检索与文档核对 |
| 代码起点与当前版本 | `add14f73bf857604e5f738fe35111ab4ef1b3e05`；当前分支 `T05-G01` |
| Spec/Standards | 尚无 T05-G01 Spec；依据根 `AGENTS.md`、`docs/ai-governance/workflow.md`、`deliverables.md`、Templates、`skills.md`、T01～T04 学员卡和现有门禁 |

## 输入与事实

### 已读取材料

- T01～T04 学员任务卡：
  - `docs/training/T01_取消出库未释放库存_学员任务卡.md`
  - `docs/training/T02_调整入库分批收货规则_学员任务卡.md`
  - `docs/training/T03_从零新建库内移库_学员任务卡.md`
  - `docs/training/T04_统一库存业务入口_学员任务卡.md`
- 治理主干：`AGENTS.md`、`docs/ai-governance/workflow.md`、`deliverables.md`、`roles-and-approvals.md`、`skills.md`。
- Templates：`work-item.md`、`input-evidence.md`、`analysis.md`、`spec.md`、`design.md`、`interface.md`、`ticket.md`、`verification.md`、`review.md`、`functional-test.md`、`qa-review.md`、`decision.md`。
- 现有项目 Skills：重点读取 `init-work-item`、`grill-with-docs`、`to-spec`、`to-tickets`、`implement`、`code-review`、`tdd`、`codebase-design` 的职责与边界，并核对 `.agents/skills/` 清单。
- 门禁：`scripts/classroom-test.sh`、`classroom-verify.sh`、`validate-ai-governance.sh`、`validate-t05-skills.sh`。

### 已确认事实

- T01 因教学目的要求学员手写完整 Prompt；T02～T04 已逐步改为单行项目命令。该差异是调用方式差异，不改变阶段权限、模板、停止点和人工批准主干（`AGENTS.md:5-14`；T01 卡 `:67-97`；T02 卡 `:89-188`；T03 卡 `:63-166`；T04 卡 `:53-160`）。
- 四个任务都重复经历建档、发现/设计、实施批准、开发验证、固定 Diff Review、独立功能测试、QA、业务验收和人工 Decision；任务类型只改变专业分析路径与条件产物（`workflow.md:29-41,96-100`）。
- 阶段产物及写入时机已经由 Deliverables 和 Templates 统一定义，不应继续在每张任务卡或每次 Prompt 中复制（`deliverables.md:7-25,29-40,56-68`）。
- 现有 `init-work-item` 已证明“项目 Skill 只做薄治理编排”可行：它定位任务卡、检查目录、选择模板、采集 Git、保存输入，并在建档边界停止（`.agents/skills/init-work-item/SKILL.md:8-29`）。
- 专业 Skills 已覆盖需求质询、领域语言、设计、规格化、拆票、TDD 实施和双轴评审；T05 不应复制这些方法（`docs/ai-governance/skills.md:6-47`）。
- 自动门禁已经能够检查治理文件、模板标题、Skill 来源/哈希、T05 Skill 结构、显式调用设置和最终验证入口，但不能判断业务语义或代替人工批准（`validate-ai-governance.sh:7-95,234-272`；`validate-t05-skills.sh:15-109`；`skills.md:94-98`）。

### 已确认解释

- 2026-09-11，用户回复“认可。全部同意”，确认五层分类及以下解释：先把 `work-item-start` 做成经过 T01/T02、两会话及失败路径深度验证的首个产品化样本；之后仍按 T05 学员卡完成其余四个入口和完整链路。
- 五个目标入口均是项目级薄编排 Skill，而不是替代现有 AI Hero Skills 的新实现。
- T01 的手写长 Prompt 只作为重复机制和行为验证样本；T05 不回写或改变 T01 的手写教学方式。

### 未知项

- 五个 Skill 的最终输入语法、状态迁移、是否允许更新已有文件及精确错误输出尚未批准。
- 每个入口调用哪些现有专业 Skill、在不同任务类型下如何裁剪，尚未形成正式接口契约。
- 测试工程师、QA、开发负责人、AI 治理所有者及最终交付负责人尚未具名。

### 失败或澄清证据

- T01 卡仍需重复粘贴建档、分析、实施、Review 和 Decision 长 Prompt（例如 `T01...学员任务卡.md:67-97,126`）；T02～T04 相同阶段已收敛为 `/init-work-item`、`/grill-with-docs`、`/to-spec`、`/to-tickets`、`/implement`、`/code-review` 等短入口。这证明重复主要属于治理编排，而不是业务内容。
- T02 卡明确说明：若 Agent 仍要求重复粘贴治理规则，应停止检查项目 Skill，而非继续扩写 Prompt（`T02...学员任务卡.md:91-104`）。
- `deliverables.md:60-66` 与 T03/T04 卡的阶段产物表（T03 `:198-205`；T04 `:188-195`）表达同一套写入权限，说明这些规则已经稳定且跨任务重复。

## 定位或设计结论

### 调用链/业务流程

```text
人提供 Work Item ID、任务类型、事实/附件
→ 项目 Skill 固定读取顺序、模板、写入权限、停止点和下一入口
→ AI Hero/参考 Skill 完成当前阶段专业动作
→ 自动门禁检查确定性结构、版本和命令结果
→ 开发负责人 / 测试工程师 / QA / 业务与交付负责人分别作出人工结论
```

项目 Skill 应是治理适配层：向上接收少量任务变量，向下调用既有专业 Skill、模板和门禁；它不拥有业务语义、代码规则或人工决策权。

### 1. 应进入项目 Skill 的稳定治理动作

| 稳定动作 | 直接来源 | 错误归层的风险 |
| --- | --- | --- |
| 根据 Work Item ID 定位唯一任务卡、Work Item 和当前阶段，再按根/Module `AGENTS.md`、Workflow、角色、Spec、Standards 顺序读取 | `AGENTS.md:15-30`；`init-work-item/SKILL.md:18`；`skills.md:36-45` | 留在每次 Prompt 会持续漂移；交给 AI Hero 会把项目权限与通用专业方法耦合 |
| 检查目标目录占用，不覆盖已有任务；创建标准 Work Item 目录 | T01 卡 `:78-87`；T02 卡 `:91-116`；T03 卡 `:63-82`；T04 卡 `:53-72`；`init-work-item/SKILL.md:19` | 交给模板无法处理冲突；交给门禁只能事后发现，可能已覆盖证据 |
| 用标准模板生成 `README.md` 与 `inputs/input-evidence.md`，采集真实分支、提交、任务类型和状态，未知角色保持待指定 | `work-item.md:3-70`；`input-evidence.md:3-30`；`init-work-item/SKILL.md:20-23` | 写入 Spec 会混淆原始事实与方案；自动填批准会伪造人工结论 |
| 按任务类型选择发现路径，只自主回答仓库可证问题，只把必须由所有者决定的问题交人 | `workflow.md:48-58`；T02 卡 `:118-134`；T03 卡 `:84-94`；T04 卡 `:74-88` | 固化到单一专业 Skill 会让 Bug、新增、调整和重构套同一方法；全部交人会退化成长访谈 |
| 当前阶段只写允许产物，并在人工关口停止；不得提前创建后续文档 | `deliverables.md:56-68`；T03 卡 `:198-205`；T04 卡 `:188-195` | 仅写进 Standards 难以形成运行时边界；仅靠门禁属于事后拦截，仍会污染 Work Item |
| 在执行前核对开发负责人批准对象、当前 Ticket、可改/禁改范围和候选版本；批准缺失或范围变化时停止 | `workflow.md:31-36`；`skills.md:44-47`；T03 卡 `:132-142`；T04 卡 `:122-132` | 交给 TDD 会让测试方法承担授权；写死在 Ticket 会遗漏跨票状态和版本变化 |
| 实施完成只形成开发验证并停在 Review 前；Review 只形成 `03_review.md` 并停在人工 Finding 裁决前 | `deliverables.md:62-63`；`skills.md:44,86-87`；T02 卡 `:156-188`；T03 卡 `:132-166` | 让实施 Skill自动评审会自证；让 Review 自动修复会改变被审 Diff |
| 固定 Review 起点、候选提交、Spec 和 Standards 来源，保证文档与实际 Diff 一致 | T02 卡 `:178-188`；T03 卡 `:156-166`；T04 卡 `:152-162`；`code-review/SKILL.md:15-74` | 仅由通用 Review 猜测基线可能审错版本；仅由门禁无法判断选择的比较点是否具有业务意义 |
| 独立交接测试工程师、QA、业务负责人，分别引用 `functional-test.md`、`qa-review.md` 和业务验收，不互相代签 | `workflow.md:37-41,96-100`；T02 卡 `:190-234`；T03 卡 `:170-190`；T04 卡 `:164-180` | 放入测试 Skill 会混淆执行与流程授权；合并成一个 PASS 会失去责任与版本追溯 |
| 只接受人的原话 Decision，记录 Finding 裁决、被审版本、测试、QA、业务验收、MR/PR、回滚和风险；绝不自行批准或合并 | `decision.md:3-35`；`deliverables.md:66`；T01 卡 `:126`；T02 卡 `:221-234`；T03 卡 `:188-190`；T04 卡 `:178-180` | 交给自动门禁会把技术结果误当人工授权；交给 AI Hero 会诱发补写或改写人的结论 |
| 每阶段完成后报告本次产物、版本、未确认项和下一人工动作 | `workflow.md:84-94`；`init-work-item/SKILL.md:29` | 不统一会造成 Agent 继续越阶段工作，或向人重复询问仓库可回答的问题 |

上述动作分别构成 `work-item-start`、`work-item-discover`、`work-item-execute`、`work-item-review`、`work-item-decision` 的稳定治理骨架。基于已确认分类，形成以下待批准契约。

### 五个项目 Skill 契约

#### `work-item-start`

| 契约项 | 内容 |
| --- | --- |
| 输入 | 必需：Work Item ID、任务类型、任务卡要求的原始事实；可选：可安全归档的附件。不得要求用户重复提供仓库内可读取的治理规则。 |
| 读取顺序 | 根 `AGENTS.md` → 根据 ID 唯一匹配的学员任务卡 → Workflow、Roles、Deliverables、Templates 和 AI Security → 当前 Git 分支/提交 → 目标目录状态。 |
| 允许写入 | 仅 `docs/work-items/<id>/README.md`、`inputs/input-evidence.md` 及经安全检查的输入附件。目录必须此前不存在；不得覆盖已有 Work Item。 |
| 固定产物 | 使用 `work-item.md` 生成的 `README.md`；使用 `input-evidence.md` 生成的基础输入证据；真实 Git 版本、`DISCOVERING` 状态和待确认负责人。 |
| 停止条件 | ID/类型/必要事实缺失；任务卡无法唯一定位；目录冲突；Git 版本未知；附件缺失、无法对应、不可读、含敏感信息或需脱敏。 |
| 禁止动作 | 不分析、设计或创建 Skill；不生成 `01`—`04`、Spec、Design、Interface、Tickets；不改代码、测试、治理规范；不猜测人员、N/A 或批准。 |
| 下一入口 | 建档人工验收通过后，提示 `/work-item-discover <id>`；未通过时只报告冲突和缺失。 |
| 验收 | T01 Bug 与 T02 需求调整两个临时 ID、两个独立会话均生成相同结构且输入隔离；目录冲突和缺材料用例必须无写入或无覆盖地停止。 |

#### `work-item-discover`

| 契约项 | 内容 |
| --- | --- |
| 输入 | Work Item ID；已验收的 `README.md` 与 `inputs/`；任务类型；仓库当前版本。 |
| 读取顺序 | 根/相关 Module `AGENTS.md` → Workflow、Roles、Deliverables → Work Item 输入 → 任务卡 → 适用 Standards → 相关架构、API、测试、源码和历史证据 → 专业 Skill。 |
| 允许写入 | 必须生成或更新 `01_analysis.md`；仅当任务类型和已确认决定要求时，才允许生成当前阶段的 Design、Interface、Spec 草案或 Tickets，且遵守阶段顺序。 |
| 固定产物 | 事实/假设/未知分离、代码或文档证据、调用方与所有权、候选及排除理由、影响范围、可改/禁改建议、验证接缝、待人工问题。 |
| 停止条件 | Work Item 未通过建档；事实与仓库冲突；任务类型无法确定；真实调用方/所有权/关键语义不明；需要由业务、契约或技术所有者决定；发现受限数据。 |
| 禁止动作 | 不要求人回答仓库可证事实；不把未知写成规则；不修改代码或测试；不生成开发/评审/决定证据；不自行批准范围。 |
| 下一入口 | 根据任务类型调用 `grill-with-docs`、`codebase-design` 等专业能力；决定收敛后提示 `/to-spec <id>`，简单裁剪必须记录理由；停在实施范围人工批准前。 |
| 验收 | T01～T04 四类输入应选择不同专业路径但输出统一分析骨架；只询问所有者决策；所有结论有来源，且没有实施写入或历史答案泄漏。 |

#### `work-item-execute`

| 契约项 | 内容 |
| --- | --- |
| 输入 | Work Item ID 或当前 Ticket 路径；已批准 Analysis、Spec/Design/Interface 或裁剪记录；开发负责人批准原文；固定代码起点和允许/禁止范围。 |
| 读取顺序 | 根/受影响 Module `AGENTS.md` → Workflow、Roles → 当前 Work Item 与批准记录 → 当前 Ticket → Spec/Design/Interface → 适用 Standards → 相关代码测试 → `tdd` 等实施能力。 |
| 允许写入 | 仅当前 Ticket 批准范围内的代码和开发测试，以及本 Work Item 的 `02_verification.md`、必要 `artifacts/`；不得静默改写批准依据。 |
| 固定产物 | 当前切片的 Red/Green/Refactor 证据或批准的替代验证、目标测试、Module 回归、全量验证、验收映射、被验证版本、未覆盖风险和开发交接。 |
| 停止条件 | 批准记录/对象版本不完整；Ticket 被阻塞；首个验证接缝无效；测试失败；修改越界；Spec、公共契约、依赖、事务、数据库或所有权需要变化。 |
| 禁止动作 | 不扩大范围、不弱化测试、不绕过业务入口；不生成 `03_review.md`、`functional-test.md`、`qa-review.md` 或 `04_decision.md`；不提交、推送、建 MR/PR 或自批。 |
| 下一入口 | 当前 Ticket 验收后停下等待下一 Ticket；全部切片及 `classroom-verify` 通过后提示固定候选提交并调用 `/work-item-review <id>`。 |
| 验收 | 未批准调用必须零实施写入；批准调用只能产生当前票 Diff 与开发证据；测试失败、范围变化和版本漂移均稳定停止。 |

#### `work-item-review`

| 契约项 | 内容 |
| --- | --- |
| 输入 | Work Item ID；明确的差异起点与候选提交；Analysis、Spec/Design/Interface、Tickets、Verification；适用 Standards 和 Skill 版本。 |
| 读取顺序 | 根/相关 Module `AGENTS.md` → Workflow、Roles、Review 模板 → 固定 Git Diff → Work Item 实施依据与开发证据 → Standards → `code-review`。 |
| 允许写入 | 仅当前 Work Item 的 `03_review.md` 及必要只读审查证据；可更新 README 的评审状态，但不得改变已批准需求或代码。 |
| 固定产物 | 被审版本、Spec 符合性矩阵、Standards 符合性矩阵、带级别/位置/证据/建议的 Findings、问题复盘、防复发动作和待人工裁决项。 |
| 停止条件 | 比较点不能唯一解析；候选不基于声明起点；实际 Diff 与文档版本不一致；Spec、Standards 或 Verification 缺失；任一评审轴无法执行。 |
| 禁止动作 | 不边审边改；不用测试全绿代替双轴评审；不关闭 Findings、不作人工例外；不生成 Decision、不代签独立测试/QA、不批准或合并。 |
| 下一入口 | 将 Findings 交人逐条裁决；REWORK 回到 `work-item-execute` 并形成新候选；无阻塞项后交测试工程师独立功能测试，再进入 QA。 |
| 验收 | 有效固定 Diff 能生成完整 `03_review.md`；无效/漂移比较点必须停止；对话中的总结不得替代落盘文档。 |

#### `work-item-decision`

| 契约项 | 内容 |
| --- | --- |
| 输入 | 最小人工输入仅为 Work Item ID 与最终决定原文；决策身份/角色、时间、对象版本、Finding 裁决、测试/QA/业务验收、MR/PR、目标分支、回滚和风险优先从当前会话、Git、Work Item 与企业项目管理事实系统解析，不要求重复粘贴。 |
| 读取顺序 | 根 `AGENTS.md` → Workflow、Roles、Decision 模板 → Work Item 全部阶段产物 → 固定提交和 MR/PR 事实 → 人的原话决定。 |
| 允许写入 | 仅按模板生成或更新 `04_decision.md`，并在不改变原意的前提下补充已有证据引用；仅在人明确授权时执行普通提交/推送。 |
| 固定产物 | 人工决定原文、Finding 裁决、版本与 MR/PR、独立测试/QA/业务验收引用、回滚点、剩余风险和最终状态。 |
| 停止条件 | 决策人、角色、对象版本、原话结论或必要证据缺失；不同角色结论冲突；阻塞项未关闭；要求 Agent 自行选择接受/例外/回滚。 |
| 禁止动作 | 不改变、补充或替代人的结论；不从测试绿灯推导 ACCEPTED；不代签测试、QA 或业务验收；不批准 MR/PR、不执行合并、强推、发布或打答案标签。 |
| 下一入口 | `REWORK` 返回对应阶段；`BLOCKED` 等待责任人；`ACCEPTED` 只提示有权限的人审核并合并，合并事实由平台记录。 |
| 验收 | 缺少人工原话时零 Decision 写入；完整输入时文档逐项可追溯且语义不变；即使 ACCEPTED 也不自动合并。 |

### 跨 Skill 共同约束

- 五个入口必须放在项目 `.agents/skills/`，显式调用，项目来源和内容哈希登记到 `skills-lock.json`；不得修改第三方 Skill。
- 每个入口只拥有自己的阶段写入权限，前一阶段输出和人工结论是后一阶段输入；不能由一个入口自动串行越过人工关口。
- 输出必须报告实际产物、Git/对象版本、停止原因、未确认项和下一入口；“PASS”不能替代人的批准。
- 确定性结构交给 `validate-t05-skills.sh` 与 `validate-ai-governance.sh`，语义、风险和放行继续由对应人工角色决定。

### 2. 继续调用 AI Hero Skill 的专业动作

| 专业动作 | 保留能力/来源 | 错误归层的风险 |
| --- | --- | --- |
| 需求质询与领域语言收敛 | `grill-with-docs` 调用 `grilling` 与 `domain-modeling`（`.agents/skills/grill-with-docs/SKILL.md:7`）；T02/T03 调查入口 | 复制进 `work-item-discover` 会形成第二套需求方法，升级时必然漂移 |
| Module、Interface、Seam、职责深度与可测试性设计 | `codebase-design/SKILL.md:10-111`；T03 卡 `:96-106`；T04 卡 `:86-98` | 写入项目 Skill 会把通用设计知识和项目阶段控制混成“大 Skill” |
| 把已确认决定组织为可评审 Spec | `to-spec/SKILL.md:11-71`；T02 卡 `:136-154`；T03 卡 `:108-118`；T04 卡 `:100-110` | 项目 Skill 自己补写业务语义会绕过所有者确认；复制格式会与上游模板分叉 |
| 将已批准规格拆成纵向、可验证 Tickets | `to-tickets/SKILL.md:13-99`；T03 卡 `:120-130`；T04 卡 `:112-120` | 固定拆票算法进编排层会让所有任务强制拆票，违背 Workflow 裁剪规则 |
| Red–Green–Refactor、测试 Seam、最小实现方法 | `tdd/SKILL.md:12-34`；`implement` 项目入口；T02 卡 `:156-176`；T03 卡 `:132-154` | 编排层复制代码实施细节会扩大权限，并让治理变化与工程方法相互牵制 |
| Spec 与 Standards 双轴代码评审、并行取证和 Finding 聚合 | `code-review/SKILL.md:15-80`；`review.md:3-50` | `work-item-review` 自建第二套审查算法会弱化独立性；只保留 PASS 会丢失 Findings |

项目 Skill 对这些能力只负责选择、传递当前 Work Item 上下文、施加项目边界并在完成后停止，不复制其专业内容，也不修改第三方 Skill。

### 3. 只作为 Standards 参考的代码规则

| 规则类别 | 直接来源 | 错误归层的风险 |
| --- | --- | --- |
| 具体业务不变量：库存、状态、幂等、事务、锁顺序、流水、审计、并发、错误码和兼容 | T02 卡 `:248-271`；T03 卡 `:92-116,211-214`；T04 卡 `:47-49,86-98` | 写进通用项目 Skill 会把 T01～T04 的答案泄漏到新任务，并错误限制其他领域 |
| Module 依赖、公共入口、Repository 隔离、平台/数据库修改边界 | `AGENTS.md:75-82` 及各 Module `AGENTS.md`；T03 卡 `:104-106,211-214`；T04 卡 `:96-98,201-204` | 编排层成为架构权威后会覆盖 Module 所有者和已批准设计 |
| Clean Code 的命名、函数、注释、测试和通用代码质量规则 | `skills.md:49-61` 及 `.agents/skills/clean-*` | 放入阶段 Skill 会复制第三方内容、破坏来源哈希，并让 Review 无法区分治理与代码质量 |
| AI 安全、输入分类、Prompt Injection、外部写入和供应链限制 | `docs/ai-governance/standards/ai-security.md:5-66` | 只写进某个 Skill 会导致其他入口失去统一安全标准；Skill 也不能自行批准例外 |
| UI/接口完整旅程和可测性要求 | `workflow.md:60-66` 与 `standards/requirements-design.md` | 写成固定字段清单可能不适用于纯后端/文档任务；应由任务类型和影响范围选择适用标准 |

项目 Skill 可以定位并要求读取适用 Standards，但不能把这些规则全文复制进自身，也不能替所有者决定例外。

### 4. 交给自动门禁的确定性检查

| 可确定检查 | 直接来源 | 错误归层的风险 |
| --- | --- | --- |
| 治理文件和模板存在且非空、必需标题存在 | `validate-ai-governance.sh:7-95` | 让 Agent 靠文字自查容易漏项；反过来让脚本判断内容质量会制造假 PASS |
| Work Item 目录、固定文件名、阶段禁止产物和答案是否泄漏 | `deliverables.md:7-25,56-68`；`validate-t05-skills.sh:15-32,95-103` | 只靠 Prompt 无法稳定阻止拼写漂移；门禁也不能判断 N/A 的语义理由是否真实 |
| Skill 目录、`SKILL.md`/`agents/openai.yaml`、frontmatter 名称和八个契约标题 | `validate-t05-skills.sh:47-68` | 人工反复检查成本高且易漏；脚本通过不代表契约语义正确 |
| 阶段 Skill 必须显式调用 | `validate-t05-skills.sh:70-73` | 若只写在说明中，Agent 可能被隐式触发并越过人工阶段；脚本不能判断某次调用是否获得业务授权 |
| 项目来源、内容哈希和锁文件登记一致 | `validate-t05-skills.sh:75-90`；`skills.md:81-88` | 交给人工容易使用错版本；哈希一致不能证明来源内容安全或设计合理 |
| 目标测试、Module 回归、全量构建、前端构建、健康检查和 `git diff --check` | `classroom-test.sh:11-55`；`classroom-verify.sh:8-36` | 写成 Agent 口头“已通过”不可复核；脚本绿不能替代独立测试、QA 或业务验收 |
| 固定提交、实际 Diff 与记录版本的一致性 | `workflow.md:35-41`；各卡 Review 步骤 | 完全依赖自然语言容易审错范围；但比较点为何正确仍需项目 Skill 和人工语义判断 |

原则：门禁验证“可以机械判断的结构与结果”，项目 Skill 负责何时调用以及失败后停止，人负责语义和放行。

### 5. 必须留在 inputs/Spec 或由人决定的任务变量

| 变量/决定 | 应保留位置或角色 | 直接来源 | 错误归层的风险 |
| --- | --- | --- | --- |
| Work Item ID、任务类型、原始描述、页面/API/日志事实、附件、观察时间和业务对象标识 | `inputs/input-evidence.md` | `input-evidence.md:3-30`；T01～T04 建档步骤 | 写死进 Skill 会串组、串任务并污染可复用能力 |
| 当前行为、目标行为、业务规则、范围、非目标、验收条件、兼容/迁移/回滚决定 | `01_analysis.md` 与获批 `spec.md` | `spec.md:18-75`；`workflow.md:48-66` | 进入 Skill 会把历史任务答案伪装成通用规则，绕过业务与契约所有者 |
| 具体 Module、类、接口、调用方、Ticket 路径、测试接缝与允许修改目录 | Analysis/Design/Interface/Ticket，经开发负责人批准 | `analysis.md:20-49`；`design.md:14-49`；`interface.md:13-61`；`ticket.md:8-33` | 固定在 Skill 会扩大权限或让未来代码结构变化后执行错误位置 |
| 是否需要 Spec/Design/Interface/Tickets 以及裁剪理由 | 按 Workflow 判断并由开发负责人审核 | `deliverables.md:43-54`；`workflow.md:68-73` | 门禁或 Skill 一刀切会强造文档，或错误跳过复杂任务的必要设计 |
| 需求、UI/契约、平台、AI 治理和实施范围的所有者及批准 | 对应具名人工角色 | `roles-and-approvals.md:5-46`；`work-item.md:51-70` | Agent 自动填充会伪造授权；泛称“测试/QA”会破坏职责分离 |
| Review Finding 的接受/拒绝、风险例外、独立测试、QA、业务验收和最终交付决定 | 对应人工角色；最终原话进入 `04_decision.md` | `roles-and-approvals.md:20-40`；`decision.md:11-35` | 交给 Skill 或门禁会把建议/绿灯错误升级为人的决定 |
| MR/PR URL、被审提交、候选提交、合并记录和实际回滚点 | 当前 Work Item 与平台事实 | `deliverables.md:63-66`；T02/T03/T04 交付步骤 | 写成默认值会造成审查版本、链接和交付状态失真 |

## 影响与边界

- 受影响 Module、调用方和所有者：预期只影响项目 `.agents/skills/`、`skills-lock.json`、T05 Work Item 与相关门禁；不涉及仓储业务 Module。调用方是 T01～T04 类型的未来 Work Item 和学员/Agent。所有权需由 AI 治理所有者与开发负责人确认。
- 数据、状态、事务、幂等与并发：本任务不改变业务数据或事务；这些只作为历史任务中的可变专业内容，用于验证路由不会泄漏答案。
- API/UI/公共契约：不改变业务 API/UI；待设计的是五个命令入口及其文档写入契约。
- 用户旅程、输入控制、结果可见性与刷新后查询：用户输入应收敛为 Work Item ID 加当前阶段必要事实/决定；每次执行应明确显示产物、版本、停止原因、未确认项和下一入口。精确交互尚未批准。
- 依赖能力归属：现有 AI Hero/参考 Skills 与 Templates 属于代码起点；五个薄编排 Skills 属于本次任务；业务规则变化属于各自独立 Work Item。
- 允许修改：当前阶段仅允许新增/更新本 Work Item 的 `01_analysis.md`。
- 禁止修改：现有 `.agents/skills/`、业务代码、测试、门禁、治理规范、Templates、Spec、Design、Interface、Tickets 及后续阶段文档。
- 风险与回滚：主要风险是复制第三方方法、泄漏历史业务答案、让 Skill 代批、让门禁越权判断语义以及五个入口职责重叠。当前只新增分析文档，回滚方式为删除本文件；不得静默回滚已记录的原始输入。

## 实施任务

- 目标行为：待批准后，形成五个项目级薄编排 Skill，使人只提供任务变量和人工决定，系统稳定执行读取、写入边界、专业能力路由、门禁和停止点。
- 首个测试或验证接缝：先以 `work-item-start` 为样本，使用 T01 Bug 与 T02 需求调整两个临时 Work Item、两个独立会话，验证相同结构、输入隔离、目录冲突/缺材料停止和不生成后续产物。
- 最小实施步骤：
  1. 已完成人工审核五层分类及解释。
  2. 已完成人工审核本分析新增的五个 Skill 契约、共同约束和范围方向。
  3. 形成并批准 `spec.md`、`design.md`、`interface.md`。
  4. 拆成五张纵向 Tickets；先实现并深度验证 `work-item-start`，再逐个实现其余四个。
  5. 运行逐 Skill 门禁、跨会话测试和完整短命令链；之后进入固定 Diff Review、独立功能测试、QA 和人工 Decision。
- 验证命令：后续按阶段使用 `./scripts/validate-t05-skills.sh <skill-name>`、`./scripts/validate-ai-governance.sh` 和 `./scripts/classroom-verify.sh T05`；当前分析阶段未执行实现验证。
- 越界停止条件：分类未获人工认可；真实调用方或所有权不清；需要修改第三方 Skill；任务事实或业务答案将进入 Skill；入口可能自行批准/合并；实施范围、接口或验证方式未获开发负责人批准。

## 契约与范围方向确认

| 确认角色 | 确认人 | 结论 | 时间 | 确认对象与边界 |
| --- | --- | --- | --- | --- |
| 开发负责人代理 | 用户（课堂代理，具名信息待登记） | APPROVED | 2026-09-11 11:16:17 CST | 同意五个 Skill 契约、共同约束和进入 Spec/Design/Interface 的范围方向；不构成实施批准 |
| AI 治理所有者代理 | 用户（课堂代理，具名信息待登记） | APPROVED | 2026-09-11 11:16:17 CST | 同意五层归类、项目薄编排边界、显式调用、逐 Skill 门禁及不得修改第三方 Skill；不构成最终放行 |

## 开发负责人实施批准

| 结论 | 开发负责人 | 时间 | 批准对象与条件 |
| --- | --- | --- | --- |
| APPROVED | 用户（开发负责人课堂代理；具名信息待登记） | 2026-09-11 11:35:58 CST | 批准 Spec v1、Design v1、Interface v1、五个 Skill 契约、验证接缝、实施顺序及可改/禁改范围；AI 治理契约由用户以另一课堂代理角色单独批准 |
