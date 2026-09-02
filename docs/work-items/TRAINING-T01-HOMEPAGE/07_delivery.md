# 07 Delivery：South Admin React 前端

## 交付版本

- 主分支实现：`b6132c0`。
- T01 初始课堂分支：`group-demo/t01`，提交 `2410d0e`。
- 前端源码：`training-server/frontend/`。
- Spring 静态产物：`training-server/src/main/resources/static/`。
- 页面入口：`http://localhost:8080/`。

## 已交付能力

- South Admin 风格的侧栏、顶部栏、页签和内容区。
- Ant Design 筛选表单、出库表格、库存表格、Modal、Alert 与消息反馈。
- 新增演示单、取消出库、刷新和库存核对完整链路。
- 静态菜单，无登录、Token、角色、权限、动态菜单或修改密码。
- Node Docker 可重复构建，Spring Boot 运行时不需要 Node。

## 验证摘要

- 前端依赖审计：0 vulnerabilities。
- TypeScript + Vite 生产构建：通过。
- 首页目标测试：3/3 通过。
- 主分支全量测试：38 通过、0 失败、1 跳过。
- 初始分支浏览器：稳定显示取消后占位 6、有效 4。

## 课堂操作

1. 打开首页，在“出库管理 → 出库订单”点击“新增演示单”。
2. 选中单据并点击“取消出库”。
3. 打开“库存明细”，读取单据与库存事实。
4. 将失败案例交给 Agent 定位；人审批修复目标、范围和最终结果。

## 回滚与恢复

- 前端可通过普通 Git 反向提交整体回滚，不影响后端数据和契约。
- H2 数据通过重建课堂容器恢复。
- Docker 空间不足时只清理确认未使用的悬空层，不删除课堂镜像或数据卷。

## 最终确认

- 当前状态：`WAITING_FOR_DELIVERY_DECISION`。
- 放行人/时间：待课程讲师填写。
