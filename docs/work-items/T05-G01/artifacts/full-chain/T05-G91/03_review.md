# 03 Review：T05-G91

## 第二轮 REWORK 最终复评

- 唯一起点：`bfba7413ed5fc95a34b8cd0110fbc4da48b01795`。
- 被审候选：`ea03913715fa9cf2e853f53515b5bd365b0a1905`；merge-base 与唯一起点一致。
- 三点差异：`git diff bfba7413ed5fc95a34b8cd0110fbc4da48b01795...ea03913715fa9cf2e853f53515b5bd365b0a1905`。
- 提交列表：`e8e1d21 feat(t05): add read-only work item status skill`；`db7ccb3 fix(t05): resolve work item status review findings`；`ea03913 fix(t05): complete work item status review evidence`。
- 文件范围：新增 `work-item-status` Skill、OpenAI 配置、锁登记和 T05-G91 文档/可复现夹具，共 21 个新增文件与 1 个修改文件。
- 被审 Skill/Standards：`work-item-status` 内容 SHA-256 `3defd0cddf3fe863c3e006f4fc0211ab05dece833ca0fb378d7c3edfcbd9bc93`；`STANDARDS.md` ID `STD-WMS-0.7-06` 及当前候选中的 AI Security、Documentation、Testing、Workflow、Skills 契约。
- Analysis、Spec v1、Design v1、Interface v1 均对应同一起点并有实施批准；Tickets 经批准裁剪为 N/A；Verification 已由开发负责人验收并以实现哈希、时间、命令、夹具和原始输出绑定候选内容。
- 自动检查：Skill Creator、AI Governance、T05 课程门禁和三点差异 `git diff --check` 全部 PASS。
- 独立双轴结论：Spec 轴 1 项 PARTIAL；Standards 轴 PASS。评审建议：REWORK。

### 最终 Spec 符合性矩阵

| 条目 | 候选实现与证据 | 结论 |
| --- | --- | --- |
| DEC-01～DEC-07、AC-01～AC-04、AC-06 | 显式只读入口、稳定字段/错误、唯一下一动作、ID 边界、来源登记、夹具和门禁均与批准文档一致；未发现范围蔓延或错误实现 | PASS |
| SPEC-01（DEC-03、AC-03） | README、Verification、人工验收与固定候选形成一致时间线；内容哈希和当前候选均可唯一解析，自查询不再冲突 | RESOLVED |
| AC-05：查询前后文件树、内容哈希、索引和工作区完全不变 | `artifacts/rework-round-2-raw-output.txt` 记录了逐场景调用前后 `git status --short` 与 `git diff --check`，但未记录文件树、内容哈希和 Git index 的前后快照 | PARTIAL / MAJOR |

### 最终 Standards 符合性矩阵

| 条目 | 候选实现与证据 | 结论 |
| --- | --- | --- |
| STD-01：STD-DOC-01、Documentation §7 | README 与 Verification 一致表达第二轮 REWORK 已验收、等待固定候选复评；历史 OPEN 作为审计记录保留 | RESOLVED |
| STD-02：STD-TEST-02、Testing §4/§7 | Verification 和原始 artifact 已记录基线、内容哈希、时间、真实命令、逐场景输出及仓库内可复现夹具 | RESOLVED |
| STD-03～STD-05 | 来源/版本/许可证/逐文件哈希完整；活动证据无本机绝对路径；Skill 引用权威 Workflow 而不复制 17 步 | RESOLVED |
| AI Security、只读权限、显式调用、批准范围与 Fowler 气味基线 | 未发现新的硬性违反或可证判断性气味 | PASS |

### 最终 Findings

| ID | 轴 | 严重级别 | 位置 | 问题与证据 | 影响 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| SPEC-03 | Spec | MAJOR | `spec.md:79`；`artifacts/rework-round-2-raw-output.txt:50-62,79-93` | AC-05 明确要求查询前后比较文件树、内容哈希、索引和工作区；候选证据仅记录工作区状态与差异格式检查 | 无法按批准验收条件证明所有成功/失败查询绝对零写入 | 在同一固定候选接缝对全部样本记录四类前后快照及比对结果，形成新候选后重新 Review | WAIVED / 人工决定本轮跳过，未修复 |

### 第二轮问题复盘与交接

