# 01 Analysis：T05-G91 项目级 Work Item 状态查询 Skill

## 当前状态

- 阶段：IMPLEMENTING / 正式实施依据已批准，执行单一纵向切片
- 代码起点：`bfba7413ed5fc95a34b8cd0110fbc4da48b01795`
- 当前分支：`T05-FULL-CHAIN-ACCEPTANCE`
- 建档验收：CONFIRMED；不构成范围或实施批准
- 当前执行入口：`/work-item-execute T05-G91`
- 批准原话：`全部通过，批准。/work-item-execute T05-G91`
- 完成后的唯一合法下一动作：开发负责人验收当前单切片
- 禁止自动 Review、提交、推送、独立测试、QA 和 Decision

## 已读取的权威材料

- 根 `AGENTS.md`、Workflow、Deliverables、Roles and Approvals、Documentation Standards。
- Work Item Template、当前 `README.md` 和 `inputs/input-evidence.md`。
- 五个项目阶段 Skills 及 `skills-lock.json`。
- T05 学员任务卡和项目 Skills 契约。

## 仓库可证事实

1. 当前没有名为 `work-item-status` 的项目 Skill 或锁文件登记。
2. Work Item Template 定义了状态、版本、产物适用性、当前下一步及“待确认事项与负责人”表。
3. Workflow 定义的状态不仅来自单一字段，还受阶段产物、人工确认、失败/阻塞和对象版本一致性约束。
4. 五个项目阶段 Skill 都要求响应报告实际状态、版本、停止原因、待确认项和下一入口，但它们各自拥有不同写入权限。
5. 项目 Skill 必须显式调用、位于 `.agents/skills/`、登记项目来源和内容哈希；第三方 Skill 不得修改。
6. 文档标准要求事实、历史、假设和待确认项分开；版本或产物冲突时必须停止，不能猜测状态。

## 事实、假设与未知项

| 类型 | 内容 | 处理 |
| --- | --- | --- |
| 事实 | 目标能力是只读状态报告 | 来自 `inputs/input-evidence.md` |
| 事实 | 至少覆盖 DISCOVERING 与等待人工裁决两种任务 | 来自用户输入 |
| 假设 | 使用者同时包括人和后续 Agent | 必须由治理/契约所有者确认 |
| 假设 | README 是入口，但必须与阶段文档和 Git 交叉核对 | 必须确认来源优先级和冲突行为 |
| 未知 | 精确输出字段、机器/人类格式、错误码、漂移处理和下一入口映射 | 通过 `grill-with-docs` 收敛 |

## 初步影响范围

- 预期新增：`.agents/skills/work-item-status/SKILL.md`、`agents/openai.yaml` 和对应 `skills-lock.json` 登记。
- 可能需要当前 Work Item 的 Spec、Design、Interface、开发验证和测试 artifacts。
- 不应修改：五个已验证阶段 Skills、第三方 Skills、业务代码/测试、治理规范、Templates 和任务卡。
- 首个验证接缝：使用隔离的临时 Work Item 文档，验证正常状态、等待裁决、版本冲突和缺文件路径；不得改写被查询任务。

## 候选方向（未批准）

- 候选 A：Skill 直接读取 Work Item 文件和 Git，只返回规范化状态报告，零仓库写入。推荐作为最小能力。
- 候选 B：新增脚本作为确定性解析器，再由 Skill 包装。只有状态规则能完全确定且多个调用方需要时才有价值；当前证据不足。
- 候选 C：把状态逻辑加入现有五个阶段 Skills。会造成重复和修改已验收入口，不推荐。

## `grill-with-docs` 问答记录

### Round 1（已确认）

1. 谁是 `work-item-status` 的调用者，以及输出是否必须同时服务人和 Agent。
2. 最小成功输出需要哪些字段。
3. README、阶段文档和实际 Git 不一致时采用何种权威顺序与停止行为。

人工原话：`全部同意推荐`。

确认结论：

- 同时服务人和 Agent，输出结构稳定的可读文本；本轮不增加 JSON、API 或持久化缓存。
- 最小输出包含 ID/阶段、记录与实际版本、漂移、未决事项/负责人、产物、停止原因、唯一下一入口和证据路径；不展开业务全文或附件。
- README 是导航入口，阶段文档和 Git 是交叉证据；冲突时返回 `WORK_ITEM_STATUS_CONFLICT`，列出来源、不给下一入口且零写入。

