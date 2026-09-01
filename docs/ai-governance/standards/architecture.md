# 架构检查引导

> 本文件负责路由与验收，不复制项目架构正文。实际架构事实以 `docs/architecture.md`、Module `AGENTS.md` 和代码为准。

## 适用场景

新增或修改 Module、公开接口、依赖、Controller/Service/Repository 调用、事务边界、库存操作入口或跨团队契约时必须检查。

## 必须读取

1. 根及受影响 Module 的 `AGENTS.md`。
2. `docs/architecture.md`。
3. 受影响 Module 的 `pom.xml`、公开接口和实际调用方。
4. 已批准 Spec 中的所有权、接口、数据和事务决定。

## 固定检查

- 依赖方向是否仍为装配层 → 业务层 → 平台契约/组件。
- Controller 是否绕过业务入口直接访问 Repository。
- 业务代码是否绕过 `InventoryOperations` 修改库存。
- `platform-*` 是否被事业部任务越权修改。
- 状态、余额和流水是否保持一个事务内一致。
- 新公开接口和依赖是否有真实调用方及所有者批准。

## 输出与验收

输出进入 Standards 矩阵，包含规则 ID、Module、代码符号、依赖证据、结论和人工裁决。跨 Module 契约、依赖方向或事务发生变化且无批准时必须阻断。
