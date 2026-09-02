# Design v3：South Admin React 课堂前端

## 架构

```text
training-server/frontend/
├── src/
│   ├── api/           # 现有 WMS HTTP API 适配
│   ├── components/    # South Admin 风格布局组件
│   ├── pages/         # 出库订单、库存明细
│   └── App.tsx        # 静态菜单与视图编排
└── vite.config.ts
        │ build
        ▼
training-server/src/main/resources/static/
        │ Spring Boot 同源提供
        ▼
浏览器 ───────────────→ 既有 /api/wms/*
```

## 上游采用与裁剪

| South Admin 能力 | 本项目处理 |
| --- | --- |
| React + TypeScript + Vite | 采用 |
| Ant Design Layout/Menu/Tabs/Table/Form/Modal | 采用 |
| 深色侧栏、白色顶部栏、内容页签 | 采用并适配 WMS |
| 动态菜单和权限 Store | 裁剪，改为静态 WMS 菜单 |
| 登录、Token、刷新权限、退出 | 裁剪 |
| KeepAlive、i18n、图表和通用系统页 | 裁剪 |
| UnoCSS | 不引入；本次只有一个课堂页面，使用集中 CSS 降低构建复杂度 |

## 组件边界

- `AppShell`：只负责后台壳层、菜单、页签和移动端折叠。
- `ShipmentPage`：查询、选择、新增和取消单据；不判断库存业务规则。
- `InventoryPage`：查询并显示 API 返回的库存事实。
- `wmsApi`：统一请求头、错误解析和已有 API 路径。
- `useTrainingScenario`：编排课堂 HTTP 调用和页面状态，不直接修改业务数据。

## 失败处理

- 网络或后端失败通过 Ant Design `message` 和页面 `Alert` 展示，不吞异常。
- 创建、预占、取消按现有独立 API 顺序执行；任一步失败立即停止后续动作。
- 取消后只根据查询结果展示库存状态，不在前端推测代码根因。

## 构建与运行

- Node 构建只在 Docker 中执行，生成带哈希的静态资源。
- Spring Boot 运行时不依赖 Node、npm 或外部 CDN。
- `package-lock.json` 固定解析版本；第三方来源、许可证和本地修改记录在 `docs/THIRD_PARTY.md`。

## 回滚

- React 源码与构建产物作为一个提交回滚。
- 后端无契约和数据变更，不需要数据库恢复。
