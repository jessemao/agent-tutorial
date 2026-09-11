# T05 项目级 Skill 复用沉淀：学员任务卡

> 类型：组件与 Skill 复用。状态：可实操。对象：学员。
> 起点：讲师从 `s2-t05-start` 创建的本组独立分支。
> 限制：本任务不再实现新的仓储业务功能；必须从 T01～T04 的真实重复中提炼，不得把任务数据、业务答案或人工批准写死进 Skill，不得修改第三方 Skills。

## 1. 业务问题与目标

T01～T04 已经反复出现建档、读取治理入口、限制当前阶段写入、等待实施批准、固定验证版本、独立 Review 和记录人工 Decision 等长 Prompt。继续复制会导致不同 Agent、不同小组逐渐遗漏字段或改变停止点。

本任务要把稳定的**治理编排**提炼为项目级 Skills，同时继续复用 AI Hero Skills 完成需求澄清、规格化、拆票和实施；不把两者复制成同一套能力。课堂产出：

1. 找出可复用与不可复用内容。
2. 定义五个项目 Skill 契约：`work-item-start`、`work-item-discover`、`work-item-execute`、`work-item-review`、`work-item-decision`。
3. 先实现并严格验证 `work-item-start`，再按同一契约生成其余四个项目 Skills。
4. 用 T01、T02 两类任务和两个独立 Agent 会话验证一致性，再验证五个入口能够组成完整链路。

## 2. 完整操作路线

1. 确认讲师已从 `s2-t05-start` 创建 `T05-Gxx` 小组分支，手动启动课堂环境并检查 T05 起点。
2. 新会话建档，只生成 `README.md` 与 `inputs/input-evidence.md`。
3. 调查 T01～T04 的重复 Prompt，形成五层分类和五个 Skill 契约。
4. 由开发负责人和 AI 治理所有者分别批准范围与治理契约。
5. 生成 `spec.md`、`design.md`、`interface.md`，固定五个项目 Skill 的接口与停止条件。
6. 先生成 `work-item-start`，用 T01/T02 两任务、两会话和失败路径验证。
7. `work-item-start` 验证通过后，依次生成 `work-item-discover`、`work-item-execute`、`work-item-review`、`work-item-decision`。
8. 对每个 Skill 做静态门禁和阶段停止测试，再运行一次完整短命令链。
9. 固定候选提交，新会话执行 `/code-review T05-Gxx`，人工裁决 Findings。
10. 测试工程师独立验证五个 Skill 的功能行为；通过后再由 QA 独立审查流程、证据和可追溯性。
11. 测试与 QA 均形成结论后创建草稿 MR/PR，由业务负责人给出最终决定；Agent 只记录决定，不自行批准或合并。

## 3. 步骤 1：确认代码起点和当前行为

1. 确认当前分支由 `s2-t05-start` 创建，并记录分支和提交。
2. 手动启动课堂环境：

```bash
./scripts/classroom-up.sh
```

3. 确认健康检查和课堂页面可用，并确认可以查阅 T01～T04 的任务卡、Work Item、治理材料和现有项目 Skills。
4. 运行 T05 起点测试：

```bash
./scripts/classroom-test.sh T05 baseline
```

5. 确认仓库中已有模板、Skill 锁文件和 T05 门禁，但没有五个 T05 答案 Skills，也没有 `T05-Gxx` 的答案 Work Item。

若起点检查失败、五个目标 Skills 已经存在或包含答案，记录 Git 版本并停止。不得自行新建另一条分支绕过错误起点，也不得把环境错误当成 Skill 行为证据。

## 4. 步骤 2：建档，不预写 Skill

新开会话执行：

```text
这是 T05 组件与 Skill 复用任务。请读取根 AGENTS.md，并按其文档路由和停止条件工作。
本轮只完成建档，不设计或创建 Skill，不修改业务代码、测试和治理规范。

Work Item ID：T05-Gxx
目标：从 T01～T04 的真实重复 Prompt 中提炼项目级治理 Skills，并验证一个 Skill。

请确认目录未被占用后创建 docs/work-items/T05-Gxx/：
- 使用模板生成 README.md，类型写“组件或 Skill 复用”，状态写 DISCOVERING；记录分支和提交。
- 使用模板创建 inputs/input-evidence.md，记录目标、验证对象为 T01/T02、当前尚未决定实现细节。
- 不得提前创建编号文档、Spec、Design、Interface 或 Tickets，不得虚构审批。

完成后停止，只报告文件、Git 版本和待确认项。
```