### Round 2（已确认）

4. “只读”是否意味着 Skill 对仓库绝对零写入，包括不得修正 README、补建缺失文档、更新状态或保存查询报告。
5. 唯一下一入口是否只能依据 Workflow、现有产物、人工确认和停止条件共同计算，遇到多个可能入口时返回冲突而不是任选其一。
6. 是否采用稳定错误集合：任务不存在、输入/阶段证据不完整、状态/版本冲突、证据受限；所有错误都零写入且不返回可执行下一入口。
7. 验收是否覆盖 DISCOVERING、等待人工裁决、版本漂移、任务不存在四类隔离场景，并比较查询前后文件树与哈希证明零写入。

人工原话：`全部同意推荐`。

确认结论：

- Skill 对仓库绝对零写入；不得修正文档、补建产物、更新状态或保存报告。
- 下一入口必须由 Workflow、产物、人工确认和停止条件唯一确定；不唯一时返回冲突。
- 固定 `WORK_ITEM_NOT_FOUND`、`WORK_ITEM_STATUS_INCOMPLETE`、`WORK_ITEM_STATUS_CONFLICT`、`WORK_ITEM_EVIDENCE_BLOCKED`；错误均零写入且不给可执行入口。
- 验收覆盖 DISCOVERING、等待人工裁决、版本漂移和任务不存在，并以前后文件树与哈希证明只读。

### Round 3（已确认）

8. 是否采用按阶段证据计算下一动作的固定顺序：建档后 discover；范围未收敛继续 grill；范围批准后 execute；开发完成后由人固定候选；候选后 review；Finding 后人工裁决；Review 通过后依次独立测试、QA、业务验收；证据齐全后 decision。
9. 负责人尚未具名但阶段事实一致时，是否正常返回当前状态，并把唯一下一动作设为“由讲师/交付负责人指定对应角色”，而不是把任务误判为版本冲突或擅自代签。
10. 是否将实现限制为一个显式、项目来源的 `work-item-status` Skill 包及锁文件登记，不增加脚本、不修改现有五个入口/治理/模板，并作为一个无需拆票的独立切片。

人工原话：`全部同意推荐`。

确认结论：

- 下一动作严格按建档、范围收敛、实施、候选、Review、Finding 裁决、独立测试、QA、业务验收和 Decision 顺序计算，不跨级推荐。
- 阶段事实一致但负责人未具名时，返回真实状态，唯一下一动作是由讲师/交付负责人指定相应角色；不误报冲突、不代签。
- 实现仅允许新增 `work-item-status` Skill 包、锁文件登记和当前 Work Item 开发验证证据；不增加脚本、不修改现有五入口/治理/模板/第三方 Skill，并裁剪为一个独立切片。

## 范围收敛摘要（已确认共同理解）

1. `work-item-status` 同时服务人和 Agent，只输出结构稳定的可读文本，不提供 JSON/API 或状态缓存。
2. 输出包含任务 ID/阶段、记录和实际版本、漂移、未决事项/负责人、阶段产物、停止原因、唯一下一入口和证据路径，不展开业务全文或附件。
3. README 是导航入口；阶段文档与 Git 是交叉证据。冲突时返回 `WORK_ITEM_STATUS_CONFLICT`，列出来源、不给下一入口。
4. Skill 对仓库绝对零写入，不修正文档、不补建产物、不保存查询结果。
5. 固定四类错误：任务不存在、状态证据不完整、状态/版本冲突、证据受限；错误均零写入。
6. 下一动作遵循 Workflow 和人工关口；负责人缺席时要求有权人员指定，Agent 不代签。
7. 实现为一个项目级显式 Skill 包及锁文件登记，不增加脚本、不修改现有五入口、治理、模板、第三方 Skill或业务代码。
8. 验收覆盖 DISCOVERING、等待人工裁决、版本漂移和任务不存在，并通过前后文件树与哈希证明只读。
9. 当前任务只有一个可独立验证切片，不拆 Tickets；该裁剪仍须开发负责人批准。
10. 本摘要只有在人工确认已形成共同理解后才能进入 `codebase-design`；它不是设计或实施批准。

共同理解人工确认原话：`同意`。

## `codebase-design` 设计草案（已批准并固化）

### Module、Seam 与 Interface

