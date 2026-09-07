# T05 平台组件与 Skill 复用：学员任务卡

> 类型：组件与 Skill 复用。状态：可实操。对象：学员。
> 起点：讲师从 `s2-t05-start` 创建的本组独立分支。
> 限制：本任务不再实现新的仓储业务功能；必须从 T01～T04 的真实重复中提炼，不得把任务数据、业务答案或人工批准写死进 Skill。

## 1. 业务问题与目标

T01～T04 已经反复出现建档、读取治理入口、限制当前阶段写入、等待实施批准、固定验证版本、独立 Review 和记录人工 Decision 等长 Prompt。继续复制会导致不同 Agent、不同小组逐渐遗漏字段或改变停止点。

本任务要把稳定的**治理编排**提炼为项目级 Skills，同时继续复用 AI Hero Skills 完成需求澄清、规格化、拆票和实施；不把两者复制成同一套能力。课堂产出：

1. 找出可复用与不可复用内容。
2. 定义五个项目 Skill 契约：`work-item-start`、`discover`、`execute`、`review`、`decision`。
3. 只实现或严格评审其中一个 `work-item-start`。
4. 用 T01、T02 两类任务和两个独立 Agent 会话验证一致性与停止行为。

## 2. 建档：记录复用任务，不预写 Skill

新开会话执行：

```text
这是 T05 组件与 Skill 复用任务。请读取根 AGENTS.md，并按其文档路由和停止条件工作。
本轮只完成建档，不设计或创建 Skill，不修改业务代码、测试和治理规范。

Work Item ID：T05-Gxx
目标：从 T01～T04 的真实重复 Prompt 中提炼项目级治理 Skills，并验证一个 Skill。

请确认目录未被占用后创建 docs/work-items/T05-Gxx/：
- 使用模板生成 README.md，类型写“组件或 Skill 复用”，状态写 DISCOVERING；记录分支和提交。
- 创建 inputs/reuse-request.md，记录目标、验证对象为 T01/T02、当前尚未决定实现细节。
- 不得提前创建编号文档、Spec、Design、Interface 或 Tickets，不得虚构审批。

完成后停止，只报告文件、Git 版本和待确认项。
```

人工检查：目录和版本正确，且没有把单号、数量、T01 根因或 T02 规则写成 Skill 内容。

## 3. 找重复：从真实材料提取稳定机制

新开会话执行：

```text
请调查 T05-Gxx。读取 T01～T04 学员任务卡、治理 Workflow、Deliverables、Templates、现有
.agents/skills 和门禁脚本。只分析，不修改任何文件。把重复指令逐条写入 01_analysis.md，并分类：
1. 应进入项目 Skill 的稳定治理动作；
2. 继续调用 AI Hero Skill 的专业动作；
3. 只作为 Standards 参考的代码规则；
4. 交给自动门禁的确定性检查；
5. 必须留在 inputs/Spec 或由人决定的任务变量。
每项引用真实来源并说明错误归层的风险。完成后停止等待人工审核。
```

人工验收：稳定流程可以提炼；任务事实、业务决定和代码答案必须保留在 Work Item；人工批准永远不能由 Skill 自动产生。

## 4. 定义五个项目 Skill 契约

人工确认分类后，让 Agent 在 `01_analysis.md` 中为五个项目 Skill 设计契约，至少包含：

| 项目 Skill | 最小输入 | 允许输出 | 必须停止的情况 |
| --- | --- | --- | --- |
| `work-item-start` | Work Item ID、类型、原始事实/附件 | `README.md`、`inputs/` | 目录冲突、事实/附件无效、Git 版本未知 |
| `discover` | Work Item ID、任务类型 | `01_analysis.md` 及按需草案 | 关键未知、越权规则、范围无法确认 |
| `execute` | 已批准 Work Item 与当前 Ticket | 代码/测试、`02_verification.md` | 未批准、越界、测试失败、Spec 变化 |
| `review` | 固定起始/候选提交、实施依据 | `03_review.md` | 比较点无效、版本不一致、依据缺失 |
| `decision` | 人工原话决定及证据链接 | `04_decision.md` | 决策人/角色/版本不明或试图让 Agent 代批 |

每个契约还必须说明读取顺序、只写当前阶段、调用哪些 AI Hero/参考 Skills、输出模板、禁止动作和成功结果。项目 Skill 负责治理编排；`/grill-with-docs`、`/to-spec`、`/to-tickets`、`/implement`、`/code-review` 保留专业方法，不复制到项目 Skill 中。

