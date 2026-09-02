# 04 Verification：South Admin React 前端

## 被验证对象

| 字段 | 内容 |
| --- | --- |
| 代码起点 | `1b750aa` |
| 主分支实现 | `b6132c0` |
| T01 初始分支实现 | `2410d0e`，`group-demo/t01` |
| Spec | `spec-v3.md` v3 |
| Standards | `STD-WMS-0.7-03` |
| 环境 | Node 22 Docker；Java 17；`training-wms-classroom:0.7` Linux/amd64；H2；真实浏览器 |

## 验证结果

| 层级 | 命令/操作 | 真实结果 | 结论 |
| --- | --- | --- | --- |
| 验证先行 | 旧页面运行新首页契约测试 | 3 个测试中 2 个失败：缺少框架标记和 `/assets/app.js` | EXPECTED RED |
| 前端构建 | Node Docker 内 `npm run build` | TypeScript 与 Vite 7.3.6 通过；业务包 gzip 8.87KB | PASS |
| 依赖审计 | Node Docker 内 `npm audit --audit-level=low` | 0 vulnerabilities | PASS |
| 首页目标测试 | 两个分支 Docker 内 `TrainingHomepageIntegrationTest` | 3/3 通过 | PASS |
| 主分支全量验证 | Docker 内 `mvn -o -q clean verify` | exit 0；38 通过、0 失败、1 个 MySQL Profile 守卫跳过 | PASS |
| 初始分支浏览器 | 新增 6 件、取消、切换库存明细 | `已取消 / 库存 10 / 占位 6 / 有效 4` | PASS |
| 服务健康 | `GET /actuator/health` | `UP` | PASS |

## Spec 验收映射

| AC | 实现/证据 | 真实结果 | 结论 |
| --- | --- | --- | --- |
| AC-06 React 管理后台 | Vite 入口、MockMvc、浏览器 | 加载 `south-admin-react` 标记和 React 根节点 | PASS |
| AC-07 South Admin 信息架构 | `AppShell`、Ant Design 页面组件 | 深色侧栏、顶部栏、页签、表单、表格和 Modal 可见可用 | PASS |
| AC-08 无鉴权 | 源码扫描与浏览器 DOM | 没有登录、退出、角色、权限、Token 或修改密码流程 | PASS |
| AC-09 初始分支复现 | 浏览器订单 `SO-T01-D7AD3B69B7BA` | 取消后占位 6、有效 4，并显示库存校验异常 | PASS |
| AC-10 答案分支行为 | 主分支全量 WMS 回归 | 取消库存释放相关测试通过 | PASS |
| AC-11 Docker 构建 | `frontend-build-verification.txt` | TypeScript、Vite、依赖审计均通过 | PASS |

## 环境异常记录

- 首次拉取 `node:22-bookworm-slim` 时 Docker 报 `no space left on device`。
- 只清理未被容器引用的悬空镜像层，释放 926MB；没有删除课堂镜像、运行容器或持久化数据。
- 重试后镜像拉取、依赖安装和构建成功。该异常可用于课堂演示“环境失败不是代码失败”的证据判断。

## 剩余风险

- Ant Design 框架包 gzip 323.61KB，适合本地课堂，但尚未为公网弱网络进一步按页面懒加载。
- 本任务没有引入浏览器 E2E 测试框架；课堂 UI 由入口契约测试和真实浏览器记录共同验证。

## 结论

`READY_FOR_DECISION`