人工检查：目录和版本正确，且没有把单号、数量、T01 根因或 T02 规则写成 Skill 内容。

## 5. 步骤 3：从真实材料提取稳定机制

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

## 6. 步骤 4：定义并批准五个项目 Skill 契约

人工确认分类后，让 Agent 在 `01_analysis.md` 中为五个项目 Skill 设计契约，至少包含：

| 项目 Skill | 最小输入 | 允许输出 | 必须停止的情况 |
| --- | --- | --- | --- |
| `work-item-start` | Work Item ID、类型、原始事实/附件 | `README.md`、`inputs/` | 目录冲突、事实/附件无效、Git 版本未知 |
| `work-item-discover` | Work Item ID、任务类型 | `01_analysis.md` 及按需草案 | 关键未知、越权规则、范围无法确认 |
| `work-item-execute` | 已批准 Work Item 与当前 Ticket | 代码/测试、`02_verification.md` | 未批准、越界、测试失败、Spec 变化 |
| `work-item-review` | 固定起始/候选提交、实施依据 | `03_review.md` | 比较点无效、版本不一致、依据缺失 |
| `work-item-decision` | Work Item ID 与人工最终决定原文 | `04_decision.md` | 决定原文缺失、证据冲突或试图让 Agent 代批；身份、时间、版本、MR/PR、目标分支、回滚和风险由 Git、Work Item 或企业项目管理事实系统解析，不要求手工重复输入 |

每个契约还必须说明读取顺序、只写当前阶段、调用哪些 AI Hero/参考 Skills、输出模板、禁止动作和成功结果。五个入口统一位于项目 `.agents/skills/`；项目 Skill 负责治理编排，`/grill-with-docs`、`/to-spec`、`/to-tickets`、`/implement`、`/code-review` 保留专业方法，不复制或修改这些第三方 Skills。

完成分类后，在当前会话发送：

```text
请依据已确认的五层分类，在 01_analysis.md 中补全五个项目 Skill 的输入、读取顺序、允许写入、固定产物、停止条件、禁止动作、下一入口和验收方式。只更新分析文档，不创建 Skill。完成后停在人工批准。
```

开发负责人确认实施范围，AI 治理所有者确认五个契约。两项结论必须分别记录，未全部批准不得生成 Skill。

## 7. 步骤 5：形成 Spec、设计和接口

新开会话执行：

```text
/to-spec T05-Gxx。把已确认的复用分类、五个项目 Skill 契约、非目标、兼容要求和可执行验收
写入 spec.md；不得把 T01/T02 业务变量写死，不得让 Skill 批准自己的输出。
随后生成 design.md 与 interface.md，说明项目 Skill 如何调用 AI Hero/参考 Skill、如何使用模板、
如何与门禁分工，以及 .agents/skills/<skill-name>/SKILL.md 的文件边界。
实施顺序固定为先完成 work-item-start，再依次完成 work-item-discover、work-item-execute、work-item-review 和 work-item-decision。
只写当前 Work Item 文档，不修改 Skill；完成后置为 WAITING_FOR_SCOPE_APPROVAL 并停止。
```

Spec 范围获批后，新开会话执行：

```text
/to-tickets T05-Gxx
```

必须生成五张纵向 Ticket，禁止标记 N/A：

- `tickets/01-work-item-start.md`
- `tickets/02-work-item-discover.md`
- `tickets/03-work-item-execute.md`
- `tickets/04-work-item-review.md`
- `tickets/05-work-item-decision.md`

每张 Ticket 必须能单独生成、校验和回退对应 Skill；不得把五个 Skill 合并成一张“大而全”Ticket。