- **Module**：项目级 `work-item-status` Skill。它负责从 Work Item 文件和 Git 证据中计算当前可证明状态，并隐藏来源交叉核对、漂移判断、错误分类和下一动作路由。
- **Seam**：显式命令 `/work-item-status <work-item-id>`。调用方只需要知道 Work Item ID、稳定输出字段和错误语义，不需要理解文件布局或 Git 解析细节。
- **Interface**：一个只读查询入口，返回结构稳定的可读文本。成功结果包含任务 ID/阶段、记录与实际版本、漂移、未决事项及负责人、阶段产物、停止原因、唯一下一动作和证据路径；失败返回四类稳定错误之一，并且不给可执行下一入口。
- **Implementation**：按固定顺序读取 README、阶段产物和 Git，验证证据一致性，计算状态与唯一下一动作，再渲染结果。文件发现、状态推导和文本渲染属于 Module 内部，不暴露为多个调用命令。

该形状通过删除测试：若删除此 Module，状态来源优先级、漂移检查、错误分类和下一动作映射会重新散落到所有阶段 Skill 与人工 Prompt 中，因此该 Module 提供了真实的 **Depth**、调用方 **Leverage** 和治理规则 **Locality**。

### 责任与依赖方向

1. 人或 Agent 调用显式命令；Skill 不隐式触发。
2. Skill 只依赖仓库中已有的 Workflow、Work Item 文件和 Git 事实，不把某个历史任务的答案写入规则。
3. Workflow 和人工批准是状态语义权威；Skill 只报告，不批准、不修复、不推进状态。
4. 当前只有一个真实实现，不引入端口或 Adapter；为测试虚构第二个 Adapter 会扩大 Interface、降低深度。
5. 不修改五个已有阶段 Skills、治理规范、Templates、第三方 Skills、业务代码或测试。

### 状态计算与失败最终状态

1. 先验证 Work Item 是否存在以及证据是否可读。
2. 再交叉核对 README 导航、阶段产物、人工确认和 Git 记录/实际版本。
3. 只有所有证据能导出一个动作时才返回唯一下一动作；多个候选或相互矛盾时返回 `WORK_ITEM_STATUS_CONFLICT`。
4. 缺少判断所需证据时返回 `WORK_ITEM_STATUS_INCOMPLETE`；证据不可访问时返回 `WORK_ITEM_EVIDENCE_BLOCKED`；目标不存在时返回 `WORK_ITEM_NOT_FOUND`。
5. 所有成功与失败路径均保持仓库绝对零写入；失败后仓库和被查询 Work Item 保持原状。
6. 负责人未具名但阶段事实一致时，不视为冲突；唯一下一动作是由讲师/交付负责人指定对应角色。

### 备选方案与取舍

| 方案 | Depth / Leverage | 代价与风险 | 建议 |
| --- | --- | --- | --- |
| A. 单一显式 Skill，内部完成读取、核对、推导和渲染 | 小 Interface 隐藏完整状态判断；调用方和测试共用同一 Seam | Skill 文本必须清楚固定来源顺序与错误语义 | 推荐 |
| B. 新增确定性解析脚本并由 Skill 包装 | 解析器可能复用 | 当前只有一个调用方；会增加新执行面、脚本维护和供应链验证 | 不采用 |
| C. 把查询逻辑分别加入五个阶段 Skills | 无新增命令 | 规则重复、输出漂移、修改已验收入口，删除任一实现不会集中消除复杂度 | 拒绝 |
| D. 暴露多个子命令分别查版本、状态和下一动作 | 参数看似精确 | Interface 变浅，调用方必须自行拼装并处理冲突 | 拒绝 |

### Interface 是测试面

| 设计决定 | 通过 Interface 的验证方式 | 失败信号 |
| --- | --- | --- |
| DISCOVERING 状态可恢复 | 查询隔离的已建档任务，核对字段、证据路径和唯一 discover/设计动作 | 缺字段、跨级入口或发生写入 |
| 等待人工裁决可恢复 | 查询包含 Review Finding 且无裁决的任务 | 自动越过裁决、代签或给出实施/交付入口 |
| 版本漂移停止 | 构造记录版本与实际 Git 不一致的隔离任务 | 未返回 `WORK_ITEM_STATUS_CONFLICT`，或仍给可执行入口 |
| 不存在与缺证据稳定失败 | 查询不存在任务以及阶段证据不完整任务 | 错误分类不稳定、补建文件或修改 README |
| 绝对只读 | 每个场景查询前后比较文件树、内容哈希和 Git 状态 | 任意仓库文件、索引或提交状态变化 |

