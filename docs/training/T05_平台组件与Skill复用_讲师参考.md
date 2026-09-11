# T05 项目级 Skill 复用沉淀：讲师参考

> 对象：讲师/助教。状态：与 `s2-t05-start` 配套。
> 投放限制：T05 的主任务是治理复用，不再抽取新的业务平台组件；仓库中早期审核 Skills 只能作为历史反例/局部参考，不能冒充五个项目 Skill 的完整答案。课堂先验证 `work-item-start`，随后仍须生成其余四个项目 Skills。

## 1. 教学目标与 PPT 对应

本节对应 PPT P54～P60：P54 发布任务，P55 识别重复 Prompt，P56 区分五层职责，P57 定义五个项目 Skill 契约，P58 路由 T01～T04，P59 实现并验证一个 Skill，P60 展示低 Prompt 结果。

本节要解决的是“同一治理流程被每个任务反复写成长 Prompt，随后逐渐漂移”。学员把项目内稳定的阶段权限、模板和停止条件做成薄编排。所有实现只能进入项目 `.agents/skills/`，不得修改第三方 Skills。

## 2. 课堂节奏

| PPT | 时间 | 学员动作 | 当页期待结果 | 不符合时 |
| --- | ---: | --- | --- | --- |
| P54 | 0—5 | 从 `s2-t05-start` 建档 | 明确不新增仓储功能，只提炼治理复用 | 跑去抽平台业务代码则停止 |
| P55 | 5—15 | 调查 T01～T04 重复 Prompt | 重复项都有文档证据，变量和规则被排除 | 业务答案进入 Skill 则退回 |
| P56 | 15—25 | 将内容分到五层 | 人、项目 Skill、AI Hero、参考 Skill、门禁职责不重叠 | 一层包办全部则重分 |
| P57 | 25—37 | 定义五个项目 Skill 契约并拆成五张 Tickets | 输入、读取、允许写入、输出、停止点完整；每个 Skill 可独立交付 | 能跨阶段写入、代批或 Tickets 标记 N/A 则不通过 |
| P58 | 37—55 | 路由 T01～T04 | Bug/需求调整/新增/重构使用不同专业路径 | 所有任务套同一链则重做 |
| P59 | 55—75 | 先实现 `work-item-start` 并跨任务验证，再生成其余四个 Skills | 两任务、两会话、成功/失败输出一致；五个入口均已生成并登记 | 只测一次、未登记版本或只生成一个 Skill 都不能推广 |
| P60 | 75—83 | 运行完整短命令链 | 人只给 ID、事实、附件和决定；每个 Skill 稳定停在自己的阶段边界 | 仍需粘贴长治理 Prompt 或任一入口不可执行则未完成 |

## 3. 五层职责标准答案

| 层 | 负责什么 | 明确不负责 |
| --- | --- | --- |
| 人 | 提供事实、回答业务问题、批准范围、裁决 Findings、决定交付 | 定位代码、复制流程细节 |
| 项目 Skill | 固定阶段入口、读取顺序、写入权限、模板、停止条件和下一步调用 | 发明业务规则、复制专业方法、自动批准 |
| AI Hero Skill | `grill-with-docs`、`to-spec`、`to-tickets`、`implement` 等专业动作 | 决定项目角色和越过治理阶段 |
| 参考 Skill | Clean Code、TDD、设计/评审方法等质量知识 | 改写项目范围或生成批准 |
| 自动门禁 | 必需文件/字段、依赖方向、规模、复杂度、测试与版本一致性 | 替代语义判断和人工验收 |

稳定术语、输入输出和停止条件可写进项目 Skill；单号、截图、业务数量、T01 根因、T02 新规则、类名答案必须留在 inputs、Spec 或当前分析中。

## 4. 五个项目 Skill 契约验收

- `work-item-start`：只能建 `README.md` 与 `inputs/`；目录冲突、Git 未知、附件无效时停止。
- `work-item-discover`：根据任务类型路由 Bug、需求调整、新增或重构；只写分析和条件产物草案，关键未知时停止。
- `work-item-execute`：必须读取明确的实施批准和当前 Ticket；只写批准代码/测试与 Verification。
- `work-item-review`：必须固定比较点；调用 `/code-review` 生成 Review，不允许边审边改或自批。
- `work-item-decision`：最小人工输入只有 Work Item ID 和人已经作出的最终决定原文；身份、时间、版本、MR/PR、目标分支、回滚与风险由当前会话、Git、Work Item 或企业项目管理事实系统解析，缺失的非阻塞元数据如实标注而不要求人重复填写；记录 Decision 和交付证据，不得自行合并。

