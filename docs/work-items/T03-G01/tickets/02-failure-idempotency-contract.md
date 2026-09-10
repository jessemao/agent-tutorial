# 02：完成失败回滚与幂等契约

**状态：** accepted
**Blocked by：** Ticket 01  
**Spec：** `docs/work-items/T03-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-04` / 当前代码版本

## 交付行为

调用方可获得稳定、可展示的移库失败结果；任何失败均不产生部分库存变化、流水或审计。同一幂等请求可安全重放，冲突请求被拒绝。

## 修改边界

- 允许修改：Ticket 01 建立的移库领域/Web/UI 路径及本 Ticket 测试。
- 禁止修改：`platform-*`、数据库结构、Module 依赖、既有错误响应结构及无关库存能力。
- Module `AGENTS.md`：实施前按根 `AGENTS.md` 自主加载涉及 Module 的规则。

## 验收条件

- [x] 先以失败测试覆盖本 Ticket 的契约差距。
- [x] 数量为零、负数或源目标相同返回 400。
- [x] 仓库或库位不存在、库位停用、跨仓、源库存不存在均返回 Spec 约定错误。
- [x] 库存不足和目标溢出时事务完整回滚，不新增流水或审计。
- [x] 相同幂等键与相同载荷重放时返回首次结果快照，且不新增记录。
- [x] 相同幂等键与不同载荷被拒绝。
- [x] UI 直接展示服务端稳定错误，不改写业务含义。

## 验证命令

```text
docker compose run --rm training-wms-mvn mvn -Dtest=T03TransferContractIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
docker compose exec -T frontend npm run build
```

## 停止条件

- 需要改变已批准的错误、幂等或审计语义。
- 需要绕过既有库存入口、直接访问 Repository 或弱化测试断言。
- 范围扩展至平台、数据库结构或既有公共契约。