- 直接原因：开发证据把 `git status --short` 视为完整零写入证明，但它不能单独证明未跟踪文件内容、完整文件树和 index 字节级状态均未变化。
- 为什么未更早发现：结构门禁和 `git diff --check` 不验证运行前后快照覆盖；上一轮 STD-02 聚焦可复现命令与原始输出，没有逐项反查 AC-05 的四类比较对象。
- 防复发动作：由开发负责人决定是否在后续 REWORK 中固定通用的文件树、内容哈希、index 与工作区四联快照协议；本 Review 不代替人裁决或修改验证依据。
- 人工裁决：用户原话“同意跳过 SPEC-03”；本轮接受该证据缺口，不得表述为已修复或 Spec PASS。
- 独立功能测试与 QA：N/A。人工确认本任务只新增项目 Skill、未修改业务代码，不执行这两个业务代码质量阶段；该裁剪不替代已有 Skill 开发验证和双轴评审。

## REWORK 后复评摘要

- 新候选：`db7ccb386267b1741b1dddebe742c1ca92e1dcee`。
- 固定差异：`bfba7413ed5fc95a34b8cd0110fbc4da48b01795...db7ccb386267b1741b1dddebe742c1ca92e1dcee`。
- 提交列表：`e8e1d21 feat(t05): add read-only work item status skill`；`db7ccb3 fix(t05): resolve work item status review findings`。
- Spec 独立复评：SPEC-02 已关闭；SPEC-01 仍为 MAJOR。未发现其他 Spec 缺失、范围蔓延或实现错误。
- Standards 独立复评：STD-03～STD-05 已关闭；STD-01、STD-02 仍为硬性违反。未发现新的可证气味、安全或依赖越界问题。
- 复评结论：REWORK。以下原始矩阵保留首次评审事实；Finding 状态和本节复评矩阵代表最新结论。

### Spec 复评矩阵

| Finding | 新候选证据 | 结论 |
| --- | --- | --- |
| SPEC-01 | README 同一候选内仍同时写“正在修复/待验收”和“修复、验收已完成”；Verification 也同时写等待验收和已验收；缺少新候选自查询结果 | OPEN / MAJOR |
| SPEC-02 | `interface.md` 与 `SKILL.md` 已固定单路径段、规范化边界和越界前拒绝；`../../AGENTS.md` 返回 NOT_FOUND | RESOLVED |

### Standards 复评矩阵

| Finding | 新候选证据 | 结论 |
| --- | --- | --- |
| STD-01 | README 与 Verification 的修复/验收状态仍互相矛盾，违反 STD-DOC-01、Documentation §7、Deliverables §5 | OPEN / MAJOR |
| STD-02 | 行为 artifact 只有摘要，缺候选版本、真实命令、时间、逐场景原始输出和可复现夹具；无法绑定 `db7ccb3`，违反 STD-TEST-02、Testing §4/§7 | OPEN / MAJOR |
| STD-03 | 锁记录已有 `sourceVersion`、`license` 和逐文件哈希 | RESOLVED |
| STD-04 | 活动证据已移除本机绝对路径；首次 Review 中的文字仅为历史 Finding 引文 | RESOLVED |
| STD-05 | Skill 改为引用 Workflow 与 Skills 契约，不再复制 17 步映射 | RESOLVED |

## 被审对象

- 代码差异起点与被审版本：`bfba7413ed5fc95a34b8cd0110fbc4da48b01795...e8e1d211e532f9d13d413b19eea964ec4aa44b24`；merge-base 与声明起点一致；候选仅含提交 `e8e1d21 feat(t05): add read-only work item status skill`。
- `01_analysis.md`：候选提交中的 T05-G91 Analysis；记录三轮范围问答、已批准设计、单切片及修改边界。
- `02_verification.md`：候选提交中的开发验证；其对象版本表达和覆盖完整性属于本次 Findings。
- Spec/Standards：`spec.md` v1、`design.md` v1、`interface.md` v1（均 APPROVED）；根 `STANDARDS.md`、Workflow、Roles、Deliverables、Skills 契约、Documentation、AI Security、Testing。
- 使用的 Skills 与版本：项目 `work-item-review`、项目锁定的 `code-review`；新 `work-item-status` 哈希 `258b9b01a32237b1b64089ca8148ffaf7763b0e756903a8a528daed21e2b29a5`。
- 原始评审证据：`artifacts/review-evidence.md`。

## Spec 符合性矩阵

