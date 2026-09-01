# T05 需求审核 Skill 运行产物

> 运行对象：“其他事业部也需要幂等、审计和 AI 审核流程，请复用仓储实现。”

## Original request

将仓储中可复用的幂等、审计和 AI 审核能力供其他事业部使用。

## Facts and evidence

- `platform-contracts` 已定义操作人、审计发布和统一异常：`README.md` 的模块所有权表及对应源码。
- 库存通过流水查询和请求字段匹配实现幂等：`InventoryService` 和 `InventoryMovement`。
- 出库取消通过单据保存幂等键和取消原因：`ShipmentOrder.matchesCancellation`。
- 现有 11 条集成测试覆盖库存、出库和移库主要行为：`WmsFlowIntegrationTest`。

## Assumptions

- 本训练用库存和出库两个差异模型代表两种事业部接入方式，不新增虚构业务模块。
- 对外 HTTP 契约、错误码和事务语义不变。
- Skill 以项目级资产分发，不在本任务中发布为公司级插件。

## Unknowns and questions by owner

- 公共研发中心：生产环境审计是入库还是发消息？故障时是阻断业务还是降级？
- 各事业部：幂等记录的保留期、重放结果和业务唯一键是什么？
- 安全/合规：审计事件的必填字段、脱敏和保留期尚未提供。

这些问题阻断生产级默认适配器的定稿，但不阻断训练用内存/日志适配器。

## Proposed acceptance criteria

1. Given 业务模块记录审计，When 调用平台组件，Then 操作人和时间由平台填充，业务传入 action/type/id。
2. Given 幂等键未使用，When 事业部提交判定，Then 返回新请求。
3. Given 已有记录与业务请求匹配/不匹配，When 提交判定，Then 分别返回安全重放/抛出业务指定的冲突错误。
4. Given 平台源码，Then 不得出现 SKU、库位、出库单状态等仓储语义。
5. Given 新组件接入，When 执行全部测试，Then 组件契约测试和原 11 条业务测试全部通过。

## Impact, ownership, and boundaries

- 平台契约和默认实现：公共研发中心所有，需契约测试。
- 库存/出库接入：仓储事业部所有，保留查询、匹配和事务。
- 服务组装：培训组确认。
- UI 契约、数据库迁移：不修改。

## Non-goals

- 不建立公司级幂等存储平台。
- 不引入 AOP、反射或流程引擎。
- 不替各事业部定义业务唯一键和重放语义。

## Readiness decision and reason

`READY_WITH_RECORDED_ASSUMPTIONS`

可按训练默认适配器实施；上线前必须由平台、事业部和合规回答上述生产级问题。