课堂先通过 `/to-tickets T05-Gxx` 形成五张纵向 Ticket，再实现和验证 `work-item-start`，通过后依次生成其余四个。每次只生成一个 Skill，完成 `./scripts/validate-t05-skills.sh <skill-name>` 和停止行为检查后再进入下一个；不能一次性生成五个未经验证的 Skills，也不能把 Tickets 标记 N/A。

## 5. T01～T04 路由参考

| 任务 | 项目治理入口 | 专业 Skills | 裁剪 |
| --- | --- | --- | --- |
| T01 Bug | `/work-item-start` → `/work-item-discover` → `/work-item-execute` → `/work-item-review` → `/work-item-decision` | Bug 调查、`tdd`、`code-review` | 规则明确时无 Spec、无 Tickets |
| T02 需求调整 | 同一五阶段项目入口 | `/grill-with-docs`、`/to-spec`、`/implement` | 单一切片可无 Tickets |
| T03 从零新增 | 同一五阶段项目入口 | `/init-work-item`、`/grill-with-docs`、`/codebase-design`、`/to-spec`、`/to-tickets`、逐票 `/implement` | 多纵向 Tickets |
| T04 重构 | 同一五阶段项目入口 | `/improve-codebase-architecture`、`grilling`、`codebase-design`、`/to-spec`、`/to-tickets`、`/implement` | 纯重构不强造 Red |

## 6. `work-item-start` 验收用例

成功路径至少两个：独立会话分别执行 `/work-item-start <临时 T01 ID>` 与 `/work-item-start <临时 T02 ID>`，输入 T01 Bug 事实+截图和 T02 需求调整事实+截图。两个会话均应得到相同结构、必填字段、Git 信息、`DISCOVERING` 状态和阶段停止点，但业务输入不能串线；输入文件必须沿用模板的 `inputs/input-evidence.md`。

失败路径至少两个：目录已占用必须停止且不覆盖；缺必需事实或附件不可读必须记录原因并停止。Skill 不得生成 `01_analysis.md`、Spec、代码、Review、Decision 或批准字段。

推广前还要检查项目来源、版本、内容哈希和验证结果是否完整登记。只保存一份当前文件、没有可核对版本证据，不能证明不同 Agent 使用的是同一个 Skill。

若结果漂移，讲师引导定位责任层：编排/权限问题改项目 Skill，专业分析问题交 AI Hero，格式问题改模板，可确定结构问题交门禁。不要把所有修补继续堆入同一个 Skill。

`work-item-start` 通过后，讲师逐个检查 `work-item-discover`、`work-item-execute`、`work-item-review`、`work-item-decision` 的生成过程。重点不是目录存在，而是每个入口都能调用正确专业 Skill、只写当前阶段并在人工关口停止。最后至少用一个临时 Work Item 跑通五段短命令链，并对 T01～T04 的任务类型路由做只读核对。

## 7. 门禁、独立测试、QA 与答案标签

候选提交必须由新会话固定 Diff Review，重点查任务变量泄漏、跨阶段写入、自动批准和复制 AI Hero 方法。人工裁决后才创建草稿 MR/PR并记录 Decision。

课堂起点先运行 `./scripts/classroom-test.sh T05 baseline`，它应证明 T05 文档、模板、锁文件和门禁已就绪，同时五个答案 Skill 尚未泄漏。每张 Ticket 完成后运行对应的 `./scripts/validate-t05-skills.sh <skill-name>`；完整链路结束后运行 `./scripts/classroom-verify.sh T05`。

Review Findings 关闭后仍有两个独立角色关口：测试工程师验证五个 Skill 的功能行为并生成 `functional-test.md`；其通过后，QA 只审查流程、证据、版本和阶段权限，生成 `qa-review.md`。开发 TDD、测试工程师功能测试、QA 审查与业务验收不得合并成一个“测试通过”。任一结论对应的提交不同，必须重新确认受影响部分。

当前仓库中的 `review-requirement`、`review-code-change` 是早期示例，不包含 PPT 定义的五个项目 Skill。课堂中只能用它们展示“输入/步骤/输出/停止条件”的局部写法，不能直接复制为新入口。只有五个契约、五张 Tickets、五个项目 Skills、跨会话与完整链路验证、Review、功能测试、QA 和人工 Decision 全部完成后，才能创建 `s3-t05-answer`。