开发负责人审核 Spec、设计、五个契约和范围；AI 治理所有者确认项目治理契约。两者角色可由课堂同一人代理，但必须分别记录。测试工程师与 QA 不参与实施前批准。

## 8. 步骤 6：生成并静态检查 `work-item-start`

获得批准后执行：

```text
/implement docs/work-items/T05-Gxx/tickets/01-work-item-start.md
请只在 .agents/skills/work-item-start/ 下创建或更新 SKILL.md 及必需资源，不修改业务代码。
它必须读取 Work Item ID、任务类型、原始事实和可选附件，检查目录占用与 Git 信息，只生成
README.md 和 inputs/，并在冲突、缺材料、附件无效或敏感时停止。它不得生成 01_analysis.md、
Spec、代码、审批或 Decision，也不得复制 AI Hero 的需求分析方法。
完成后先做静态结构检查，再停止并报告变更和待运行场景；不得生成 Review 或自行批准。
```

如果起点中已有同名 Skill，不重复创建，改为依据批准契约评审并只修有证据的差距。

生成后先执行：

```bash
./scripts/validate-ai-governance.sh
./scripts/validate-t05-skills.sh work-item-start
git diff --check
```

静态检查通过只说明目录和治理材料合格，不能代替下一步的跨会话行为验证。

## 9. 步骤 7：用两任务、两会话验证 `work-item-start`

使用临时测试 ID，不覆盖正式 Work Item：

1. 会话 A：用 Bug 类型和 T01 失败事实运行 `/work-item-start <临时 T01 ID>`。
2. 会话 B：用需求调整类型和 T02 当前/目标规则运行 `/work-item-start <临时 T02 ID>`。
3. 检查两者目录结构、必填字段、Git 版本、状态和停止点一致，业务输入彼此隔离。
4. 再各做一个失败用例：目录已占用、缺少必要事实或无效附件；Skill 必须停止且不得覆盖。
5. 清理测试产物前先保存验证日志到 `artifacts/`，按模板生成 `02_verification.md`。
6. 登记 Skill 的项目来源、版本、内容哈希和验证结果；四项齐全后才能把它作为项目入口推广。

只测一个任务、一个 Agent，或只看成功路径不能判定可复用。若输出漂移，先判断问题属于项目 Skill、AI Hero Skill、模板还是门禁，再修最小责任层并重测。

## 10. 步骤 8：依次生成其余四个项目 Skills

`work-item-start` 验证通过并登记版本后，每个 Skill 使用独立 Agent 会话，按顺序执行：

```text
/implement docs/work-items/T05-Gxx/tickets/02-work-item-discover.md
/implement docs/work-items/T05-Gxx/tickets/03-work-item-execute.md
/implement docs/work-items/T05-Gxx/tickets/04-work-item-review.md
/implement docs/work-items/T05-Gxx/tickets/05-work-item-decision.md
```

每次只生成一个 `.agents/skills/<skill-name>/`，读取已批准的 Ticket、`spec.md`、`design.md` 和 `interface.md`，不得顺手生成下一个 Skill。每个切片完成后分别运行 `./scripts/validate-t05-skills.sh <skill-name>` 与 `./scripts/validate-ai-governance.sh`，并人工检查：

| Skill | 必须证明的行为 | 必须停止的位置 |
| --- | --- | --- |
| `work-item-discover` | 按任务类型选择专业 Skill，并只生成分析和条件产物 | 范围批准前 |
| `work-item-execute` | 读取实施批准和当前 Ticket，只修改批准范围并记录验证 | Review 前 |
| `work-item-review` | 固定起始/候选提交，调用 `/code-review` 并生成 `03_review.md` | 人工裁决前 |
| `work-item-decision` | 原样记录人工决定及交付证据 | 合并前，永不自行合并 |

任何一个 Skill 越过停止点，都要只修当前切片并重新验证；不得用后续 Skill 掩盖前一阶段的问题。

## 11. 步骤 9：验证完整短命令链

使用一个临时 Work Item 验证以下链路，每一步都检查产物、状态和停止点后再提供人工批准或决定：

