# Interface：项目级 Work Item 治理 Skills

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T05-G01 |
| 状态 | APPROVED |
| Spec 路径/版本 | `spec.md` v1.1 |
| 代码起点 | `add14f73bf857604e5f738fe35111ab4ef1b3e05` |
| 契约所有者/调用方 | 用户代理 AI 治理所有者；调用方为学员与执行 Work Item 的 Agent；具名信息待登记 |

## 契约范围

- 类型：项目 Skill 命令 / 文档与 Git 工作区 Interface。
- 新增、修改或保持的契约：新增 `/work-item-start`、`/work-item-discover`、`/work-item-execute`、`/work-item-review`、`/work-item-decision` 五个显式入口；保持现有 AI Hero/参考 Skills、Templates、Workflow 和人工权限不变。
- 明确非目标：不提供 HTTP/UI 接口，不直接暴露业务 Repository 或数据，不允许单一入口自动完成全流程。

## UI 交互契约

- 页面入口与完整操作路径：不适用业务页面；入口是新 Agent 会话中的显式单行命令。用户在每个人工阶段检查落盘结果后，另行触发下一入口。
- 字段标签、业务值、默认值和可编辑/只读/可选择状态：Work Item ID 和阶段必要变量可输入；仓库事实及已批准文档只读；未知结论无默认 PASS；允许状态来自 Workflow。
- 系统生成标识与连续操作、重试、冲突行为：不生成正式 Work Item ID。重试先重新读取版本；目录或版本冲突时停止，不覆盖。
- 成功与失败反馈、结果明细及保留行为：成功显示写入文件、对象版本、门禁摘要和下一入口；失败显示错误标识、原因、已完成/未执行动作、责任角色，上一次有效产物保持可见。
- 列表、查询、筛选、刷新及重新进入后的行为：新会话通过 Work Item ID 从 README、阶段文档和 Git 恢复；不依赖对话缓存。无额外列表 UI。

## 请求或输入

| 字段/方法 | 类型 | 必填 | 约束 | 示例 |
| --- | --- | --- | --- | --- |
| `/work-item-start <id>` | 命令 | 是 | ID 唯一且匹配 Txx 任务卡；同时提供类型和任务卡要求的事实 | `/work-item-start T02-G01` |
| 任务类型 | 枚举文本 | start 必填 | 必须是 Workflow 支持的任务类型，不得由 Skill 猜测 | `需求调整` |
| 原始事实/附件 | 文本/附件 | start 按任务卡 | 只提供必要、可安全使用的输入；附件必须可唯一对应且无需秘密数据 | 页面失败事实与截图 |
| `/work-item-discover <id>` | 命令 | 是 | Work Item 已建档且输入通过人工检查 | `/work-item-discover T03-G01` |
| `/work-item-execute <id-or-ticket>` | 命令 | 是 | 必须存在已批准实施依据；多 Ticket 任务传当前 Ticket 路径 | `/work-item-execute docs/work-items/T05-G01/tickets/01-work-item-start.md` |
| `/work-item-review <id>` | 命令 | 是 | Work Item 必须能唯一解析记录的起点和候选提交；不能时再要求明确比较点 | `/work-item-review T05-G01` |
| `/work-item-decision <id>` | 命令 | 是 | 同时提供人的最终决定原文；角色、时间、版本、MR/PR、目标分支、回滚与风险从当前会话、Git、Work Item 或企业项目管理系统解析 | `ACCEPTED /work-item-decision T05-G01` |
| 人工批准/裁决 | 原文记录 | 条件必填 | 只接受有权角色已经作出的结论，不接受 Agent 推导 | `APPROVED` / `REWORK` / `ACCEPTED` |

## 响应或输出

| 字段/返回值 | 类型 | 语义 | 示例 |
| --- | --- | --- | --- |
| `status` | Workflow 状态 | 本入口执行后的真实阶段，不代表自动批准 | `DISCOVERING` |
| `written` | 路径列表 | 本次实际新增或更新的允许文件 | `docs/work-items/T02-G01/README.md` |
| `versions` | 对象 | Git 提交、Spec/Standards 或被审版本 | `git=add14f7…` |
| `checks` | 结果列表 | 实际运行的确定性门禁及结果；未运行必须明确 | `validate-t05-skills: PASS` |
| `stop_reason` | 稳定错误标识与说明 | 失败或人工关口的停止原因；成功进入人工关口也必须说明 | `WORK_ITEM_SCOPE_APPROVAL_REQUIRED` |
| `pending` | 列表 | 需要哪个角色确认哪个对象版本 | `开发负责人批准 spec v1` |
| `next_entry` | 命令或人工动作 | 唯一建议下一入口，不自动执行 | `/work-item-discover T02-G01` |

### 各入口固定产物

| 入口 | 允许写入 | 成功停止点 |
| --- | --- | --- |
| `work-item-start` | `README.md`、`inputs/` | 建档人工验收前 |
| `work-item-discover` | `01_analysis.md` 及按类型/顺序允许的当前阶段草案 | 未收敛时继续质询；需设计时停在 `codebase-design` 或设计人工确认点；无需设计或设计获确认后停在 `to-spec` 入口 |
| `work-item-execute` | 当前 Ticket 批准范围内的实现/开发测试、`02_verification.md`、必要 artifacts | Review 前；每张 Ticket 完成后先停一次 |
| `work-item-review` | `03_review.md`、README 评审状态 | Finding 人工裁决前 |
| `work-item-decision` | `04_decision.md`；普通提交/推送仅在明确授权时 | 有权限的人合并前，永不自动合并 |

