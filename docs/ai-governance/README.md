# AI Coding 治理体系与仓库落地规范

> 文档状态：课程仓库已落地，后续按评审意见维护  
> 适用范围：`training-wms` 课程仓库及后续同类 Java / Spring Boot 培训项目  
> 核心原则：文档提供上下文，Workflow 固定流程，Standards 定义质量目标，Templates 统一产物，Skills 保存可执行设置，自动校验负责拦截，人负责批准和最终裁决。

## 1. 目标

这套体系解决五个问题：

1. 不同 Agent 按相同顺序读取材料并在相同节点停下。
2. Agent 在修改代码前完成复现或澄清、定位和影响分析。
3. 实现同时符合已批准 Spec 与适用 Standards。
4. 不同工具生成结构一致、可自动检查的任务产物。
5. 交付结论能直接追溯到 Spec 版本、Standards 版本、代码差异和测试证据。

已批准 Spec 是需求依据；Standards 的固定版本是质量依据；Git 提交或标签定义代码差异起点。三者直接记录和追溯，不再增加中间封装。

## 2. 各层职责

### 2.1 `AGENTS.md`：行为控制层

`AGENTS.md` 是强制入口，只规定读取顺序、人工批准点、禁止改代码的条件、可写范围、停止条件和完成条件。它不复制业务需求、代码规则、Skill 步骤或模板字段。

仓库采用“根目录全局规则 + Module 局部规则”的嵌套方式。文件统一命名为 `AGENTS.md`，避免同时出现 `Agent.md`、`AGENT.md` 等不同写法导致工具识别不一致。

默认读取顺序：

1. 仓库根目录 `AGENTS.md`
2. 从仓库根目录到目标文件所在目录，按路径由远到近读取沿途所有 `AGENTS.md`
3. `docs/ai-governance/workflow.md`
4. 当前任务材料与已批准 Spec
5. `docs/ai-governance/standards/` 中与任务相关的引导文件
6. 相关架构、API、测试和源码
7. 被引导文件指定的 Skill

#### Module 级 `AGENTS.md` 的职责

每个独立 Module 可以在自身根目录放置一个 `AGENTS.md`，只补充本 Module 的局部上下文：

- Module 的职责、所有者和允许承载的业务。
- 允许修改与禁止修改的目录、包、类和公开接口。
- 上游/下游依赖、依赖方向及不得跨越的边界。
- 核心入口、领域不变式、事务和并发约束。
- 本 Module 必须运行的目标测试、回归命令和验收证据。
- 修改公开契约、跨 Module 调用或引入依赖时的人工批准点。
- 本 Module 需要调用的 Standards 引导文件和 Skills。

Module 文件不重复根目录的通用流程、Clean Code 规则正文或模板内容。它的价值是把局部边界放到代码旁边，让 Agent 进入该目录时自动获得更精确的约束。

#### 继承、覆盖与冲突规则

1. 根目录 `AGENTS.md` 始终生效，Module 文件不得覆盖或放宽根规则。
2. 越接近目标文件的 `AGENTS.md` 优先补充更具体的局部约束，但只能细化或收紧上层规则。
3. 同时修改多个 Module 时，必须读取所有受影响 Module 的 `AGENTS.md`，并同时满足各自约束。
4. 跨 Module 的接口、依赖方向、事务或所有权发生变化时，Agent 必须停止并取得相关所有者批准。
5. 上下层规则冲突、规则适用范围不清或两个 Module 的要求互斥时，Agent 不得自行选择，必须记录冲突并交人工裁决。

示例：

```text
training-wms/
├── AGENTS.md                         # 全局流程、共同禁止项、统一完成条件
├── platform-contracts/
│   └── AGENTS.md                     # 公共契约兼容性、版本和所有权约束
├── platform-web-starter/
│   └── AGENTS.md                     # Web 装配、统一异常与组件扩展约束
├── business-wms/
│   └── AGENTS.md                     # 仓储领域、不变式、事务和业务测试约束
└── training-server/
    └── AGENTS.md                     # 只允许装配，禁止承载业务规则
```

