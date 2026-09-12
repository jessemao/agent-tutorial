# Work Item：T05-G91 项目级 Work Item 状态查询 Skill

## 任务身份

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T05-G91 |
| 任务类型 | 组件或 Skill 复用 |
| 状态 | ACCEPTED |
| 业务/规则所有者 | 待指定 |
| 开发负责人（实施前批准人） | 待指定 |
| UI/契约所有者（涉及变更时） | 待指定 |
| 测试工程师 / 团队 | N/A；本任务未修改业务代码，人工确认裁剪 |
| QA 审核人 / 团队（不同于测试工程师） | N/A；本任务未修改业务代码，人工确认裁剪 |
| 业务验收人 | 待指定 |
| 创建时间 | 2026-09-11 14:04:14 CST |

## 版本与输入

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `bfba7413ed5fc95a34b8cd0110fbc4da48b01795` |
| 当前代码版本 | 查询或评审时解析的当前 `HEAD`；实施内容以 `work-item-status` 两个文件及 `skills-lock.json` 中登记的 SHA-256 固定 |
| Standards ID/版本 | 当前提交中的根 `STANDARDS.md` 与 `docs/ai-governance/standards/` |
| 根及 Module `AGENTS.md` | 根 `AGENTS.md`；受影响 Module 尚待发现阶段确认 |
| 原始输入路径 | `inputs/input-evidence.md` |

## 目标与范围摘要

- 目标：新增项目级 `work-item-status`，只读报告指定 Work Item 的当前状态、Git/对象版本、未决事项和唯一合法下一入口。
- 允许修改：新增 `.agents/skills/work-item-status/SKILL.md`、`.agents/skills/work-item-status/agents/openai.yaml`、`skills-lock.json` 以及当前 Work Item 的实施验证证据。
- 禁止修改：五个既有项目阶段 Skills、第三方 Skills、治理规范、Templates、课程卡、业务代码与业务测试。
- 当前状态：第二轮固定候选 `ea039137` 已完成双轴复评；SPEC-03 经人工决定本轮跳过且未修复；独立业务功能测试与 QA 经人工裁剪为 N/A；人工最终决定为 ACCEPTED。尚未配置正式目标分支、远端或 MR/PR，未执行合并。

## 产物适用性

| 产物 | 适用/不适用 | 路径或理由 | 状态 |
| --- | --- | --- | --- |
| Inputs | 适用 | `inputs/` | 已创建基础输入证据 |
| Spec | 适用 | `spec.md` v1 | APPROVED |
| Tickets | 不适用 | 单一 Interface 纵向切片；裁剪已随实施范围批准 | N/A（已批准） |
| Design | 适用 | `design.md` v1 | APPROVED |
| Interface | 适用 | `interface.md` v1 | APPROVED |
| Analysis | 适用 | `01_analysis.md` | 已完成发现与候选设计 |
| Verification | 适用 | `02_verification.md` | 第二轮 REWORK 开发验证完成并验收通过 |
| Review | 适用 | `03_review.md` | 候选 `ea039137` 已复评；SPEC-03 人工决定本轮跳过，其他历史 Findings 已关闭 |
| 独立功能测试 | 不适用 | 本任务只新增项目 Skill、未修改业务代码；Skill 行为已由开发验证与固定候选双轴评审覆盖 | N/A（人工确认） |
| QA 审核 | 不适用 | 本任务只新增项目 Skill、未修改业务代码；治理、来源、版本和证据已由 Standards 轴评审 | N/A（人工确认） |
| Decision & Delivery | 适用 | `04_decision.md` | ACCEPTED；正式 MR/PR 与合并待外部交付环境处理 |

## 人工批准点

| 阶段 | 待批准内容 | 决策人 | 结论/时间 |
| --- | --- | --- | --- |
| 实施前 | 复用目标、Interface、实现范围和验证方式 | 开发负责人及 AI 治理/契约所有者 | APPROVED / 2026-09-11 / 用户原话“全部通过，批准。/work-item-execute T05-G91” |
| 独立测试 | 完整需求的功能与风险测试 | 测试工程师 | N/A；人工确认本任务无业务代码变更 |
| QA 审核 | 流程、规范、追溯与缺陷关闭 | QA 人员 | N/A；人工确认本任务无业务代码变更 |
| 业务验收 | 只读状态报告的目标与风险 | 业务/治理所有者 | ACCEPTED / 2026-09-11 / 用户原话“ACCEPTED /work-item-decision T05-G91”；独立身份源未配置 |
| 交付前 | 评审结论、风险和是否放行 | 交付负责人 | ACCEPTED / 2026-09-11 / 同一人工决定；正式 MR/PR 与合并未执行 |

## 当前下一步

