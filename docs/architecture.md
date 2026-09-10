# Architecture Decision: Lightweight Training WMS

> 用途：记录当前四模块架构、依赖方向、库存深模块和必须保持的不变式。  
> 证据来源：各 Module POM、公开接口、业务实现和集成测试。  
> 限制：这是课程架构事实，不是生产部署架构；修改 Module、依赖、公开入口、事务或不变式前必须更新并取得所有者批准。

## 1. 决策

采用四模块单体，不采用完整微服务。模块通过明确接口协作，数据库和事务仍在一个进程内，以保证课堂可运行、可复现和可测试。

```text
training-server → business-wms → platform-web-starter → platform-contracts
                         └────────────────────────────→ platform-contracts
```

| Module | 所有者 | 职责 | 禁止事项 |
| --- | --- | --- | --- |
| `platform-contracts` | 技术中台 | 稳定的平台契约和最小类型 | 业务语义、Web/JPA 实现、下游依赖 |
| `platform-web-starter` | 技术中台 | 响应、异常、审计、幂等等通用 Web 机制 | 仓储判断、依赖业务/装配模块 |
| `business-wms` | 仓储事业部 | 主数据、库存、单据、盘点和 Web 入口 | 应用装配、绕过平台所有权 |
| `training-server` | 课程维护团队 | 组合根、配置、演示数据和集成测试 | 仓储业务规则 |

局部修改权限、验证命令和停止条件以各 Module 的 `AGENTS.md` 为准。

## 2. 库存深模块

库存模块对调用方只暴露 `InventoryOperations`：

- `receive`
- `reserve`
- `release`
- `ship`
- `transfer`
- `adjustFromCount`
- `getBalance`
- `getExistingBalance`

库存行锁、余额校验、幂等记录和库存流水属于实现细节。Controller、单据领域和装配层不得直接访问库存 Repository 或修改 `InventoryBalance`。

### T03 能力演进

| 版本 | 移库能力 |
| --- | --- |
| 基线 `s2-t03-start` / `e27b3d3` | 已准备 SKU 303 在源库位的 10 件期初库存，并支持按单个或全部有效库位独立查询；没有公开移库 Interface、HTTP 入口或课堂页面。 |
| T03 目标状态 | `InventoryOperations.transfer` 在单事务内按库位 ID 稳定加锁，原子更新两端余额，以双流水保存幂等快照，并由 `/api/wms/inventory/transfer` 和课堂移库页面提供单一入口。 |

统一公开入口不等于巨型实现类。内部职责可以按锁定、校验、流水或查询等真实变化原因组织，但不得创建透传层、万能 Service 或无调用方接口。

## 3. 关键不变式

1. `availableQuantity >= 0`。
2. `reservedQuantity >= 0`。
3. 出库只能扣减已占用库存。
4. 同一业务动作重复提交不能重复改变库存。
5. 每次库存变化必须生成对应库存流水。
6. 库存维度至少包含 SKU、仓库和库位。
7. 移库必须在同一事务中原子更新源/目标，并以稳定顺序加锁。
8. 盘点只在审批时调整库存；调整失败不得把盘点单标记为已审批。
9. 单据状态、库存、流水及需同步记录的审计不得留下部分成功。

当前 T02 起点中的入库单只支持一次性足量收货：`CREATED → RECEIVED`。这是待调整的旧业务规则，不能被当作通用库存不变式；允许分批收货的新状态、数量语义与重复提交规则，必须先在 T02 Spec、Design 与 Interface 中明确，再修改领域模型和公开接口。

任何需求若要改变这些不变式，必须先形成新 Spec、分析调用方和数据影响，并由仓储事业部、技术中台和 QA 共同批准。

## 4. 事务与并发边界

- 状态编排由对应业务 Service 负责，库存写入委托 `InventoryOperations`。
- 一次业务动作涉及的单据、库存和流水保持批准的事务语义。
- 多库存行操作按稳定顺序加锁；不得依赖数据库偶然执行顺序。
- H2 用于默认课堂回归；锁、唯一约束或隔离级别相关结论需要指定 MySQL Profile 补充验证。

## 5. 非目标

- 微服务注册发现、网关和服务拆分。
- Redis、MQ、工作流引擎和分布式事务。
- 完整多租户、生产鉴权和复杂数据权限。
- PDA、波次、灯光拣选和 3D 仓库。
- 生产监控、备份、容灾和自动发布。

Agent 不得用“更接近生产”作为理由自行引入这些能力。

## 6. 架构变更门禁

以下变化必须先停止实施并取得相关所有者批准：新增 Module/依赖；修改公共接口或错误契约；改变数据库结构、事务、锁顺序或库存入口；将事业部语义上收平台；改变 `training-server` 的组合根职责。

交付时按 `docs/ai-governance/standards/architecture.md` 输出 Standards 矩阵。测试通过不能替代架构与所有权评审。
