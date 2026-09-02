# T01 代码场景说明

## 版本映射

| 版本 | 产生方式 | 用途 |
| --- | --- | --- |
| S0 | 项目当前正确母版 | 课前验证和故障恢复 |
| S1-T01 | 在 S0 上应用 `introduce-bug.patch` | 讲师复现演示 |
| S2-T01 | 在干净 S0 上应用 `introduce-bug.patch` | 学员起始版 |
| S3-T01 | 在 S1/S2 上应用 `answer.patch` | 讲师标准答案，结果等于 S0 |

课堂稳定切换优先使用已验证标签：学员起点 `s2-t01-start`，讲师答案 `s3-t01-answer`。补丁仅用于讲解差异，不再作为现场恢复的首选方式。

S1 和 S2 的代码故障相同，区别在于讲师是否展示根因和标准答案。不为此复制整套 Maven 工程。

## 制作 S1/S2

在 `training-wms` 根目录执行：

```bash
git apply --check scenarios/T01/introduce-bug.patch
git apply scenarios/T01/introduce-bug.patch
```

然后执行目标测试：

```bash
mvn -pl training-server -am -Dtest=WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：测试失败，出库单为 `CANCELLED`，但可用量仍为 `4`、预占量仍为 `6`。

## 制作 S3/恢复 S0

学员版未做其他修改时，执行：

```bash
git apply --check scenarios/T01/answer.patch
git apply scenarios/T01/answer.patch
mvn clean verify
```

如学员已经修改代码，不要强行应用答案补丁。应先评审其差异，再使用干净 S0 工作目录恢复。

## 课堂前检查

- [ ] S0 执行 `mvn clean verify` 成功
- [ ] `introduce-bug.patch` 可应用
- [ ] 应用故障后目标测试按预期失败
- [ ] `answer.patch` 可应用
- [ ] 应用答案后全量测试成功
- [ ] 学员版不包含讲师参考和 `answer.patch`

## T01—T03 测试与 QA 交接口径

开发仅提交当前任务的 TDD 与必要回归（`02_verification.md`）；代码评审记录于 `03_review.md`。测试团队的工程师独立执行功能、页面/API、异常和回归验证，结果写入 `functional-test.md`；另一团队的 QA 审核规范、需求追溯、覆盖、证据和缺陷闭环，结论写入 `qa-review.md`。业务所有者另行验收，交付负责人在 `04_decision.md` 记录最终裁决。不能用开发全绿替代独立测试，不能用 QA 审核替代业务验收。

各阶段分别记录对象版本、具名负责人及团队、待确认事项、状态、结论时间与证据。测试工程师与 QA 不得由同一人员兼任；缺少负责人由讲师/交付负责人指定，未执行保持待执行，未确认保持待确认，不预填 PASS。修复后先更新开发证据，再由测试工程师重测，QA 审核新证据，必要时重新业务验收。

T01 仍按完整手写 Prompt 操作；职责分离不意味着引入预置 Skill。T02/T03 保留各自命令入口。操作与模板以对应学员卡及 `docs/ai-governance/roles-and-approvals.md` 为准。