- 当前结果：人工最终决定已原样记录为 ACCEPTED；SPEC-03 保持 WAIVED、未修复，测试与 QA 保持 N/A。
- 唯一合法下一动作：由正式交付环境中有权限的人核对目标分支和 MR/PR，并决定是否合并。
- Agent 必须停止：不得自行提交、推送、创建或批准 MR/PR，不得执行合并、发布或标签操作。
- 已完成：基础建档、`grill-with-docs`、共同理解确认、`codebase-design`、`to-spec`，以及 Spec v1、Design v1、Interface v1、单切片和实施范围的人工批准。

## 待确认事项与负责人

| 事项 ID | 当前阶段 | 待确认内容及对象版本 | 所需角色 | 具名负责人 / 团队 | 状态 | 结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- | --- | --- |
| T05-G91-Q01 | DISCOVERING | 基础建档；对象为 `bfba7413` 上的 README 与输入证据 | 课堂验收人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“建档人工验收通过。可以进入 work-item-discover；这不是范围或实施批准。” |
| T05-G91-Q02 | DISCOVERING | 实现细节、输出字段、错误边界及验证对象语义 | AI 治理/契约所有者、开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 三轮 `grill-with-docs` 问答及 `01_analysis.md` |
| T05-G91-Q03 | DISCOVERING | 三轮问答是否形成共同理解 | AI 治理/契约所有者、开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“同意” |
| T05-G91-Q04 | WAITING_FOR_SCOPE_APPROVAL | `codebase-design` 复用设计、Interface、测试接缝和范围方向 | AI 治理/契约所有者、开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“批准。进入/to-spec T05-G91” |
| T05-G91-Q05 | WAITING_FOR_SCOPE_APPROVAL | Spec v1、Design v1、Interface v1、单切片裁剪及实施范围 | AI 治理/契约所有者、开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“全部通过，批准。/work-item-execute T05-G91” |
| T05-G91-Q06 | IMPLEMENTING | 当前单切片实现与 `02_verification.md` | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“验收完毕没问题。进入下一个动作” |
| T05-G91-Q07 | IMPLEMENTING | 形成固定候选提交并进入 Review | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 同一原话授权进入下一个动作；候选 `e8e1d211e532f9d13d413b19eea964ec4aa44b24` |
| T05-G91-Q08 | REVIEWING | SPEC-01～02、STD-01～05 / 候选 `e8e1d211` | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：REWORK | 用户原话：“帮我修复这7项” |
| T05-G91-Q09 | IMPLEMENTING | 7 项 Finding 的 REWORK 实现与补充开发验证 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“验收这次 REWORK” |
| T05-G91-Q10 | REWORK | 形成新的固定候选提交 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户显式调用：“/work-item-review T05-G91” |
| T05-G91-Q11 | REVIEWING | SPEC-01、STD-01、STD-02 / 新候选 `db7ccb38` | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：REWORK | 用户原话：“帮我完成剩余3项” |
| T05-G91-Q12 | REWORK | SPEC-01、STD-01、STD-02 第二轮修复 | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：REWORK | 用户原话：“帮我完成剩余3项” |
| T05-G91-Q13 | REWORK | 第二轮 REWORK 实现与可复现开发验证 | 开发负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED | 用户原话：“验收完成 /work-item-review T05-G91”；`02_verification.md`、`artifacts/rework-round-2-raw-output.txt` |
| T05-G91-Q14 | REVIEWING | SPEC-03 / 候选 `ea039137`：AC-05 四类零写入快照证据不完整 | 开发负责人、AI 治理/契约所有者 | 用户（课堂代理；具名信息待登记） | CONFIRMED：WAIVED | 用户原话：“同意跳过 SPEC-03”；本轮接受证据缺口，未修复 |
| T05-G91-Q15 | QUALITY_STAGE_APPLICABILITY | 候选 `ea039137` 是否需要独立业务功能测试和 QA | 业务/AI 治理所有者、交付负责人 | 用户（课堂代理；具名信息待登记） | CONFIRMED：N/A | 用户原话：“T05是skills的生成任务，没有改动代码，所以不用测试和QA的环节” |
| T05-G91-Q16 | WAITING_FOR_DELIVERY_DECISION | 候选 `ea039137` 的业务/治理验收和最终交付决定 | 业务/AI 治理所有者、交付负责人 | 当前 Codex 会话用户；企业身份源未配置 | CONFIRMED：ACCEPTED | 用户原话：“ACCEPTED /work-item-decision T05-G91”；记录时间 2026-09-11 17:38:40 CST |
| T05-G91-Q17 | DELIVERY | 正式目标分支、MR/PR 与实际合并 | 有权限的交付负责人 | 待由企业项目管理事实系统解析 | PENDING | 当前隔离验收仓库无远端；本入口未执行合并 |

负责人未知时由讲师或交付负责人指定，Agent 不得代填或代签。
