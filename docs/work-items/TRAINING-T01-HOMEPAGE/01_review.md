# 01 Review：TRAINING-T01-HOMEPAGE

## 元数据

| 字段 | 内容 |
| --- | --- |
| Agent/工具 | Codex，仓库搜索、Docker、真实浏览器 |
| 代码起点 | `f5d4640` |
| 当前代码版本 | T01 起始工作树 |
| Spec 路径/版本 | `spec.md` v1 |
| Standards ID/版本 | `STD-WMS-0.7-03` |

## 已读取材料

- 根 `AGENTS.md`：已读取。
- Module `AGENTS.md`：`training-server/AGENTS.md`。
- 任务材料：当前用户需求、T01 任务卡。
- 架构/API/测试：`docs/architecture.md`、`docs/training/06_UI静态契约包.md`、Controller 与 `WmsFlowIntegrationTest`。
- 使用的 Skill：`design-taste-frontend`，Clean Code 五个专项 Skill。

## 事实、假设与待确认项

### 已确认事实

1. T01 故障可由后端测试复现，但 `/` 当时返回 404。
2. 现有 API 已覆盖入库、创建出库、预占、取消与查询，不需要新后端契约。
3. T01 起始实现返回单据 `CANCELLED`，但库存保留可用 4、预占 6。

### 假设

1. 课堂使用者在可访问同源 Spring Boot 服务的浏览器中操作。

### 待人工确认

1. 最终视觉和课堂文案是否放行。

## 复现或澄清证据

- 命令/步骤：访问 `/`；在 T01 起始分支运行取消流程。
- 真实结果：首页实施前为 404；业务结果为 `CANCELLED / 4 / 6`。
- 日志/截图路径：当前真实浏览器会话，不写入历史 Evidence。

## Agent 定位结果

- 调用链或业务流程：首页 `fetch` → 既有 Controller → 既有业务入口。
- 目标 Module、类与方法：`training-server` 静态资源和首页入口集成测试。
- 根因或需求冲突：不存在后端能力缺口；缺口是课堂浏览器入口。
- 排除的错误方向及证据：不新建 Controller，因为现有 API 已能组成全流程。

## 请求人工裁决

- 需要确认的结论：只增加同源静态首页和入口测试。
- 建议下一步：在 T01 共同基座中实施，重放修复历史。
- 状态：`APPROVED_FOR_IMPLEMENTATION`。
