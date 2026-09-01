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