| 要求 | 代码符号 | 测试 | 真实结果 | 证据 | 结论 |
| --- | --- | --- | --- | --- | --- |
| DEC-01/02：单一显式可读查询 Interface | `.agents/skills/work-item-status/SKILL.md`、`agents/openai.yaml` | Skill Creator 校验、显式调用策略检查 | Skill 有效且 `allow_implicit_invocation: false` | `02_verification.md`、候选 Diff | PASS |
| DEC-03/05、AC-03：交叉核对版本并稳定报告冲突 | `SKILL.md` 状态判断与错误表 | 三轮独立前向查询 | Skill 能发现冲突，但候选自身 README/Verification 未绑定候选 SHA | `README.md:23,78`、`02_verification.md:10` | FAIL |
| DEC-04/07：唯一下一动作且不代签 | `SKILL.md` 下一动作顺序 | 第三次独立查询 | 返回开发负责人验收，未越过 Review | `artifacts/development-forward-validation.md` | PASS |
| DEC-06、AC-05：绝对零写入 | `SKILL.md` 绝对只读边界 | 查询前后 Git 状态比较 | 多次查询前后状态一致 | `artifacts/development-forward-validation.md` | PASS |
| Interface 输入：ID 只能定位一个 Work Item | `SKILL.md:12-14` | 未覆盖路径越界输入 | 只写“精确映射”，未拒绝 `/`、`..`、绝对路径或规范化越界 | 候选 Diff | FAIL |
| AC-04：四类稳定错误 | `SKILL.md` 错误与边界表 | 开发阶段仅实际验证 NOT_FOUND 和 CONFLICT | 契约已声明，INCOMPLETE 与 EVIDENCE_BLOCKED 尚无行为结果 | `02_verification.md` | PARTIAL |
| 范围：只新增 Skill、锁登记和本任务证据 | 候选 12 个文件 | 固定文件列表 | 未发现批准范围外生产/治理/第三方修改 | `artifacts/review-evidence.md` | PASS |

## Standards 符合性矩阵

| 规则 ID | 适用性 | 代码符号 | 检查证据 | 例外 | 结论 |
| --- | --- | --- | --- | --- | --- |
| STD-DOC-01；Documentation §5/§7；Deliverables §5 | MUST | `README.md:9,23,33,45,78`、`02_verification.md:10` | 候选已固定为 `e8e1d21`，文档仍记录起点/未提交对象/候选待记录 | 无 | FAIL |
| STD-TEST-01；Testing §2/§6 | MUST | `02_verification.md:26-29,47-50,57-58` | Red 仅证明文件不存在；DISCOVERING、等待裁决、INCOMPLETE、EVIDENCE_BLOCKED 未完成同接缝红绿验证 | 无 | FAIL |
| STD-SUPPLY-01；AI Security §4 | MUST | `skills-lock.json:64-72` | 新项目 Skill 只有项目名与哈希，缺版本/提交和许可证字段或引用 | 无 | FAIL |
| Documentation §6 | MUST | `02_verification.md:12`、`artifacts/development-validation.txt:8` | 活动文档提交 `/tmp/...` 与 `/Users/...` 本机绝对路径 | 无 | FAIL |
| Fowler Shotgun Surgery | REVIEW | `SKILL.md:35-55` | Skill 重新编码 Workflow 的 17 步路由，规则变化可能要求同步两处 | 无 | REVIEW |
| AI Security：显式调用、零外部写入 | MUST | `agents/openai.yaml`、`SKILL.md` | 隐式调用关闭；候选未执行外部写入 | 无 | PASS |
| 文档路由与阶段隔离 | MUST | `docs/work-items/T05-G91/` | Analysis、Verification、Review 和条件产物位于同一 Work Item；未提前生成 Decision/独立测试/QA | 无 | PASS |

## Findings

