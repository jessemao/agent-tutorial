# QA 质量审核：T04-G01

## 审核对象与责任

- QA 人员 / 所属团队（不同于测试执行人员）：当前 Codex 会话；用户原话“你的身份现在是 QA，现在进行测试”。但同一 Codex 会话刚刚作为测试工程师生成 `functional-test.md`，不满足项目要求的不同人员/团队分离条件，因此本次只能完成 QA 核查并记录阻塞，不能签署独立 QA PASS。
- 候选提交 / Spec / Standards 版本 / 审核时间：`f49448795a114b32d242442f15c66db2da66c27e` / `spec.md` v1 APPROVED / `STD-WMS-0.7-06` / 2026-09-10（Asia/Shanghai）。
- 输入：`02_verification.md`、`03_review.md`、`functional-test.md`、`artifacts/README.md` 及其日志、Work Item `README.md`、Workflow、角色与审批规范、测试和文档 Standards。
- 审核边界：QA 审核流程、追溯、覆盖和证据；不重新执行或代签测试工程师的功能测试，不代替业务放行。

## 质量审核

| 检查项 | 规则 / AC | 证据路径与版本 | 缺口 / Finding | 结论 |
| --- | --- | --- | --- | --- |
| 候选版本一致性 | STD-TEST-02、STD-DOC-01 | `f494487`；`artifacts/candidate-and-zero-reference-f494487.log`；QA 再次核对生产、测试和架构目录相对候选零 Diff | 无生产代码漂移；日志 SHA-256 与索引一致 | PASS |
| 角色与审批分离 | `roles-and-approvals.md` 第 1、5 节；Workflow 第 6 节 | `functional-test.md` 和本文件的会话身份记录 | **QA-F-01 BLOCKER**：同一 Codex 会话先以测试工程师身份执行并签署 PASS，随后切换为 QA；角色名称变化不等于不同人员/团队，QA 不能审核并批准自己的测试输出 | BLOCKED |
| 阶段与 MR/PR 门禁 | Workflow 固定流水线“评审与 MR/PR 准备” | `03_review.md`、Git 状态、任务过程记录 | **QA-F-02 BLOCKER**：独立功能测试前没有已推送候选分支和草稿 MR/PR；目前没有可审的 MR/PR 链接或平台版本锚点。因外部写入未获本轮授权，QA 仅记录缺口，不代为推送 | BLOCKED |
| 需求到开发及独立测试的追溯 | AC-01—AC-08；测试 Standards 第 3、8 节 | `spec.md` v1、`02_verification.md`、`functional-test.md` | 开发验证覆盖全部 AC；独立测试覆盖正常/全量移库、列表、入出库、幂等、回滚和 H2 并发，但没有独立覆盖盘点、禁用库位、跨仓库库位、源库存不存在、目标溢出；与 AC-02、AC-04 的独立测试承诺不完整 | REWORK |
| Spec / Standards 双轴评审完整 | STD-DELIVERY-01 | `03_review.md`；起点 `ae996fe5` 至候选 `f494487` | Spec 轴 0 Findings；Standards 初审 2 Findings 均有人工 REWORK 和关闭证据 | PASS |
| 测试环境与原始证据 | STD-TEST-02；测试 Standards 第 4、7、8 节 | `functional-test.md`；`artifacts/functional-test-http-ui-f494487.log`，SHA-256 `11b808...aa3c3d` | **QA-F-03 MAJOR**：所谓原始日志只保存每例 PASS 摘要，没有实际 HTTP 请求、状态码、响应体、余额查询输出或页面截图/导出；QA 无法从持久证据独立复核断言。运行时输出曾在会话可见，但未完整落盘 | REWORK |
| 并发与数据库差异 | AC-06、STD-DATA-02；测试 Standards 第 4、5、7 节 | `functional-test.md` FT-11；开发全量日志 | **QA-F-04 MAJOR**：H2 公开 HTTP 反向并发通过，但 MySQL Profile 两项条件测试仍跳过；没有测试/交付负责人对“不补测 MySQL”的明确风险接受，不能把该缺口作为已关闭风险 | BLOCKED |
| 缺陷重测与例外有效性 | Workflow 第 6 节；Standards 第 6 节 | `03_review.md` Findings、`functional-test.md` | 代码评审 Findings 已关闭；功能测试称未发现缺陷。当前没有登记的有效例外可覆盖 QA-F-01—04 | PASS（仅对既有 Findings）；新 QA Findings OPEN |
| 交付材料与回滚 | STD-DELIVERY-01、文档 Standards 第 5、7、8 节 | `README.md`、`design.md`、`spec.md`、Tickets、Review、测试记录 | 回滚方式与版本可追溯；`04_decision.md` 尚未生成是正确的。草稿 MR/PR 和独立 QA 签署缺失阻止业务验收 | BLOCKED |

