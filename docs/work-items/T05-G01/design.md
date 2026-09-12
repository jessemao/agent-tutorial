# Design：项目级 Work Item 治理 Skills

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T05-G01 |
| 状态 | APPROVED |
| Spec 路径/版本 | `spec.md` v1.1 |
| 代码起点 | `add14f73bf857604e5f738fe35111ab4ef1b3e05` |
| Standards ID/版本 | 代码起点中的 AI Governance、Standards、Templates 和 Skills 锁定版本 |
| 所有者 | 用户分别代理 AI 治理所有者与开发负责人；具名信息待登记 |

## 问题与设计目标

- 要解决的能力缺口：同一项目治理主干在 T01～T04 通过长 Prompt 或多个阶段命令重复表达，缺少一个能在新会话中稳定恢复上下文、限制写入并路由专业能力的五阶段接口。
- 必须保持的行为：T01 手写教学方式；现有 AI Hero/参考 Skills 的专业职责和锁定内容；Templates、Workflow、Standards、人工批准、测试工程师、QA、业务验收和 MR/PR 权限边界。
- 非目标：实现新的仓储能力、建立全能 Agent、复制第三方 Skill、自动批准或把语义判断写进门禁。

## 现状与约束

- 相关 Module、类/方法和调用方：本任务没有业务代码 Module。实现位置限定为项目 `.agents/skills/<skill-name>/`；调用方是学员和处理 T01～T04 类型 Work Item 的 Agent；被调用能力包括 Templates、AI Hero/参考 Skills 和门禁脚本。
- 现有数据、事务、并发和异常语义：数据是 Git 版本化的 Work Item 文件及 Skill 锁记录。不存在业务事务；文件冲突、版本漂移和重复调用必须先检测后停止，不用覆盖模拟幂等。
- 不可修改的边界：第三方 Skill、T01 教学卡调用方式、业务代码/API/UI/数据库、未经批准的治理规范和人工结论。

## 方案

- 责任和依赖方向：
  - 人负责事实、业务/契约决定、范围批准、Finding 裁决、独立测试与 QA 签署、业务验收和最终放行。
  - 五个项目 Skill 只负责编排：读取、状态判断、写入权限、模板、专业能力调用、门禁和停止。
  - AI Hero/参考 Skills 负责需求质询、领域语言、设计、Spec、Tickets、TDD 和双轴评审方法。
  - Standards 提供代码、架构、安全和需求完整性规则。
  - 脚本只验证文件、标题、来源、哈希、构建和版本等确定性结果。
- 数据与状态变化：每个 Skill 只更新自己阶段允许的 Work Item 文件；实现 Tickets 才能新增对应 Skill 目录并更新 `skills-lock.json`。状态严格沿 Workflow 推进，等待人工时保持等待状态。
- 事务、幂等、锁顺序与并发：写入前读取当前 Git 和目标文件状态；若与已读取版本不同、目录被并发创建或已批准对象变化，停止并报告。重复运行只能在契约允许的同阶段更新，不得覆盖其他任务或重放人的签字。
- 异常与失败后最终状态：失败不创建后续阶段产物，不回滚或重写上一次有效证据；输出停止原因、已完成动作、版本和责任角色。门禁失败保持当前阶段。
- 可观测性与回滚：每次输出实际读取/写入文件、Git/对象版本、门禁结果、停止原因和下一入口；每个 Skill 一张 Ticket、一份独立提交和锁文件变化，支持逐 Skill 回退。

### 五个深模块

| Module | Interface | 隐藏的治理复杂度 | 调用的专业能力 |
| --- | --- | --- | --- |
| `work-item-start` | Work Item ID、类型、事实/附件 → 建档结果 | 任务卡定位、目录冲突、模板、Git、附件安全、未知字段 | Templates；不调用需求分析 Skill |
| `work-item-discover` | Work Item ID → 分析结果/待决定问题 | 任务类型路由、证据读取、事实/决定分离、设计适用性与下一入口判断 | `grill-with-docs`、`grilling`、`domain-modeling`；需设计时依次 `codebase-design` → 人工确认 → `to-spec`，无需设计时记录 N/A 后 `to-spec` |
| `work-item-execute` | Work Item ID 或 Ticket → 当前切片开发结果 | 批准校验、范围限制、TDD、回归、Verification、失败停止 | `implement`、`tdd`、按需参考 Standards Skills |
| `work-item-review` | Work Item ID 与固定 Diff → Review 结果 | 比较点解析、Spec/Standards 来源、两轴聚合、落盘、裁决交接 | `code-review` |
| `work-item-decision` | Work Item ID 与人工原话决定 → 自动解析 Git/Work Item/项目管理事实 → Decision 记录 | 原意保真、版本/证据一致性、来源可追溯、缺失字段显式化、零合并 | Decision Template；不调用自动决策能力 |

