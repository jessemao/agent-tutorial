# 03 Agent Task：TRAINING-T01-HOMEPAGE

## 已批准输入

- Spec 路径/版本：`spec-v3.md` v3。
- Impact 批准记录：`02_impact.md`。
- Standards ID/版本：`STD-WMS-0.7-03`。
- 代码差异起点：`f5d4640`。
- 适用 Module `AGENTS.md`：`training-server/AGENTS.md`。

## 目标行为

- 访问 `/` 后进入真实 WMS 风格的出库订单列表。
- 用户新增演示单、选择单据并取消，再到库存明细页验收修复前后结果。

## 目标代码与测试接缝

- Module：`training-server`。
- 目标文件：`training-server/frontend/` React 源码及 Spring 静态构建产物。
- 首个失败测试：旧静态页不包含 South Admin React 应用标记和 Vite 构建资源。
- 相关调用方：课程浏览器、现有 WMS Controller。

## 执行步骤

1. 将入口测试调整为真实出库订单与库存明细页面契约，并先确认旧页面不能通过。
2. 在 `training-server/frontend/` 建立最小化 South Admin React 工程，固定依赖和来源。
3. 用 Ant Design 实现后台壳层、静态菜单、页签、筛选、表格、对话框和状态反馈。
4. 接入新增演示单、取消、刷新和库存明细查询；不增加鉴权。
5. 在 Node Docker 中构建，在课堂 Docker 中运行目标测试。
6. 在真实浏览器里运行完整 T01 复现。
7. 运行 Module 回归和全量验证。

## 允许与禁止

### 允许

- 修改批准的前端源码、静态构建产物、入口测试和 Work Item 文档。

### 禁止

- 在 JavaScript 中直接改数据或复制后端业务写逻辑。
- 修改现有 API、业务代码、事务、数据库或依赖。

## 停止条件

- 需要改变 Spec、公开契约、依赖方向、事务或批准范围。
- 无法建立有效测试或浏览器结果与预期冲突。

## 人工批准

`APPROVED`；用户，UI/契约所有者及课程讲师代理，2026-09-02；范围为真实 WMS 页面替换且不实现鉴权。