## QA Findings

| ID | 严重级别 | 责任人 | 所需动作 | 状态 |
| --- | --- | --- | --- | --- |
| QA-F-01 | BLOCKER | 课程讲师 / 交付负责人 | 指定未参与本轮功能测试的另一位 QA 人员或团队，重新审核 `functional-test.md` 及补充证据；当前会话不得自签 QA PASS | OPEN |
| QA-F-02 | BLOCKER | 交付负责人 / 有推送权限人员 | 在获得明确外部写入授权后，提交当前过程产物、推送候选分支并创建关联 T04-G01 的草稿 MR/PR；记录链接和固定被审提交 | OPEN |
| QA-F-03 | MAJOR | 测试工程师 | 对候选 `f494487` 补录可复核的公开 HTTP 请求/响应、状态码和最终余额，并保存页面截图或等价导出；补测盘点、禁用/跨仓库库位、源库存不存在、目标溢出 | OPEN |
| QA-F-04 | MAJOR | 测试负责人 / 交付负责人 | 在 MySQL Profile 环境执行移库并发专项，或由有权人员书面接受未执行风险并记录范围、理由和期限 | OPEN |

## QA 结论

**BLOCKED**。

候选代码本身没有发现版本漂移，开发双轴评审 Findings 已关闭；但角色独立性、草稿 MR/PR 门禁、独立测试覆盖及持久原始证据不满足当前 Workflow 和 Standards，MySQL 并发风险也尚未得到补测或有效人工接受。因此不能进入业务验收或生成 `04_decision.md`。

QA 不推翻测试工程师在其已执行范围内的 PASS；本结论表示该 PASS 尚不足以满足完整交付门禁。

## 人工阶段决定

- 决定人：当前用户代理；具体治理角色尚未在本次原话中声明。
- 时间：2026-09-10（Asia/Shanghai）。
- 人工原话：“先跳过QA测试。进入到下一个步骤”。
- 执行解释：暂时跳过 QA 补测/复核并进入业务验收，不等同于 QA PASS，不关闭 QA-F-01—04，也不构成符合 Standards 的正式例外。
- 交付影响：业务所有者可以开始验收业务场景，但在最终放行、生成接受性 Decision 或合并前，仍须由独立 QA 关闭 Findings，或由有权治理/交付负责人补齐例外的规则 ID、范围、理由、风险、替代措施、有效期和整改负责人。

## 待确认与交接

| 待确认事项 / 对象版本 | 负责角色 | 具名负责人 / 团队 | 状态 | 确认结论 / 时间 / 证据 |
| --- | --- | --- | --- | --- |
| QA-F-01：独立 QA 人员/团队 | 课程讲师 / 交付负责人 | 待指定 | BLOCKED | 当前 QA 与测试工程师为同一 Codex 会话 |
| QA-F-02：提交、推送和草稿 MR/PR | 交付负责人 / 有权限人员 | 待指定 | PENDING | 需要针对目标远端和分支的明确授权 |
| QA-F-03：补充独立测试覆盖和原始证据 | 测试工程师 | 待指定；不得由最终独立 QA 兼任 | PENDING | 对象为候选 `f494487`；见本文件 Findings |
| QA-F-04：MySQL 专项或风险接受 | 测试负责人 / 交付负责人 | 待指定 | PENDING | H2 并发已通过，MySQL Profile 未独立执行 |
| 业务验收 | 业务所有者 | 待指定 | BLOCKED | QA Findings 关闭并由独立 QA 签署后执行 |
| 临时进入业务验收 | 业务所有者 | 当前用户代理待确认是否担任 | PENDING | 用户决定暂时跳过 QA 并进入下一步；只允许形成独立业务验收结论，不代表最终放行 |

仅记录本阶段实际证据及有权人员的原文结论；当前会话不代签独立 QA PASS、业务验收或最终交付决定。
