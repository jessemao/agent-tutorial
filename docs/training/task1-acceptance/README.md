# 任务一验收操作手册

本手册用于验证“库存盘点与差异调整”的业务结果和 UI 主路径。人工验收与 Playwright 自动化使用同一候选提交，但不相互代替。

## 1. 验收角色

| 角色 | 责任 |
| --- | --- |
| 项目经理 / 业务所有者 | 确认业务目标、操作路径、差异结果和是否满足业务验收条件 |
| 开发负责人 | 固定候选提交，确认环境、公开入口和已知风险，根据证据作出 `PASS / RETURN / BLOCKED` 决定 |
| 测试工程师 | 独立执行人工任务卡和 Playwright，核对页面、状态、报告与候选提交 |

QA 是非强制阶段；企业流程未启用 QA 时，在 `04_decision.md` 记录 `N/A`、替代证据和剩余风险。

## 2. 验收材料

验收前准备以下材料：

- 已固定的候选提交，工作区无未说明修改。
- 当前 Work Item 的 `spec.md`、`design.md`、Tickets、`02_verification.md` 和 `03_review.md`。
- [任务卡 A：UI 盘点与差异调整](01_UI盘点与差异调整_验收任务卡.md)。
- [任务卡 B：UI 驳回与状态保护](02_UI驳回与状态保护_验收任务卡.md)。
- [任务卡 C：Playwright 自动化 UI 验收](03_Playwright自动化UI验收_验收任务卡.md)。
- `docs/ai-governance/templates/functional-test.md`，用于记录独立测试结论。

固定起始数据：仓库 `1`、货位 `1`、SKU `303`、可用量 `10`、预留量 `0`。一轮验收前必须重建课堂运行环境，不得复用上轮修改后的 H2 内存数据。

## 3. 验收前准备

将组号替换为本组 Work Item ID：

```bash
git rev-parse HEAD
git status --short
./scripts/classroom-down.sh
./scripts/classroom-up.sh
./scripts/classroom-verify.sh TASK1 V2-T1-G01
```

记录候选提交、启停时间、数据库/Profile 和命令结果。任一命令失败时停止验收，不得将环境失败写成业务通过。

## 4. 执行顺序

1. 测试工程师按任务卡 A 完成一次正差盘点，核对 `ADJUSTED`、差异 `2`、一条流水和刷新后结果。
2. 重建课堂环境，按任务卡 B 驳回盘点，核对 `REJECTED`、无调整流水和刷新后状态。
3. 再次重建课堂环境，按任务卡 C 运行 Playwright。
4. 打开 `docs/work-items/<work-item-id>/artifacts/ui-acceptance/<run-id>/report/index.html`，核对用例数、失败截图、Trace、视频和 `run.txt` 中的候选提交。
5. 测试工程师将实际结果写入当前 Work Item 的 `functional-test.md`；项目经理单独完成业务验收；开发负责人在 `04_decision.md` 作最终决定。

## 5. 证据要求

每条结论至少记录：角色/人员、候选提交、环境、开始结束时间、预期、实际结果、证据路径、缺陷 ID 和 `PASS / FAIL / BLOCKED`。截图、视频或 HTML 报告不能代替对状态、差异和流水的实际核对。

## 6. 自动化与人工的边界

- Playwright 验证公开页面的可重复行为，不读取 Repository 或数据库内部状态。
- 脚本只记录执行结果和候选提交，不生成人工签字或最终放行结论。
- 事务、并发、幂等冲突和 MySQL 差异仍使用已批准的 API/集成测试，不要强迫业务人员通过 UI 构造内部技术状态。
