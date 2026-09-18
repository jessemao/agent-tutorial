# T03 “从 0 新建库内移库” AI Hero Skills 流程调研

> 调研日期：2026-09-03  
> 范围：课件 P41–P46、T03 任务材料与 [AI Hero Skills 官方页](https://www.aihero.dev/skills)  
> 事实来源约束：有关 AI Hero Skill 的行为、输入、输出与边界，只依据 AI Hero 官方详情页。

## 结论摘要

T03 不应替换现有五步主链，而应纠正两个层次：

1. **把“做设计决定”放回 `/to-spec` 之前。** P42–P43 现在容易让人理解为 `/to-spec` 负责定义命令、API、事务、锁顺序和测试接缝。但 AI Hero 明确说明：`/to-spec` 不访谈、不做新决定、不做验证，只把已经达成的决定合成为 Spec；它会在写 Spec 前先提出测试接缝并请人确认。因此，T03 必须在 `/grill-with-docs` 阶段完成业务和设计决定，并用 `codebase-design` 作为接口/测试接缝的参考词汇，再进入 `/to-spec`。来源：[/to-spec](https://www.aihero.dev/skills-to-spec)、[/codebase-design](https://www.aihero.dev/skills-codebase-design)。
2. **把 `tdd` 显式化为 `/implement` 内部的必选引擎，而不是再加一个串行大步骤。** AI Hero 的 `/implement` 会按已同意的接缝驱动 `tdd`，逐个纵向切片经历红灯→最小绿灯。`tdd` 本身是参考规则，不是驱动整个实施的步骤。来源：[/implement](https://www.aihero.dev/skills-implement)、[/tdd](https://www.aihero.dev/skills-tdd)。
3. **唯一建议默认新增到 T03 的 Skill 是 `codebase-design`，但作为参考层嵌入设计审核，不要独立“跑一遍”。** 它专门统一 module、interface、seam、adapter、depth 等词汇，且把“Interface 就是测试面”当作原则，正好覆盖 T03 从 `InventoryOperations.transfer()` 公开入口到领域内部的设计问题。官方同时警告它是参考而非流程，应让 `/grill-with-docs` 或 `tdd` 做驱动器。来源：[/codebase-design](https://www.aihero.dev/skills-codebase-design)。
4. **`domain-modeling`、`codebase-design` 和 `tdd` 已是本仓库安装能力，但课程应显式检查它们是否被真正加载。** AI Hero 官方指出 `/grill-with-docs` 依赖 `grilling` 和 `domain-modeling`，而且在其他编排层中可能只发生访谈、不落文件；官方也指出 `tdd` 依赖 `codebase-design`。本轮调研后已从官方仓库补齐 `codebase-design`，来源提交和哈希见 [`skills-lock.json`](../../skills-lock.json)。来源：[/grill-with-docs](https://www.aihero.dev/skills-grill-with-docs)、[/domain-modeling](https://www.aihero.dev/skills-domain-modeling)、[/tdd](https://www.aihero.dev/skills-tdd)。
5. **AI Hero 当前没有一个可替代 T03 人工验收与真实测试证据的默认 Skill。** `/code-review` 的官方定位是分开比对 Standards 与 Spec，不是竞态、空值、off-by-one 等缺陷搜寻；`/implement` 也不会替你勾选验收条件或关闭 Ticket。所以 P45–P46 的两端数量、双流水、幂等、失败回滚、并发和全量回归证据必须保留，且仍由人工做交付裁决。来源：[/code-review](https://www.aihero.dev/skills-code-review)、[/implement](https://www.aihero.dev/skills-implement)。

### T03 本次的三层裁决

| 层级 | Skill | 本次结论 |
| --- | --- | --- |
| **Default** | `codebase-design` | **该默认用**，但作为 `/grill-with-docs` / `tdd` 下的设计参考，不单独跑流程；本轮调研后已从官方仓库补齐。 |
| **Default** | `domain-modeling` | **该默认用**，已安装；它是 `/grill-with-docs` 的稳定术语/ADR 依赖，本次应显式核对已加载。 |
| **Default** | `tdd` | **该默认用**，已安装；作为 `/implement` 内部引擎，在已批准 seam 上逐个纵向切片红→绿，不另设顶层阶段。 |
| **Conditional** | `to-questionnaire` | **不默认**；仅当决定在不在场的单一责任人脑中、grilling 无法继续时使用。 |
| **Conditional** | `prototype` | **不默认**；仅当一个具体状态模型/UI 问题靠讨论无法决定时短暂切入，原型代码不进 main。 |
| **Not default** | `wayfinder` | **本次不该默认用**；T03 是路线可在单次课堂规划会话收敛的有边界功能。只有计划本身都跨多会话且路线仍模糊时才升级到 Wayfinder。 |

## P41–P46 现状与调整点

核对对象：工作区外 `../../培训教程/PPT成品/AI_Coding_V0.7_两天课程_Agent优先版.pptx` 中 P41–P46。

| 页面 | 当前表达 | 判断 | 建议调整 |
| --- | --- | --- | --- |
| P41 | 先补业务语义，再设计实现路径 | 方向正确 | 在“范围与不变量”后加一句：用 `codebase-design` 词汇确认公开 Interface 和测试 seam。 |
| P42 | “语义质询 → `/to-spec` → 领域/API 设计” | **顺序需改** | 调为“语义质询 + 领域/API/事务设计 → 人工确认 → `/to-spec` 固化决定与测试接缝”。 |
| P43 | “`/to-spec` 定义命令、API、事务、锁顺序……” | **与官方边界不一致** | 改为“在 `/grill-with-docs` 内借助 `codebase-design` 完成设计决定；`/to-spec` 只记录已确认命令、API、事务、锁顺序与 seam”。实施步骤写明 `/implement` 内部驱动 `tdd`。 |
| P44 | 把五步 Skill 作为唯一 AI 链 | 可保留 | 将 `codebase-design` / `domain-modeling` / `tdd` 标为“嵌入式参考能力”，不与五步并排为新的线性阶段。 |
| P45 | 未批准库存口径、锁顺序、事务边界就停止 | 正确 | 增加 seam 停止条件：公开验证面未确认时，`tdd` 不应开始写测试。 |
| P46 | 交付 Spec、测试骨架、参考分支 11 个业务测试 | 正确，但不能把 Skill 输出当验收 | 保留当前真实测试和人工裁决；将 `/code-review` 限定为 Spec/Standards 双轴，另外做缺陷、并发和 QA 复测。 |

## 建议的 T03 流程

```text
读取 T03 任务卡、现有领域代码和架构约束
  → /grill-with-docs
      · 显式确认 grilling + domain-modeling 已加载
      · 读仓库可回答的事实，人只裁决业务和设计选择
      · 嵌入 codebase-design 参考，确认 InventoryOperations.transfer()
        的 Interface、不变量、错误、顺序约束与测试 seam
      · 只在真正阻塞时走条件分支：
          - 决定在业务责任人脑中：/to-questionnaire → 收回答案后继续 grill
          - 状态模型/交互无法靠讨论定：/prototype → 带一句结论回到 grill
          - 外部规范/驱动/数据库事实阻塞：/research → 将引用文件带回
  → 人工确认业务规则和设计决定
  → /to-spec（只固化已确认决定，并确认尽量少的测试 seam）
  → 批准 Spec
  → /to-tickets（纵向 tracer bullets，每张可独立演示和验证）
  → /implement（每张 Ticket 一次，内部驱动 /tdd）
  → 目标测试 + Module 回归 + 全量验证 + 并发/QA 复测
  → 在新会话中以固定 Diff 起点运行 /code-review
  → 人工逐条裁决并关闭验收项
```

上述链路与 AI Hero 的官方分层一致：五步主链仍是主干；`prototype` / `research` 是 Shaping 分支；`codebase-design` / `domain-modeling` / `tdd` 是被其他 Skill 调用的参考层。来源：[AI Hero Skills 总览](https://www.aihero.dev/skills)。

## 候选 Skill 对比

| Skill | T03 使用阶段 | 主要输入 | 主要输出 | 官方边界/风险 | T03 是否默认 |
| --- | --- | --- | --- | --- | --- |
| [`codebase-design`](https://www.aihero.dev/skills-codebase-design) | `/grill-with-docs` 内的领域/API/测试接缝设计 | 已知需要设计的模块与业务能力 | 精确词汇和原则：module、interface、depth、seam、adapter、leverage、locality；自身不生成文档 | 是参考，不是过程；单独指挥它“开始工作”会自行即兴一条无停止规则的流程 | **是，默认加载为参考层** |
| [`domain-modeling`](https://www.aihero.dev/skills-domain-modeling) | 需求发现和建模 | 会话、现有代码、`CONTEXT.md`/相关 ADR | 已确认术语即时写入 `CONTEXT.md`；真正难逆、意外且有取舍的决定才提议 ADR | 不会替业务人员发明领域词汇；`CONTEXT.md` 只是词汇表，不存 Spec/实现细节；自动加载有失败记录 | **是，作为 `/grill-with-docs` 依赖并显式核对加载** |
| [`tdd`](https://www.aihero.dev/skills-tdd) | 批准 seam 后的实施 | 可观测的具体行为、明确输入/输出、已同意 seam | 一次一个测试的红灯→最小绿灯纵向切片；自身不写流程文件 | 参考而非驱动器；行为未明确时要回 `/to-spec`；对配置/粘合/CRUD 是官方已知缺口；依赖 `codebase-design` | **是，在 `/implement` 内默认启用** |
| [`to-questionnaire`](https://www.aihero.dev/skills-to-questionnaire) | `/grill-with-docs` 卡在不在场的业务/平台责任人时 | 单一收件人角色；需从对方得到的决定/事实列表 | 当前目录的 `to-questionnaire-<slug>.md`，问题按重要度和主题排序 | 每次只面向一人，非分支问卷，不会自动发送；最好在原 grilling 会话中运行 | **否，责任人不在场才用** |
| [`prototype`](https://www.aihero.dev/skills-prototype) | 设计阶段出现“靠讨论无法确定”的状态模型或 UI 问题 | 一句可回答的具体问题 | 逻辑分支产出单个可分享 HTML；UI 分支产出多个结构不同的方案；保留一句决策结论和独立 prototype 分支 | 无测试、无持久化、无生产级错误处理，分支不合入 main；不能用来原型化整个应用 | **否，只有一个具体设计问题时短暂分支** |
| [`research`](https://www.aihero.dev/skills-research) | 外部事实阻塞设计决定时 | 窄且可回答的外部事实问题 | 仓库既有约定位置中的单个带引用 Markdown 文件 | 只用一手来源；不做决定；文件不会被未来会话自动加载；官方记录了递归派生背景任务的已知问题 | **否，只在外部事实阻塞时用** |
| [`wayfinder`](https://www.aihero.dev/skills-wayfinder) | 路线模糊且需多个 agent 会话才能做完的大型规划 | 能说出目的地，但无法说清路径的大任务 | 一张 map issue + 决策 Tickets，包括 destination、已做决定、fog、out of scope 和依赖边 | 只计划不实施；是整套中最重的流程；官方明确以“能否在一个会话完成规划”区分它和 `/grill-with-docs` | **否**；T03 的需求/设计阶段可在单次课堂会话收敛，实施跨会话不等于规划必须用 Wayfinder |
| [`handoff`](https://www.aihero.dev/skills-handoff) | 必须换 harness、换目录/仓库、交给同事或派生侧任务时 | 当前长会话和“下一次会话要做什么” | OS 临时目录中的一个 Markdown 交接文件 | 买到的是可携带性，不是普通压缩；同 harness/同目录下续做不需要；临时文件可消失 | **否**；T03 Day 1→Day 2 应优先靠已经入库的 Work Item、Spec、Tickets、Diff 和测试证据恢复 |
| [`ask-matt`](https://www.aihero.dev/skills-ask-matt) | 不知道下一个 Skill 时 | 当前情境 | 推荐下一个 Skill/流程后停止 | 它是手写路由表，不扫描实际安装，也不运行被推荐 Skill；官方记录了路由摘要与详细 Skill 行为不一致的风险 | **否，不应是固定课程步骤** |

## 对现有五步主链的保留与边界

| 主链 Skill | T03 中应保留的职责 | 不能承担的职责 |
| --- | --- | --- |
| [`/grill-with-docs`](https://www.aihero.dev/skills-grill-with-docs) | 从仓库事实和人工决定中建立共同理解，同步稳定术语和少数合格 ADR | 不会自动保存大多数决定；普通决定仍只在会话中，所以必须紧接 `/to-spec` |
| [`/to-spec`](https://www.aihero.dev/skills-to-spec) | 把已达成的范围、非目标、业务规则、设计决定和已同意 seam 固化为可跨会话使用的 Spec | 不重新访谈，不做新决定，不验证决定，不应替新需求补齐业务语义 |
| [`/to-tickets`](https://www.aihero.dev/skills-to-tickets) | 把 T03 拆成单会话可完成、跨层且能独立演示的 tracer bullets，发布前由人审核粒度和 blocker | 不应按数据层/API/测试层水平切割；不能假定自己产生的验收条件都可失败，必须人工检查 |
| [`/implement`](https://www.aihero.dev/skills-implement) | 每次实施一张已决定 Ticket，按 seam 驱动 `tdd`，类型检查、单测、全量测试后提交 | 不重开设计，不自动勾选/关闭 Ticket；官方记录了“未提交 Diff 在内置 Review 中不可见”问题，所以建议独立新会话再评审 |
| [`/code-review`](https://www.aihero.dev/skills-code-review) | 对明确固定点到 HEAD 的已存在 Diff 分别做 Standards 与 Spec 审核 | 不是通用缺陷搜寻；不代替并发、回滚、UI/QA 复测和人工交付裁决；官方建议新会话以降低自证偏差 |

## 落地优先级

### P0：必做

1. 已将 `codebase-design` 加入项目级 Skill 安装与 `skills-lock.json`；它是当前已安装 `tdd` 的官方前置。
2. 修正 P42–P43 的语义/设计顺序：`/to-spec` 只记录决定，不负责发明决定。
3. 在 T03 设计评审中默认引用 `codebase-design`，明确 `InventoryOperations.transfer()` 的 Interface 与测试 seam；但不把它新增为第六个主链命令。
4. 在运行 `/grill-with-docs` 时显式核对 `grilling` 和 `domain-modeling` 加载；在 `/implement` 时显式要求仅在已批准 seam 上应用 `tdd`。
5. 保留 T03 的测试矩阵、真实运行证据、Spec/Standards 双轴审核与人工裁决，不以 Skill “PASS”代替验收。

### P1：条件式加入

- `to-questionnaire`：只在业务、平台或 UI 责任人不在场、决定无法继续时使用。
- `prototype`：只给单个无法靠讨论确定的状态模型/UI 问题；不应把整个移库功能做成原型。
- `research`：只在库外的规范、数据库/驱动语义等外部事实阻塞决定时使用；库内事实仍由 `/grill-with-docs` 直接读取。

### P2：不建议加入固定 T03 链路

- `wayfinder`：T03 是一个可在单次课堂规划会话中收敛的有边界功能，用官方最重的多会话决策地图会增加流程开销。
- `handoff`：Day 1 到 Day 2 的同仓库恢复已有持久化的 Work Item、Spec、Tickets、Diff 和测试证据；不应额外依赖可消失的临时 handoff 文件。
- `ask-matt`：可用于自学时路由，但不产生工件、不运行 Skill，且只知道自家 Skill，不适合成为固定课程环节。

## 官方来源

- [AI Hero Skills 总览](https://www.aihero.dev/skills)
- [/grill-with-docs](https://www.aihero.dev/skills-grill-with-docs)
- [/to-spec](https://www.aihero.dev/skills-to-spec)
- [/to-tickets](https://www.aihero.dev/skills-to-tickets)
- [/implement](https://www.aihero.dev/skills-implement)
- [/code-review](https://www.aihero.dev/skills-code-review)
- [/wayfinder](https://www.aihero.dev/skills-wayfinder)
- [/prototype](https://www.aihero.dev/skills-prototype)
- [/research](https://www.aihero.dev/skills-research)
- [/to-questionnaire](https://www.aihero.dev/skills-to-questionnaire)
- [/handoff](https://www.aihero.dev/skills-handoff)
- [/ask-matt](https://www.aihero.dev/skills-ask-matt)
- [/codebase-design](https://www.aihero.dev/skills-codebase-design)
- [/domain-modeling](https://www.aihero.dev/skills-domain-modeling)
- [/grilling](https://www.aihero.dev/skills-grilling)
- [/tdd](https://www.aihero.dev/skills-tdd)
