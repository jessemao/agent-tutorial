# 01：完成原子移库主路径

**状态：** accepted  
**Blocked by：** None  
**Spec：** `docs/work-items/T03-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-04` / 当前代码版本

## 交付行为

用户可在课堂 UI 中发起同仓不同库位的库存移动；页面自动生成移库单号，系统原子扣减源库位、增加目标库位并返回两端最新库存快照。页面可从既有双流水重新加载已完成移库任务，目标库存余额不存在时自动创建。

## 修改边界

- 允许修改：`business-wms` 库存领域与 Web 接入、`training-server` 对应 UI/API 类型和本 Ticket 集成测试。
- 禁止修改：`platform-*`、数据库结构、Module 依赖、Maven/npm 依赖及既有公共契约。
- Module `AGENTS.md`：实施前按根 `AGENTS.md` 自主加载涉及 Module 的规则。

## 验收条件

- [x] 先以失败测试证明当前系统不支持该主路径。
- [x] 源可用库存 10 移动 4 后，源为 6、目标为 4，保留量不变。
- [x] 支持移动全部可用库存。
- [x] 成功操作恰好产生两条库存流水和一条审计记录。
- [x] 课堂 UI 可发起移库、自动生成移库单号，并展示完整成功详情。
- [x] 刷新页面后可从只读列表接口恢复已完成任务，不新增移库任务表或状态机。

## 验证命令

```text
docker compose run --rm training-wms-mvn mvn -Dtest=T03TransferHappyPathIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
docker compose exec -T frontend npm run build
```

## 停止条件

- 需要修改平台、数据库结构、依赖或增加第二个 HTTP 接口。
- 无法在一个事务内按库位 ID 稳定升序加锁并完成两端更新。
- 发现必须改变既有行为才能通过回归。