### 文件布局

```text
.agents/skills/<skill-name>/
├── SKILL.md
└── agents/
    └── openai.yaml
```

仅当单个 `SKILL.md` 无法清晰维护且 Ticket 明确批准时，才在同一 Skill 目录新增引用资源。`SKILL.md` 必须包含：输入、读取顺序、允许写入、固定产物、停止条件、禁止动作、下一入口和验收。`openai.yaml` 必须关闭隐式调用。

## 备选方案与取舍

| 方案 | 优点 | 代价/风险 | 结论 |
| --- | --- | --- | --- |
| 继续在每张任务卡复制长 Prompt | 无新实现 | 内容漂移、重复维护、新会话容易漏规则 | 拒绝 |
| 创建一个覆盖全流程的 `work-item` Skill | 单入口最短 | 自动跨人工关口、职责巨大、失败难定位和回退 | 拒绝 |
| 修改现有第三方 AI Hero Skills 加入项目规则 | 表面上文件更少 | 破坏上游来源/哈希，专业方法与项目权限耦合 | 拒绝 |
| 五个薄编排 Skill，各自一个阶段和 Ticket | 权限清楚、可独立验证/回退、复用既有能力 | 需要维护五个小契约和完整链路测试 | 采用 |
| 只实现 `work-item-start` | 课堂时间最短 | P60 的完整短命令链不可执行，其余长 Prompt 继续漂移 | 拒绝；仅作为首个验证样本 |

## 测试接缝

| 设计决定 | 验证方式 | 失败信号 |
| --- | --- | --- |
| 每个 Skill 只有阶段级写入权限 | 在临时 Git 工作区调用并比较调用前后文件集合与内容 | 出现未允许文件、覆盖其他任务或修改第三方 Skill |
| `work-item-start` 跨任务、跨会话稳定 | T01/T02 两个临时 ID 在两个新会话运行成功路径 | 结构漂移、输入串线、依赖旧聊天、生成后续产物 |
| 失败必须无越界副作用 | 目录冲突、缺事实、附件无效、未批准、测试失败、Diff 漂移、决定缺失等场景 | 失败后仍继续写入、推进状态或作出决定 |
| 专业方法只由既有 Skill 提供 | 静态检查引用和内容审查，核对第三方文件哈希 | 新 Skill 复制完整专业流程或修改第三方文件 |
| 显式调用和来源锁定 | `validate-t05-skills.sh <skill-name>` | 隐式调用开启、来源非项目、哈希缺失/过期、契约标题缺失 |
| 五段链可恢复且不跨人工关口 | 临时 Work Item 逐段调用，分别补充人工材料后继续 | 单次调用越过批准点、重进会话无法恢复或下一入口错误 |
| 最终治理材料一致 | `validate-ai-governance.sh`、`classroom-verify.sh T05`、`git diff --check` | 任一门禁失败、健康检查失败或记录版本不一致 |

## 待裁决项与批准

| 事项 | 所有者 | 结论 | 时间 |
| --- | --- | --- | --- |
| 五个薄编排 Module 及依赖方向 | AI 治理所有者代理 | APPROVED | 2026-09-11 11:35:58 CST |
| 五张 Ticket 顺序、文件范围和逐 Skill 回滚 | 开发负责人代理 | APPROVED | 2026-09-11 11:35:58 CST |
| 临时 Work Item/独立会话作为最高行为测试接缝 | 开发负责人代理；测试工程师仍待指定 | APPROVED_FOR_DEVELOPMENT | 2026-09-11 11:35:58 CST |
| 不修改第三方 Skill、T01 手写方式和业务代码 | AI 治理所有者及业务/规则所有者代理 | APPROVED | 2026-09-11 11:35:58 CST |
