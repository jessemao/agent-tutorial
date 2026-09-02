# Training WMS Frontend

本目录是基于 `southliu/south-admin-react` 技术栈和后台布局模式裁剪的 T01 课堂前端。第三方来源、固定提交、许可证和本地修改见 `docs/THIRD_PARTY.md`。

## 课堂裁剪

- 保留：React、TypeScript、Vite、Ant Design、静态后台菜单、表格、表单和对话框。
- 移除：登录、Token、角色、动态权限菜单、国际化、KeepAlive 和通用系统管理模块。
- 边界：前端只调用现有 `/api/wms/*`，不实现库存或单据业务规则。

## Docker 构建

在仓库根目录执行：

```bash
docker run --rm --platform linux/amd64 \
  -v "$PWD:/workspace" \
  -v training-wms-frontend-node-modules:/workspace/training-server/frontend/node_modules \
  -w /workspace/training-server/frontend \
  node:22-bookworm-slim npm ci --ignore-scripts

docker run --rm --platform linux/amd64 \
  -v "$PWD:/workspace" \
  -v training-wms-frontend-node-modules:/workspace/training-server/frontend/node_modules \
  -w /workspace/training-server/frontend \
  node:22-bookworm-slim npm run build
```

构建产物写入 `training-server/src/main/resources/static/`，Spring Boot 运行时不依赖 Node。
