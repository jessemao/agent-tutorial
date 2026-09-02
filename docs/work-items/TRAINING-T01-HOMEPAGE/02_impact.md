# 02 Impact：TRAINING-T01-HOMEPAGE

## 影响范围

| Module | 所有者 | 类/方法或契约 | 影响 | 可改/禁改 |
| --- | --- | --- | --- | --- |
| `training-server` | 课程维护团队 | React 前端源码、静态构建产物、`GET /` | 使用 South Admin React 重构课堂入口 | 可改 |
| `business-wms` | 仓储事业部 | 既有 HTTP API | 只被调用，语义不变 | 禁改 |
| `platform-*` | 技术中台 | `ApiResponse`、异常契约 | 只被页面读取 | 禁改 |

## 依赖与调用方

- 上游调用方：课程讲师和学员浏览器。
- 下游依赖：既有 WMS HTTP API。
- 依赖方向是否变化：否。
- 新增公开接口/Module：无。
- 新增前端构建依赖：React 19、Vite 7、Ant Design 6、TypeScript 5；仅用于课堂 UI 与构建，不进入后端依赖图。

## 数据、状态与事务

- 数据表/实体：不变更。
- 状态变化：页面发起现有入库和出库状态流转。
- 事务归属：不变，仍由业务入口管理。
- 幂等/并发/回滚影响：不改后端语义；页面每次演示使用新键。

## API 与 UI 契约

- 请求/响应字段：既有 API 字段不变。
- 状态/错误码：不变，页面使用 `success/code/message/data` 契约。
- 兼容性：旧 API 调用方无影响。
- UI 框架：基于 `southliu/south-admin-react` 固定提交 `9810d29697f914d91de24f58a5be4d41c4e0a789` 的布局与组件模式进行 MIT 许可下的最小化适配。
- 鉴权影响：不新增登录、角色、菜单权限或鉴权依赖；页面仅沿用后端审计适配器所需的固定课堂操作人头。

## 测试影响

- 首个失败测试或验收测试：入口测试要求 South Admin React 标记和构建资源，旧静态页应失败。
- 前端构建：Node Docker 内 `npm ci && npm run build`。
- Module 回归：`mvn -pl training-server -am test`。
- 全量验证：`mvn clean verify`。

## 修改边界

### 允许修改

- `training-server/frontend/`
- `training-server/src/main/resources/static/`
- `training-server/src/test/java/com/acme/training/TrainingHomepageIntegrationTest.java`
- `training-server/AGENTS.md`，只补充课堂静态操作页边界及禁止项。
- 本 Work Item 文档。

### 禁止修改

- `business-wms`、`platform-*`、Maven POM、数据库和所有既有 API。
- T01 起始故障与修复实现。

## 人工批准

| 结论 | 批准人/角色 | 时间 | 备注 |
| --- | --- | --- | --- |
| `APPROVED` | 用户，课程讲师/交付负责人 | 2026-09-02 | 明确要求实现首页复现 |
| `APPROVED` | 用户，UI/契约所有者及课程讲师代理 | 2026-09-02 | 真实 WMS 页面返工，不做鉴权 |
| `APPROVED` | 用户，UI/契约所有者及课程讲师代理 | 2026-09-02 | 使用 South Admin React 重构，继续裁剪鉴权 |
