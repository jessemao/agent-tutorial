# Training WMS Frontend

本目录是基于 `southliu/south-admin-react` 技术栈和后台布局模式裁剪的课堂前端。第三方来源、固定提交、许可证和本地修改见 `docs/THIRD_PARTY.md`。

## 课堂裁剪

- 保留：React、TypeScript、Vite、Ant Design、静态后台菜单、表格、表单和对话框。
- 移除：登录、Token、角色、动态权限菜单、国际化、KeepAlive 和通用系统管理模块。
- 边界：前端只调用现有 `/api/wms/*`，不实现库存或单据业务规则。
- T01：创建并预占出库单，页面复现“取消后库存未释放”。
- T02：创建计划数量为 10 的入库单，页面复现旧规则拒绝“首批收货 4”的需求差距。

## 课堂开发

在仓库根目录执行：

```bash
./scripts/classroom-up.sh
```

浏览器访问 `http://localhost:8080`。Vite 会热更新前端修改，`/api`、`/actuator` 和 `/h2-console` 由开发服务代理到 Spring Boot。生产构建由各任务的 `classroom-verify.sh` 统一执行，产物仍写入 `training-server/src/main/resources/static/`。