### 2.2 `workflow.md`：统一流水线层

```text
读取上下文 → 复现问题/澄清需求 → 影响分析 → 人工批准
→ 测试先行 → 最小化实施 → Spec/Standards 双轴评审
→ 人工决定接受/退回/回滚 → 交付与沉淀
```

| 阶段 | Agent 必须完成 | 人工职责 | 停止条件 |
| --- | --- | --- | --- |
| 读取上下文 | 校验入口、材料版本和所有权 | 提供任务材料与代码起点 | 必需材料缺失或冲突 |
| 复现/澄清 | 运行失败案例或列出待确认问题 | 确认现象或业务规则 | 没有可观察证据，禁止猜测修复 |
| 影响分析 | 定位调用链、模块、契约、数据、事务和测试 | 批准根因、目标和可改范围 | 未批准前禁止修改业务代码 |
| 测试先行 | 建立能表达需求或暴露缺陷的测试 | 确认验收含义 | 测试无法表达验收条件 |
| 实施 | 在批准范围内完成最小修改 | 裁决范围扩张、新契约和例外 | Spec、Standards 或范围需要变化 |
| 双轴评审 | 分开核对 Spec 与 Standards | 逐条裁决发现和风险 | 参照版本或测试证据无法对应代码 |
| 交付 | 汇总差异、测试、评审、决策和回滚 | 最终放行 | 不得以“AI 已完成”替代人工结论 |

### 2.3 `standards/`：质量导航层

`standards/` 说明“要满足哪类质量要求、应调用哪个检查 Skill、需要提供哪些输入和证据”。引导文件不复制 Skill 内的具体规则设置。

| 文件 | 定位 | 内容边界 |
| --- | --- | --- |
| `architecture.md` | 架构质量入口 | 指向分层、依赖方向、模块边界和所有权检查 |
| `clean-code.md` | Clean Code 引导文件 | 说明适用场景、调用哪个 Skill、输入、输出和验收方式；不保存阈值、检查步骤和规则正文 |
| `testing.md` | 测试质量入口 | 指向测试层级、测试先行、回归和证据检查 |
| `documentation.md` | 文档质量入口 | 指向任务产物、Spec 审批、版本变更和交付追溯检查 |

#### `clean-code.md` 与 Clean Code Skill 的唯一分工

`clean-code.md` 是给人和 Agent 阅读的导航页，只回答：当前任务是否需要 Clean Code 检查、调用哪个 Skill、调用前准备什么、人工验收什么。

Clean Code Skills 才是具体设置的唯一来源，负责保存并执行：

- 命名、职责、抽象层级、副作用与复用规则。
- 类/方法规模、复杂度和重复度的阈值或预警设置。
- 设计模式、SOLID、依赖与过度抽象的判断步骤。
- 扫描范围、固定命令、停止条件和输出字段。
- 规则 ID、严重级别和适用/例外判定方式。

不得在 `clean-code.md` 和 Skill 中各维护一套规则。引导文件只引用 Skill 名称和版本；具体规则变化只修改 Skill，并更新其版本或变更记录。自动校验读取 Skill 设置，不从引导文件解析阈值。

### 2.4 `templates/`：产物格式层

Templates 只统一格式，不预填业务答案：

| 模板 | 必填内容 |
| --- | --- |
| `task-card.md` | 场景、目标、输入、范围、非目标、验收条件、代码起点和所有者 |
| `spec.md` | 问题、范围、业务/设计决定、契约、验收条件、风险和审批记录 |
| `ticket.md` | 可独立验证行为、阻塞关系、修改边界、验收条件和验证命令 |
| `review.md` | 事实、假设、待确认项、复现命令、调用链、根因/决策和证据 |
| `impact.md` | 模块、类/方法、契约、数据、事务、调用方、测试、可改/禁改范围和批准 |
| `agent-task.md` | 已批准输入、目标代码/方法、测试接缝、执行步骤和停止条件 |
| `verification.md` | 代码版本、测试命令与结果、验收映射、Spec/Standards 结论和风险 |
| `code-review.md` | 独立的 Spec/Standards 矩阵、阻塞项和 Agent 建议 |
| `problem-review.md` | 成因、更早发现门禁和防止重复的改进动作 |
| `decision.md` | 人对每条 Finding 的决定、理由、例外或整改动作 |
| `delivery.md` | 最终提交、版本引用、测试评审证据、回滚和放行确认 |

