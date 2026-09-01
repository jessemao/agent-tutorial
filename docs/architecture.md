# Architecture Decision: Lightweight Training WMS

## 决策

采用四模块单体，不采用完整微服务。模块之间通过小接口形成清楚的 seam，数据库和事务仍在一个进程内，保证课堂可运行性。

## 深模块

库存模块对调用方只暴露 `InventoryOperations`：

- `receive`
- `reserve`
- `release`
- `ship`
- `transfer`
- `adjustFromCount`
- `getBalance`
- `getExistingBalance`

库存行锁、余额校验、幂等记录和库存流水属于实现细节。删除该模块后，这些复杂性会重新散落到入库、出库、盘点等调用方，因此它值得作为独立模块存在。

## 关键不变量

1. `availableQuantity >= 0`。
2. `reservedQuantity >= 0`。
3. 出库只能扣减已占用库存。
4. 业务动作重复提交不能重复改变库存。
5. 每次库存变化必须生成库存流水。
6. 库存维度至少包含 SKU、仓库和库位。
7. 移库必须在一个事务中原子更新源库位和目标库位，并以稳定顺序加锁。
8. 盘点差异只能通过盘点审批触发，调整失败时不得将盘点单标记为已审批。

## 非目标

- 微服务注册发现和网关。
- Redis、MQ、工作流引擎和分布式事务。
- 完整多租户和复杂数据权限。
- PDA、波次、灯光拣选和3D仓库。
