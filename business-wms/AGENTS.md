# Module Agent Instructions: business-wms

本文件继承仓库根目录 `AGENTS.md`，只规定 `business-wms` 的局部边界。

## 1. 模块职责

- 所有者：仓储事业部。
- 承载仓库主数据、库存、入库、出库、移库、盘点及对应 Web 入口。
- 使用平台提供的稳定契约和默认组件，但不修改、复制或绕过平台实现。

## 2. 修改范围

- 业务实现和 Web 入口只能位于本模块对应领域包中。
- Controller 只能调用业务 Operations / Service，不得直接访问 Repository。
- 库存变化必须通过 `InventoryOperations`；其他领域不得直接修改 `InventoryBalance`。
- `InventoryService` 负责库存锁、余额校验、幂等和库存流水的一致实现。
- 出库、移库和盘点服务负责各自业务状态，将库存变化委托给 `InventoryOperations`。

## 3. 依赖边界

- 可以依赖 `platform-contracts` 和 `platform-web-starter`。
- 不得依赖 `training-server`。
- 未经对应所有者批准，不得修改 `platform-*`、公共 API、错误契约、数据库结构或跨模块依赖。
- 平台只复用技术机制；仓储规则不得为了代码复用而上收为平台行为。

## 4. 业务不变式

1. 可用量和占用量不得为负。
2. 出库只能扣减已占用库存。
3. 同一业务动作重复提交不得重复改变库存。
4. 每次库存变化必须产生对应库存流水。
5. 移库必须在同一事务中原子更新源和目标库存，并按稳定顺序加锁。
6. 盘点调整失败时不得把盘点单标记为已审批。

## 5. 验证

- 模块验证：`mvn -pl business-wms -am test`。
- 行为测试优先通过 `training-server` 的公开 API 或稳定业务入口执行。
- 库存写入变化必须覆盖成功、非法状态、库存不足、幂等和回滚；存在竞争风险时必须覆盖并发场景或明确记录剩余风险。
- 最终验证：`mvn clean verify`。

## 6. 停止条件

出现以下情况时停止并请求相关所有者批准：

- 需要修改平台模块、公开契约、错误码、数据库结构或事务语义。
- 需要绕过 `InventoryOperations` 或直接从 Controller 访问 Repository。
- 修改可能破坏库存不变式、幂等、流水完整性或并发安全。
- 无法建立表达业务风险的测试，或实现范围扩展到未批准领域。