```text
/work-item-start <临时 Work Item ID>
/work-item-discover <临时 Work Item ID>
/work-item-execute <临时 Work Item ID 或 Ticket 路径>
/work-item-review <临时 Work Item ID> <固定起始提交>..<候选提交>
/work-item-decision <临时 Work Item ID>
```

再对 T01～T04 四类任务分别检查专业 Skill 路由，不要求重复完成四次业务实施。缺材料、未批准、无效比较点或缺少人工决定时，入口必须稳定停止。完整结果写入 `02_verification.md`，临时产物清理前保存日志。

最后执行统一门禁：

```bash
./scripts/classroom-verify.sh T05
```

## 12. 步骤 10：形成候选提交并独立 Review

Verification 验收后提交五个项目 Skills、测试和 Work Item 文档，形成固定候选提交并停止。新开会话运行：

```text
/code-review T05-Gxx
```

必须生成 `03_review.md`。Spec 轴检查五个契约、五个入口与完整链路；Standards 轴检查 Skill 输入、写入权限、停止条件、任务变量泄漏、重复方法和安全边界。被审起始提交、候选提交和实际 Diff 必须一致。

## 13. 步骤 11：测试工程师独立功能测试

代码 Review 的 Findings 处理完成并固定新候选提交后，切换到测试工程师角色。测试工程师不得补写实现或代替 QA，只验证五个项目 Skill 的功能行为：正常输入、失败停止、跨会话隔离、完整短命令链和 T01～T04 路由。结果按模板写入 `functional-test.md`，记录被测提交、环境、用例、实际结果和结论。失败则退回开发，不进入 QA。

## 14. 步骤 12：QA 独立审查

功能测试通过后，切换到 QA 角色。QA 不重复开发 TDD，也不代替测试工程师执行功能用例；只审查 Spec、Tickets、Verification、Review、功能测试、版本与审批链是否一致，五个 Skill 是否遵守阶段权限和可追溯要求。结果按模板写入 `qa-review.md`。证据缺失或版本不一致时退回，不得给出业务 Decision。

## 15. 步骤 13：MR/PR 与人工 Decision

代码 Review、测试工程师与 QA 各自形成可追溯结论后，才推送并创建关联 Work Item 的草稿 MR/PR。业务负责人只需提供最终决定原文；Agent 从当前会话、Git、Work Item 和已配置的企业项目管理事实系统解析身份、时间、对象版本、目标分支、MR/PR、回滚及剩余风险，并原样记录到 `04_decision.md`。无法取得的非阻塞元数据必须标明来源未配置或尚未创建，不得反复要求人手工填写；证据冲突仍须停止。只有 `ACCEPTED` 且检查通过时，由有权限的人合并。

## 16. 必交付物

- [ ] `README.md`、`inputs/input-evidence.md`
- [ ] `01_analysis.md`：重复 Prompt 证据、五层分类和五个 Skill 契约
- [ ] 经批准的 `spec.md`、`design.md`、`interface.md`、五张 Tickets 和角色记录
- [ ] 五个项目 Skills 的实现；每个 Skill 都有独立生成和停止证据
- [ ] T01/T02、两个会话、成功/失败路径的 `02_verification.md` 与日志，以及来源/版本/哈希/验证登记
- [ ] 固定 Diff 的 `03_review.md`、独立的 `functional-test.md` 与 `qa-review.md`
- [ ] 草稿 MR/PR 和人工 `04_decision.md`

## 17. 停止条件

- 没有引用 T01～T04 真实重复，或只凭想象设计通用 Skill。
- 把单号、数量、根因、业务规则或实现答案写死进 Skill。
- 项目 Skill 复制 AI Hero 方法、复制 Standards，或替人批准/决定。
- Skill 可以覆盖现有目录、越过阶段写文件、在材料不足时继续。
- 只在一个任务或一个会话验证，失败路径没有证据。
- 只生成 `work-item-start`，其余四个停留在文档契约，完整短命令链不可执行。
- 把开发 TDD、测试工程师功能测试、QA 审查或业务验收合并成同一结论。
- Review 与候选提交不一致，或阻塞 Finding 未关闭。
