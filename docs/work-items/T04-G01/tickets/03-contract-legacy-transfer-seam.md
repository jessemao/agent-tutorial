# 03：收缩遗留移库 Seam 并完成等价性验证

**状态：** accepted  
**Blocked by：** None（Ticket 02 已于 2026-09-10 由开发负责人验收）  
**Spec：** `docs/work-items/T04-G01/spec.md` / v1 / APPROVED  
**Standards：** `STD-WMS-0.7-06` / 代码起点版本

## 交付行为

系统只保留 `InventoryOperations` 这一个公开库存业务 Interface；遗留移库 Interface 和纯透传 Adapter 被删除。所有 HTTP/UI、库存业务、移库、错误、幂等、事务、并发、流水、审计和任务列表行为与起点等价，架构文档准确反映完成状态。

## 修改边界

- 允许修改：删除已零调用的 `InventoryTransferOperations` 与 `LegacyInventoryTransferService`；同步 `docs/architecture.md` 当前事实；生成 `02_verification.md` 及本 Ticket 必需验证证据。
- 禁止修改：统一 Interface/Implementation 的业务语义、Controller/测试逻辑、HTTP/UI、数据模型、Repository、事务/锁/幂等/流水/审计、平台、依赖、已批准 Spec/Design/Interface 和后续 Review/Decision 文档。
- Module `AGENTS.md`：读取根、`business-wms/AGENTS.md`、`training-server/AGENTS.md`；旧类型只有零引用后才可删除。

## 验收条件

- [x] 删除前搜索证明旧 Interface/Adapter 在 Java 生产与测试源码中只有自身定义/实现、无调用方；删除后 Java 生产与测试源码搜索为零。分析、设计与 Ticket 中的历史证据不计为代码残留。
- [x] `InventoryOperations` 是唯一公开库存业务 Seam，Spring 启动和注入正常。
- [x] 同一 T04 baseline 前后均为 0 failures、0 errors，并在 `02_verification.md` 逐项映射 AC-01—AC-08。
- [x] 受影响 Module 回归、前端生产构建、项目 `mvn clean verify` 和健康检查通过，记录实际版本、命令、数量和结果。
- [x] `docs/architecture.md` 与完成后代码一致，只描述现行单一 Interface，不改写历史证据。
- [x] 无数据库、依赖或外部契约 Diff；回滚方式为逆序恢复 Contract、Migrate、Expand，无数据处理。

## 验证命令

```text
rg -n "InventoryTransferOperations|LegacyInventoryTransferService" business-wms/src training-server/src/test --glob '*.java'
./scripts/classroom-test.sh T04 baseline
docker compose -f compose.classroom.yml exec -T classroom /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl business-wms -am test
docker compose -f compose.classroom.yml exec -T frontend npm run build
docker compose -f compose.classroom.yml exec -T classroom /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify
curl -fsS http://localhost:8080/actuator/health
```

## 停止条件

- 旧类型仍有调用方，或删除导致编译/注入失败。
- 任一前后行为映射、测试、构建或健康检查失败。
- 需要改变已批准 Spec、Implementation、外部契约、数据、平台或依赖。
- 无法证明被测代码、Verification 和候选提交版本一致。

## 人工验收

- 结论：ACCEPTED
- 时间：2026-09-10 20:54:19 +0800
- 开发负责人原话：“测试完成没问题。 /code-review T04-G01”