## 错误与边界行为

| 条件 | HTTP/错误码/异常 | 调用方行为 | 最终状态 |
| --- | --- | --- | --- |
| ID、任务类型或必要事实缺失 | `WORK_ITEM_INPUT_REQUIRED` | 补充明确缺失项后重试当前入口 | 状态不推进 |
| 任务卡不能唯一定位 | `WORK_ITEM_TASK_CARD_NOT_FOUND` | 修正 ID 或由讲师确认任务卡 | 状态不推进 |
| 目录已存在或版本并发变化 | `WORK_ITEM_CONFLICT` | 核对归属和 Git 版本；不得覆盖 | 保留原状态和文件 |
| 附件无效、不可读、不可对应或敏感 | `WORK_ITEM_EVIDENCE_BLOCKED` | 由人更换、删除或脱敏后重试 | 不归档问题附件 |
| 当前阶段或前置产物不满足 | `WORK_ITEM_STAGE_MISMATCH` | 返回正确人工动作或入口 | 不产生后续产物 |
| 开发负责人批准缺失/对象版本不一致 | `WORK_ITEM_SCOPE_APPROVAL_REQUIRED` | 对当前 Spec/Ticket/Diff 取得有效批准 | 停在实施前 |
| 实施测试失败或修改超范围 | `WORK_ITEM_EXECUTION_BLOCKED` | 回到当前 Ticket 修复或重新批准范围 | 不进入 Review |
| Review 比较点无效或证据版本漂移 | `WORK_ITEM_REVIEW_VERSION_MISMATCH` | 固定正确起点/候选提交并重新 Review | 不写虚假评审结论 |
| 人工决定、角色或交付证据缺失/冲突 | `WORK_ITEM_DECISION_REQUIRED` | 由有权人员补充或裁决 | 不生成最终决定 |
| 任一入口尝试修改第三方 Skill、越阶段或自动批准/合并 | `WORK_ITEM_GOVERNANCE_VIOLATION` | 立即停止，保留证据并交 AI 治理所有者 | BLOCKED 或当前等待状态 |

## 兼容、迁移与回滚

- 旧调用方影响：T01 学员卡继续手写完整 Prompt；T02～T04 既有命令仍可用。五个新入口是新增的项目编排层，不删除旧入口。
- 兼容窗口/版本策略：T05 期间新旧入口并存；只有五个 Skill 完成逐项及完整链路验证、锁定来源和人工验收后，才能作为后续课程推荐入口。
- 迁移步骤：先启用 `work-item-start` 并跨 T01/T02 验证；再依次启用 discover、execute、review、decision；最后以临时 Work Item 验证完整链路。不得批量替换历史任务卡。
- 回滚方式：逐个删除或回退当前 Ticket 新增的项目 Skill 目录与对应 `skills-lock.json` 登记；保留此前已验证入口。全部回滚到代码起点 `add14f73bf857604e5f738fe35111ab4ef1b3e05` 时恢复无五个答案 Skill 的状态。

## 契约验证

| 契约条目 | 测试/检查 | 预期结果 |
| --- | --- | --- |
| 文件结构、标题、显式调用、来源和哈希 | `./scripts/validate-t05-skills.sh <skill-name>` | 当前 Skill 通过；未完成 Skill 不被误判为完成 |
| 项目治理材料 | `./scripts/validate-ai-governance.sh` | 模板、治理文件和已登记项目 Skills 完整 |
| `work-item-start` 正常与失败路径 | T01/T02 两个新会话加目录冲突/缺材料/附件失败 | 正常结构一致且输入隔离；失败无覆盖和越界产物 |
| 其余四个入口停止行为 | 临时 Work Item 分别构造未批准、测试失败、Diff 漂移和缺决定 | 每个入口停在自己的人工关口 |
| 完整短命令链 | 临时 Work Item 逐阶段执行并人工补充必要材料 | 新会话可恢复；没有入口自动跨阶段 |
| 最终门禁 | `./scripts/classroom-verify.sh T05` | 五个 Skill 和治理检查全部通过，健康检查成功 |
| 第三方及历史材料保持 | 基线 Diff、锁文件哈希和答案泄漏搜索 | 第三方 Skill、T01 手写方式和历史业务答案未被修改/复制 |

## 审批记录

| 角色/调用方 | 姓名 | 结论 | 时间 | 备注 |
| --- | --- | --- | --- | --- |
| 业务/规则所有者代理 | 用户（课堂代理） | APPROVED | 2026-09-11 | 已批准五层分类和契约方向 |
| AI 治理所有者代理 | 用户（课堂代理；具名信息待登记） | APPROVED | 2026-09-11 11:35:58 CST | 批准五个命令、错误、写入和人工停止契约 |
| 开发负责人代理 | 用户（课堂代理；具名信息待登记） | APPROVED | 2026-09-11 11:35:58 CST | 批准实施范围、测试接缝、顺序和回滚 |
| T01～T04 类型调用方 | 课堂代理 | PENDING | 待验证 | 通过临时 Work Item 验证兼容和可用性 |
