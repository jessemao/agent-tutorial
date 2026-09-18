# 任务卡 C：Playwright 自动化 UI 验收

## 1. 验收目标

通过真实浏览器和公开 UI 重复执行盘点调整与驳回两条用户路径，生成可对应候选提交的 HTML 报告和失败证据。

## 2. 参与角色

- 执行人：测试工程师。
- 证据核对：开发负责人。
- 业务结果核对：项目经理 / 业务所有者。

## 3. 前置条件

- 课堂 Docker 镜像包含当前基线对应的 Playwright 和 Chromium。
- 已使用 `./scripts/classroom-down.sh` 和 `./scripts/classroom-up.sh` 建立干净环境。
- 已记录 `git rev-parse HEAD`，工作区没有未说明修改。

## 4. 执行

将组号替换为本组 Work Item ID：

```bash
./scripts/classroom-ui-acceptance.sh TASK1 V2-T1-G01
```

脚本会创建新的时间戳目录，不覆盖旧证据：

```text
docs/work-items/V2-T1-G01/artifacts/ui-acceptance/<run-id>/
├── report/
│   └── index.html
├── results/
│   └── <failed-test>/   # 仅失败时含截图、Trace 和视频
└── run.txt              # 候选提交、时间和 Playwright 退出码
```

## 5. 自动化检查项

- [ ] 页面可从公开菜单进入“库存盘点”。
- [ ] 正差场景经过 `DRAFT → COUNTED → SUBMITTED → ADJUSTED`。
- [ ] 正差场景显示差异 `2`、一条流水，刷新后仍可见。
- [ ] 驳回场景进入 `REJECTED`，不显示调整流水，刷新后仍可见。
- [ ] 测试只通过页面公开行为验证，不读取数据库或 Repository。

## 6. 人工核对

- [ ] `run.txt` 中的 `candidate_commit` 与 M4 候选提交一致。
- [ ] Playwright 退出码为 `0`，HTML 报告显示两条用例通过。
- [ ] 失败时已查看截图、Trace 和视频，并记录环境问题或缺陷 ID。
- [ ] 自动化结果已转记到当前 Work Item 的 `functional-test.md`。

## 7. 验收记录

- 候选提交：
- 执行人 / 时间：
- 报告路径：
- Playwright 结果：`PASS / FAIL / BLOCKED`
- 人工业务结论：`PASS / RETURN / BLOCKED`
- 缺陷 ID / 返回阶段：

Playwright `PASS` 只表示两条 UI 用例通过，不代表事务、并发、MySQL 专项或最终交付自动通过。