### 2.5 `skills/`：具体设置与重复执行层

Skill 不是知识介绍，而是可重复执行的操作包。每个 Skill 必须明确触发条件、输入、版本校验、固定步骤、规则设置、写入范围、产物、命令、成功条件、人工批准点和停止条件。

| Skill | 职责 | 规定输入 | 固定产物 |
| --- | --- | --- | --- |
| `/grill-with-docs` | 复现问题或澄清需求 | 任务卡、截图/日志、架构、代码起点 | `01_review.md` |
| `/to-spec` | 将已确认决定写成可审核 Spec | review、业务决定、契约、非目标、验收条件 | Spec 草案与审批记录 |
| `/to-tickets` | 将已批准 Spec 拆成可独立验证的任务 | 已批准 Spec、Standards 版本、代码起点 | Ticket 文件 |
| `/implement` | 先建立失败测试，再实施最小变更 | impact、Agent 任务、Spec、Standards | 代码/测试差异、verification 草案 |
| `/code-review` | 对固定差异分别做 Spec 与 Standards 评审 | 差异起点、实现版本、Spec、Standards、测试证据 | 两张符合性矩阵与阻塞清单 |
| `/clean-names`、`/clean-functions`、`/clean-general`、`/clean-comments`、`/clean-tests` | 按变更内容执行 Clean Code 专项规则 | 固定差异、调用方、测试和各 Skill 自带规则 | 规则 ID、位置、证据、影响、建议和裁决状态 |

五个 Clean Code Skills 是 `/code-review` 的 Standards 专项能力，不能代替 Spec 评审，也不能自行批准例外。上游当前没有 Java 主 Skill，本项目不安装 Python/TypeScript 主 Skill，只使用五个专项 Skill 中语言无关的规则。`/tdd` 可作为 `/implement` 的子能力，不新增课程主入口。

## 3. 标准产物链

```text
evidence/<group>/<task>/
├── 01_review.md
├── 02_impact.md
├── 03_agent-task.md
├── 04_verification.md
├── code-review.md
├── 05_problem-review.md
├── 06_decision.md
└── 07_delivery.md
```

Review 与 Decision 必须分开：Review 提供事实、问题和建议；Decision 记录人的正式裁决。`07_delivery.md` 记录最终差异、提交、已批准 Spec 路径/版本、Standards ID/版本、测试证据、回滚、风险和后续动作。

## 4. 版本固定与直接追溯

不建立额外中间包。每项交付直接固定：

1. 已批准 Spec：路径、版本/提交、状态、批准人和时间。
2. 适用 Standards：ID、版本/提交和已批准例外。
3. 代码差异：起点提交/标签、被审提交和实际 Diff。
4. 验证证据：测试命令、真实结果、运行代码版本和证据路径。

`/code-review` 生成两张独立矩阵：

- `Spec 条目 → 代码符号 → 测试 → 真实结果 → 结论`
- `Standards 规则 ID → 代码符号 → 检查证据 → 例外/人工裁决 → 结论`

Spec 实质变化时新建版本并重新审批；Standards 变化时记录新版本。不得覆盖已被交付记录引用的版本。

## 5. 自动校验层

