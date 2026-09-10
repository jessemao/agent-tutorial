# 03：验证并发、回归与架构文档

**状态：** accepted
**Blocked by：** Ticket 02  
**Spec：** `docs/work-items/T03-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-04` / 当前代码版本

## 交付行为

正向与反向移库并发执行时不死锁、不超扣且库存总量守恒；完整回归通过，架构文档明确区分基线能力与本 Work Item 的目标状态。

## 修改边界

- 允许修改：仅在失败并发测试证明必要时最小修正移库并发路径；`training-server` 并发/回归测试；对应架构文档。
- 禁止修改：事务隔离级别、数据库结构、`platform-*`、错误契约及无关功能。
- Module `AGENTS.md`：实施前按根 `AGENTS.md` 自主加载涉及 Module 的规则。

## 验收条件

- [x] 先增加反向并发保护测试；首次执行即通过，未伪造 Red。
- [x] H2 下反向并发移库无死锁，余额非负且总量守恒。
- [x] MySQL 下执行等价并发验证并通过；该验证不可省略。
- [x] 受影响 Module 回归与全量构建通过。
- [x] 架构文档明确标出移库能力的基线状态与 T03 目标状态。
- [x] 命令、环境与结果只记录到 `02_verification.md`，不复制到设计或 Ticket。

## 验证命令

```text
docker compose run --rm training-wms-mvn mvn -Dtest=InventoryConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=false test
docker compose run --rm training-wms-mvn mvn test
docker compose exec -T frontend npm run build
docker compose run --rm training-wms-mvn mvn clean verify
MySQL 等价验证命令：实施时从仓库现有环境入口解析，不得猜测或新建入口。
```

## 停止条件

- 需要改变事务隔离级别、数据库结构、平台能力或错误契约。
- MySQL 验证环境不可用、验证失败或结果与 H2 冲突。
- 完整回归失败，或架构文档与实际实现无法保持一致。
