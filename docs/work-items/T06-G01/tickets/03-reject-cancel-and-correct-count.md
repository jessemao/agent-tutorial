# 03：驳回、重新编辑、取消与纠错

**状态：** ACCEPTED（开发负责人于 2026-09-13 验收完成）  
**Blocked by：** None（Ticket 02 已于 2026-09-13 验收通过）  
**Spec：** `docs/work-items/T06-G01/spec.md` v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06`

## 交付行为

不同操作者可对待审核盘点填写原因并驳回，用户通过明确的重新编辑动作把驳回单恢复为草稿；草稿或待审核盘点可填写原因取消并释放全部活动范围。已批准或已取消盘点保持不可变。若已批准盘点后来发现录入错误，用户可立即创建一张可选关联原单的新盘点，原单历史不被改写。

## 修改边界

- 允许修改：`business-wms` 盘点状态转换、原因/审计/范围释放/纠错关联及相应 HTTP Adapter；`training-server` 的驳回、重新编辑、取消、复制纠错页面和集成测试。
- 禁止修改：平台 Modules、库存调整和审核实现、Ticket 01—02 已批准创建/保存/提交语义、反审、物理删除、附件、治理规范和第三方 Skills。
- Module `AGENTS.md`：必须遵守根规则、`business-wms/AGENTS.md` 与 `training-server/AGENTS.md`。

## 验收条件

- [x] AC-12：`SUBMITTED` 填写非空原因后变为 `REJECTED`，保存操作者/时间/原因并继续占用范围；缺失原因拒绝。
- [x] AC-12：只有显式重新编辑可使 `REJECTED → DRAFT`，历史驳回事实仍可查询。
- [x] AC-13：`DRAFT` 或 `SUBMITTED` 填写原因后可取消、释放全部范围并不可恢复；缺失原因拒绝。
- [x] AC-13：取消后可创建覆盖相同维度的新盘点，没有残留范围占用。
- [x] AC-14：`APPROVED`、`CANCELLED` 对保存和本票转换返回非法状态；本票未实现审核或库存调整。
- [x] AC-15：驳回、重新编辑、取消分别满足幂等、版本和新键重复规则。
- [x] AC-17：已实现新盘点可选关联已批准原单并保护原单不可变；因 Ticket 04 尚未提供批准入口，端到端纠错场景留待 Ticket 04 后联调。
- [x] 列表、详情与页面刷新显示状态、原因、操作者、时间及纠错关联。

## 开发验证

- `T06CountRejectCancelIntegrationTest`：4 tests / 0 failures / 0 errors。
- `business-wms -am test`：BUILD SUCCESS；`training-server -am test`：57 tests / 0 failures / 0 errors / 3 skipped。
- `clean verify`、前端构建、执行 Skill 门禁、AI Governance 与差异空白检查全部通过。
- 纠错关联的完整 HTTP 流程需要 Ticket 04 产生 `APPROVED` 原单后补充联调；本票没有加入审核后门。

## 验证命令

```text
mvn -pl business-wms -am test
mvn -pl training-server -am test
mvn clean verify
```

## 停止条件

- 状态转换需要绕过盘点聚合或在页面复制业务判断。
- 取消无法原子释放全部范围，或并发创建可观察到部分释放。
- 纠错要求修改/删除原批准单或自动反转库存。
- 需要引入完整鉴权、附件、归档或反审能力。