| 检查 ID | 检查内容 | 失败处理 |
| --- | --- | --- |
| `GOV-DOC-01` | 必需产物、固定标题和字段 | 缺失或空字段时阻断 |
| `GOV-AGENT-01` | 受影响 Module 的 `AGENTS.md` 是否已读取，局部约束是否与修改范围对应 | 未读取、规则冲突未裁决或局部规则放宽根规则时阻断 |
| `GOV-VERSION-01` | Spec、Standards、代码起点和被审版本 | 缺失、未批准或不可还原时阻断 |
| `GOV-TRACE-01` | 验收条件/规则到代码、测试、结果和裁决的追溯 | 无证据的“通过”阻断 |
| `GOV-ARCH-01` | 分层依赖、所有权和事务边界 | MUST 违反且无例外时阻断 |
| `GOV-CODE-01` | 调用 Clean Code Skill 执行其版本化规则和设置 | 阻塞项阻断；预警交人工裁决 |
| `GOV-TEST-01` | 目标测试、回归和全量验证是否对应被审代码 | 失败、未运行或版本不对应时阻断 |
| `GOV-REVIEW-01` | Spec 与 Standards 两张矩阵是否独立且完整 | 任一轴缺失时阻断 |

自动校验不能代替需求裁决、例外批准和最终放行。

## 6. 人与 Agent 的分工

| 对象 | 负责什么 | 不负责什么 |
| --- | --- | --- |
| 人 | 提供材料；批准需求、根因、范围和例外；裁决评审发现；最终放行 | 不替 Agent 追调用链、猜根因或填写重复报告 |
| Agent | 复现/澄清；定位；生成影响分析；批准后实施、测试、评审和汇总 | 不代替人批准 Spec、范围、例外或最终交付 |
| 自动校验 | 检查文档、字段、版本、依赖、代码规则、测试和追溯关系 | 不自动决定业务含义、例外风险或最终放行 |

## 7. 目标仓库结构

```text
仓库根目录
├── AGENTS.md
├── platform-contracts/
│   └── AGENTS.md                 # 公共契约的局部行为约束
├── platform-web-starter/
│   └── AGENTS.md                 # 平台 Web 组件的局部行为约束
├── business-wms/
│   └── AGENTS.md                 # 仓储业务的局部行为约束
├── training-server/
│   └── AGENTS.md                 # 应用装配的局部行为约束
├── docs/ai-governance/
│   ├── README.md
│   ├── workflow.md
│   ├── deliverables.md
│   ├── skills.md
│   ├── skills.sha256
│   ├── standards/
│   │   ├── architecture.md
│   │   ├── clean-code.md         # 只做引导；不保存具体规则设置
│   │   ├── testing.md
│   │   └── documentation.md
│   └── templates/
│       ├── task-card.md
│       ├── spec.md
│       ├── ticket.md
│       ├── review.md
│       ├── impact.md
│       ├── agent-task.md
│       ├── verification.md
│       ├── code-review.md
│       ├── problem-review.md
│       ├── decision.md
│       └── delivery.md
├── .agents/skills/
│   ├── grill-with-docs/SKILL.md
│   ├── to-spec/SKILL.md
│   ├── to-tickets/SKILL.md
│   ├── implement/SKILL.md
│   ├── code-review/SKILL.md
│   ├── clean-comments/SKILL.md   # 上游 Clean Code 专项 Skills
│   ├── clean-functions/SKILL.md
│   ├── clean-general/SKILL.md
│   ├── clean-names/SKILL.md
│   └── clean-tests/SKILL.md
└── scripts/validate-ai-governance.sh # 本地与 CI 共用校验入口
```

## 8. 当前落地状态

- 根及四个 Maven Module 的 `AGENTS.md` 已建立。
- Workflow、产物定义、四个 Standards 引导文件和四个 Templates 已建立。
- AIHero 主链路 Skills 已保留在项目级 `.agents/skills/`。
- 五个 Clean Code 专项 Skills 已从指定上游下载并记录提交与哈希，未修改上游内容。
- `scripts/validate-ai-governance.sh` 已提供文档、Module 规则、Skill 名称、Skill 哈希和禁用术语检查。
- 代码分层、复杂度、测试执行和 Spec/Standards 符合性仍应继续接入 Maven/CI；当前脚本只验证治理材料和 Skill 完整性。