| ID | 严重级别 | 位置 | 问题与证据 | 建议 | 状态 |
| --- | --- | --- | --- | --- | --- |
| SPEC-01 | MAJOR | `README.md:23,78`；`02_verification.md:10` | DEC-03/05 与 AC-03 要求版本冲突可识别，但候选仍把当前版本写成起点、候选写成待记录、验证对象写成未提交工作区；查询自身会发生版本/对象冲突 | 固定候选 SHA，同步 README 与 Verification，并对新候选重跑前向查询 | OPEN |
| SPEC-02 | MAJOR | `.agents/skills/work-item-status/SKILL.md:12-14` | Interface 要求 ID 只能定位一个 Work Item，但实现未明确拒绝 `/`、`..`、绝对路径和规范化越界 | 把 ID 限定为单一路径段，规范化后必须仍在 `docs/work-items/` 下；无效输入稳定失败且不给入口 | RESOLVED：见 REWORK 后复评 |
| STD-01 | MAJOR | `README.md:9,23,33,45,78`；`02_verification.md:10` | 候选版本、阶段和验证对象不一致，违反版本固定与文档一致性要求 | 同步候选对象并重新固定评审版本 | OPEN |
| STD-02 | MAJOR | `02_verification.md:26-29,47-50,57-58` | 核心状态/错误行为缺少开发级失败与转绿证据，却记录开发完成，违反 STD-TEST-01 | 在同一命令 Interface 补 DISCOVERING、等待裁决、INCOMPLETE、EVIDENCE_BLOCKED 等可重复行为验证 | OPEN |
| STD-03 | MAJOR | `skills-lock.json:64-72` | 新 Skill 缺项目版本/提交和许可证追溯，违反 STD-SUPPLY-01 | 登记本项目来源版本/提交及许可证字段或权威引用，并验证哈希 | RESOLVED：见 REWORK 后复评 |
| STD-04 | MINOR | `02_verification.md:12`；`artifacts/development-validation.txt:8` | 提交本机绝对路径，证据不可移植 | 改用仓库相对路径和稳定工具标识 | RESOLVED：见 REWORK 后复评 |
| STD-05 | MINOR | `.agents/skills/work-item-status/SKILL.md:35-55` | 判断性 Shotgun Surgery：重复编码 Workflow 17 步，可能与权威 Workflow 漂移 | 保留推导原则并引用权威 Workflow，或明确维护同步门禁 | RESOLVED：见 REWORK 后复评 |

Spec 与 Standards Findings 分轴编号和保留；内容重叠不表示自动合并或关闭。

## 问题复盘

- 直接原因：形成候选后没有把开发工作区证据转换为候选提交证据；ID 安全边界停留在自然语言“精确映射”；验收集中在结构和两个实际错误，没有逐条执行全部契约场景；项目 Skill 的供应链字段沿用了既有最小锁格式而未满足当前 STD-SUPPLY-01 全量要求。
- 为什么原有检查没有更早发现：`classroom-verify.sh T05` 只识别五个既有阶段 Skill，不理解第六个 Skill 的语义；Skill Creator 只检查结构；Git Diff 格式检查不核对版本和审批语义。
- 哪道门禁可以提前发现：候选形成后执行一次 `/work-item-status T05-G91`；增加 Work Item ID 越界样本；为项目 Skill 锁记录增加版本/许可证结构检查；禁止过程文档本机绝对路径。
- 防止重复的具体动作、负责人和期限：是否采用上述门禁及如何避免复制 Workflow，由 AI 治理所有者和开发负责人随 Findings 裁决；Agent 不代定负责人或期限。

## 阶段状态

- 开发证据版本：候选 `e8e1d211e532f9d13d413b19eea964ec4aa44b24`；Verification 的对象绑定存在 Finding。
- 独立功能测试：N/A；本任务未修改业务代码，人工确认裁剪。
- QA 审核：N/A；本任务未修改业务代码，人工确认裁剪。

代码评审不代签上述结论；未到对应阶段不等于代码缺陷，缺失必需交付证据仍阻止最终放行。

## Agent 建议

READY_FOR_DECISION（SPEC-03 经人工决定本轮跳过，未修复；独立功能测试与 QA 经人工裁剪为 N/A）

> 本文件不记录人工接受或合并决定；正式裁决写入 `04_decision.md`。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| SPEC-01～SPEC-02 / 候选 `e8e1d211` | 开发负责人、AI 治理/契约所有者 | 待指定 | PENDING | 逐条裁决 |
| STD-01～STD-04 / 候选 `e8e1d211` | 开发负责人、AI 治理所有者 | 待指定 | PENDING | 硬性违反，逐条裁决 |
| STD-05 / 候选 `e8e1d211` | AI 治理所有者、开发负责人 | 待指定 | PENDING | 判断性气味，决定修复、接受或建立门禁 |
| 独立功能测试 / 候选 `ea039137` | 业务/AI 治理所有者、交付负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED：N/A | 用户原话：“T05是skills的生成任务，没有改动代码，所以不用测试和QA的环节” |
| QA 审核 / 候选 `ea039137` | 业务/AI 治理所有者、交付负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED：N/A | 同上；不生成 `qa-review.md` |
| SPEC-03 / 候选 `ea039137` | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：WAIVED | 用户原话：“同意跳过 SPEC-03”；证据缺口本轮接受，Finding 未修复 |

仅记录本阶段实际证据及有权人员的原文结论；缺席或未知负责人必须保留待确认，不得由 Agent 代签，也不得把开发测试、测试工程师结论与 QA 结论混为一项。