测试只观察命令输出和仓库前后状态，不直接测试内部解析步骤；内部实现重排不应要求改动这些验收。

### 建议实施范围与裁剪

- 可改：`.agents/skills/work-item-status/SKILL.md`、`.agents/skills/work-item-status/agents/openai.yaml`、`skills-lock.json`，以及 T05-G91 在正确阶段允许的过程与验证证据。
- 禁改：现有五个项目阶段 Skills、任何第三方 Skill、治理规范、Templates、课程卡、业务代码与测试。
- 任务只有一个可由该 Interface 独立验收的纵向切片，不拆 Tickets；若 `/to-spec` 确认该裁剪，应直接进入整个 Work Item 的单切片实施审批。
- 回滚：删除新增 Skill 目录并撤销其锁文件登记；不影响现有五阶段链路或业务代码。

### 本阶段请求的人工审批

请开发负责人和 AI 治理/契约所有者明确批准或退回以下四项：

1. 单一显式只读 Interface 与内部状态推导职责；
2. 四类稳定错误、唯一下一动作和绝对零写入语义；
3. 以上可改/禁改范围及不新增脚本、不修改既有 Skills 的限制；
4. 单一纵向切片、无需 Tickets 的裁剪和 Interface 级验收接缝。

人工批准原话：`批准。进入/to-spec T05-G91`。该批准对象是上述 `codebase-design` 草案；正式生成的 Spec v1、Design v1、Interface v1 与实施范围仍需另行批准。

## 当前待确认

| 问题 | 所需角色 | 对象版本 | 状态 |
| --- | --- | --- | --- |
| 调用者与输出受众 | AI 治理/契约所有者 | `bfba7413` / Round 1 | CONFIRMED：人和 Agent，结构稳定的可读文本 |
| 最小输出字段 | AI 治理/契约所有者 | `bfba7413` / Round 1 | CONFIRMED：采用推荐字段 |
| 来源优先级与冲突行为 | AI 治理所有者、开发负责人 | `bfba7413` / Round 1 | CONFIRMED：交叉核对，冲突停止 |
| 绝对只读边界 | AI 治理所有者 | `bfba7413` / Round 2 | CONFIRMED：仓库绝对零写入 |
| 下一入口计算规则 | AI 治理/契约所有者 | `bfba7413` / Round 2 | CONFIRMED：只能返回唯一入口 |
| 错误集合与零写入 | AI 治理所有者、开发负责人 | `bfba7413` / Round 2 | CONFIRMED：四类稳定错误，全部零写入 |
| 验收场景 | 测试工程师、开发负责人 | `bfba7413` / Round 2 | CONFIRMED：四类隔离场景与前后哈希 |
| 阶段到唯一下一动作的映射 | AI 治理/契约所有者 | `bfba7413` / Round 3 | CONFIRMED：采用固定顺序 |
| 负责人缺席行为 | AI 治理所有者、交付负责人 | `bfba7413` / Round 3 | CONFIRMED：要求有权人员指定，不代签 |
| 最小实现与 Ticket 裁剪 | 开发负责人、AI 治理所有者 | `bfba7413` / Round 3 | CONFIRMED：单切片、无新脚本、限定路径 |
| 三轮问答是否形成共同理解 | 开发负责人、AI 治理/契约所有者 | 范围收敛摘要 / `bfba7413` | CONFIRMED：用户原话“同意” |
| `codebase-design` 复用设计、Interface、测试接缝与实施范围 | 开发负责人、AI 治理/契约所有者 | 本文件设计草案 / `bfba7413` | CONFIRMED：用户原话“批准。进入/to-spec T05-G91” |
| Spec v1、Design v1、Interface v1 与实施范围 | 开发负责人、AI 治理/契约所有者 | `spec.md`、`design.md`、`interface.md` / `bfba7413` | CONFIRMED：用户原话“全部通过，批准。/work-item-execute T05-G91” |

三轮问题、候选设计和正式实施依据已经分别获得人工确认。当前通过 `/work-item-execute T05-G91` 实施唯一纵向切片；完成开发验证后必须停止等待开发负责人验收。