## 5. 形成 Spec 与设计并批准范围

新开会话执行：

```text
/to-spec T05-Gxx。把已确认的复用分类、五个项目 Skill 契约、非目标、兼容要求和可执行验收
写入 spec.md；不得把 T01/T02 业务变量写死，不得让 Skill 批准自己的输出。
随后生成 design.md 与 interface.md，说明项目 Skill 如何调用 AI Hero/参考 Skill、如何使用模板、
如何与门禁分工，以及 .agents/skills/<skill-name>/SKILL.md 的文件边界。
本任务只实现或评审 work-item-start，Tickets 标记 N/A 并说明它是一个独立验证切片。
只写当前 Work Item 文档，不修改 Skill；完成后置为 WAITING_FOR_SCOPE_APPROVAL 并停止。
```

开发负责人审核 Spec、设计、五个契约和范围；AI 治理所有者确认项目治理契约。两者角色可由课堂同一人代理，但必须分别记录。UI 测试/QA 不参与实施前批准。

## 6. 实现或评审 `work-item-start`

获得批准后执行：

```text
/implement T05-Gxx 的 work-item-start 独立切片。
请只在 .agents/skills/work-item-start/ 下创建或更新 SKILL.md 及必需资源，不修改业务代码。
它必须读取 Work Item ID、任务类型、原始事实和可选附件，检查目录占用与 Git 信息，只生成
README.md 和 inputs/，并在冲突、缺材料、附件无效或敏感时停止。它不得生成 01_analysis.md、
Spec、代码、审批或 Decision，也不得复制 AI Hero 的需求分析方法。
完成后先做静态结构检查，再停止并报告变更和待运行场景；不得生成 Review 或自行批准。
```

如果起点中已有同名 Skill，不重复创建，改为依据批准契约评审并只修有证据的差距。

## 7. 两任务、两会话验证

使用临时测试 ID，不覆盖正式 Work Item：

1. 会话 A：用 Bug 类型和 T01 失败事实运行 `work-item-start`。
2. 会话 B：用需求调整类型和 T02 当前/目标规则运行同一 Skill。
3. 检查两者目录结构、必填字段、Git 版本、状态和停止点一致，业务输入彼此隔离。
4. 再各做一个失败用例：目录已占用、缺少必要事实或无效附件；Skill 必须停止且不得覆盖。
5. 清理测试产物前先保存验证日志到 `artifacts/`，按模板生成 `02_verification.md`。

只测一个任务、一个 Agent，或只看成功路径不能判定可复用。若输出漂移，先判断问题属于项目 Skill、AI Hero Skill、模板还是门禁，再修最小责任层并重测。

## 8. 独立 Review、MR 与人工决定

Verification 验收后形成候选提交并停止。新开会话对固定 `起始提交..候选提交` 运行 `/code-review`，生成 `03_review.md`：Spec 轴检查五个契约及 `work-item-start` 验收；Standards 轴检查 Skill 输入、写入权限、停止条件、任务变量泄漏、重复方法和安全边界。

人工裁决 Findings，关闭阻塞项并重新验证后，才推送并创建关联 Work Item 的草稿 MR/PR。人工决定由 Agent 原样记录到 `04_decision.md`；只有 `ACCEPTED` 且检查通过时，由有权限的人合并。

## 9. 必交付物

- [ ] `README.md`、`inputs/reuse-request.md`
- [ ] `01_analysis.md`：重复 Prompt 证据、五层分类和五个 Skill 契约
- [ ] 经批准的 `spec.md`、`design.md`、`interface.md` 和角色记录
- [ ] `work-item-start` 的实现或契约差距修正
- [ ] T01/T02、两个会话、成功/失败路径的 `02_verification.md` 与日志
- [ ] 固定 Diff 的 `03_review.md`、草稿 MR/PR 和人工 `04_decision.md`

## 10. 停止条件

- 没有引用 T01～T04 真实重复，或只凭想象设计通用 Skill。
- 把单号、数量、根因、业务规则或实现答案写死进 Skill。
- 项目 Skill 复制 AI Hero 方法、复制 Standards，或替人批准/决定。
- Skill 可以覆盖现有目录、越过阶段写文件、在材料不足时继续。
- 只在一个任务或一个会话验证，失败路径没有证据。
- Review 与候选提交不一致，或阻塞 Finding 未关闭。
