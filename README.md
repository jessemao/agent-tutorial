# Training WMS

`training-wms` 是“AI 辅助研发实战培训 v0.7”的 Java / Spring Boot 仓储教学仓库。它用一个可在两天内理解和运行的四模块单体，训练学员在 AI Coding 中完成需求审查、边界控制、测试先行、代码评审和人工放行。

本项目参考 `ruoyi-vue-pro` 的工程分层思路和 Open-WMS 类项目的仓储术语，但代码为培训独立实现，不是第三方仓库源码拼接。

## 1. 项目边界

### 解决什么

- 演示入库、库存、出库、移库和盘点的最小业务闭环。
- 演练 Bug 修复、需求调整、新需求、重构、平台复用和综合交付六类任务。
- 演示技术中台、UI、事业部和 QA 如何共同约束 Agent。
- 提供 Docker 离线课堂环境、任务分支、答案标签和异常恢复材料。

### 不解决什么

- 不是生产 WMS，不提供完整鉴权、租户、数据权限、监控、备份或生产发布方案。
- 不采用微服务、MQ、Redis、工作流引擎或分布式事务。
- 不允许把培训用 `X-Operator` 请求头当成生产身份认证。
- 不承诺 H2 行为可以替代 MySQL 生产验证；MySQL 只通过指定 Profile 做补充检查。
- 不允许把 AI 输出、测试全绿或答案分支直接视为人工验收通过。

## 2. 开始工作前

Agent 和学员必须依次读取：

1. [AGENTS.md](AGENTS.md)——全局行为、人工批准点和停止条件。
2. 受影响 Module 的 `AGENTS.md`——局部所有权、边界和验证命令。
3. [统一 Workflow](docs/ai-governance/workflow.md)——不同任务类型的固定流程。
4. 当前任务卡及需要时的已批准 Spec。
5. [STANDARDS.md](STANDARDS.md)及相关 Standards 引导文件。
6. 相关架构、API、测试、源码和项目级 Skills。

未完成读取、发现/设计、影响分析和人工范围批准前，不得修改业务代码。

## 3. 模块与所有权

| Module | 所有者 | 只负责 | 禁止承载 |
| --- | --- | --- | --- |
| `platform-contracts` | 技术中台 | 审计、幂等、操作人和异常等稳定契约 | 仓储语义、Web/JPA 实现、事业部特例 |
| `platform-web-starter` | 技术中台 | 统一响应、异常映射及平台默认实现 | 库存/单据规则、对业务模块的反向依赖 |
| `business-wms` | 仓储事业部 | 主数据、库存、入出库、移库、盘点及 Web 入口 | 平台实现、应用装配、跨所有者规则 |
| `training-server` | 课程维护团队 | Spring Boot 组合根、配置、演示数据和集成测试 | 仓储业务规则和绕过公开入口的测试逻辑 |

固定依赖方向：

```text
training-server → business-wms → platform-web-starter → platform-contracts
                         └────────────────────────────→ platform-contracts
```

实际依赖、核心入口和仓储不变式见 [架构说明](docs/architecture.md)及各 Module 的 `AGENTS.md`。跨 Module 契约、依赖方向、事务或所有权变化必须先取得相关所有者批准。

## 4. 环境与验证

- Java：17（课程运行时；源码目标兼容级别以 `pom.xml` 为准）
- Maven：3.8+
- Spring Boot：2.7.18
- 默认数据库：H2
- 课堂镜像：`training-wms-classroom:0.7`，`linux/amd64`

优先使用课堂脚本，避免依赖学员主机环境：

```bash
./scripts/classroom-up.sh
```

浏览器访问 `http://localhost:8080`。前端修改自动热更新，后端源码修改自动编译并重启 Spring Boot；不需要重启 Docker。任务测试和最终验证使用 `scripts/classroom-test.sh` 和 `scripts/classroom-verify.sh`。完整使用与限制见项目外教学材料中的 `../教学材料/教师与课程/09_Docker课堂环境使用与交付.md`。

主机环境仅作为备用：

```bash
mvn clean verify
mvn -DskipTests clean package
mvn -f training-server/pom.xml spring-boot:run
```

启动后可访问：

- 健康检查：`GET http://localhost:8080/actuator/health`
- H2 控制台：`http://localhost:8080/h2-console`
- 仓储 API：`/api/wms/**`
- 请求示例：[docs/api-examples.http](docs/api-examples.http)

## 5. AI Coding 治理入口

完整文档分区和维护责任见 [docs/README.md](docs/README.md)。

| 文档/目录 | 作用 | 不能替代 |
| --- | --- | --- |
| [AGENTS.md](AGENTS.md) | Agent 的读取顺序、权限、停止和完成条件 | Spec、Standards、业务规则 |
| [STANDARDS.md](STANDARDS.md) | 跨任务项目强制标准 | 本次需求与设计决定 |
| [治理总览](docs/ai-governance/README.md) | 各治理层职责和产物关系 | 实际执行证据 |
| [Workflow](docs/ai-governance/workflow.md) | Bug、需求、重构、复用等任务流水线 | 人工批准 |
| [角色与审批](docs/ai-governance/roles-and-approvals.md) | 各所有者的决策权、代理和升级方式 | Agent 或测试自动批准 |
| [Standards 引导](docs/ai-governance/standards/) | 架构、Clean Code、AI 安全、测试和文档检查入口 | Skills 的具体规则设置 |
| [Templates](docs/ai-governance/templates/) | 统一 Review、Impact、Spec、Decision 等格式 | 业务答案 |
| [Work Items](docs/work-items/) | 每个任务的 Spec、Tickets、Design、Interface、执行与验收产物 | 项目级规范或课程通用材料 |
| [.agents/skills](.agents/skills/) | 可重复执行的发现、设计、实施和评审步骤 | 最终放行决定 |
| [校验门禁](docs/ai-governance/validation.md) | 区分已自动化、辅助、人工和待接入检查 | 业务裁决或最终放行 |

交付必须分开检查：

- Spec 轴：批准的业务/设计条目是否落实到代码、测试和真实结果。
- Standards 轴：项目规则和适用 Skill 规则是否满足，例外是否获批。

Review 只提供发现和建议；Decision 记录人的正式裁决；Delivery 汇总最终版本、证据、风险和回滚方式。

## 6. 培训任务入口

项目仓库只保留学员可直接使用的任务卡和入口手册：

- [学员入口](docs/training/08_学员唯一入口手册.md)
- [T01 学员任务卡](docs/training/T01_取消出库未释放库存_学员任务卡.md)
- [T02 学员任务卡](docs/training/T02_调整入库分批收货规则_学员任务卡.md)
- [T03 学员任务卡](docs/training/T03_从零新建库内移库_学员任务卡.md)
- [T04 学员任务卡](docs/training/T04_统一库存业务入口_学员任务卡.md)
- [T05 学员任务卡](docs/training/T05_平台组件与Skill复用_学员任务卡.md)
- [T06 学员任务卡](docs/training/T06_库存盘点与差异调整_学员任务卡.md)

讲师参考、评分细则、课程执行稿、PPT、离线演练包和历史课堂证据均在项目外的 `../教学材料/` 目录独立管理，不属于代码基座。学员始终从指定 `s2` 标签创建独立分支，不直接在标签或答案上开发。

## 7. 已知限制与安全约束

- 不提交真实账号、令牌、客户数据、完整 AI 账号对话或 Docker 镜像 tar。
- 不修改、删除或覆盖其他小组的分支和工作目录。
- 不直接在答案标签上开发；从指定起点创建小组分支。
- 不使用旧日志证明新代码通过；验证记录必须对应当前代码版本和环境。
- 不因课堂时间不足降低验收条件；改用备用任务或明确标记未完成。
- 生产化所需鉴权、密钥管理、数据库迁移、可观测、备份和发布回滚均不在本课程实现范围内。

## 8. 来源与许可

- 工程结构参考：[YunaiV/ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro)，MIT License。
- 仓储术语参考：[yiruantong2014/open-wms](https://github.com/yiruantong2014/open-wms)，以其仓库 License 为准。
- 第三方 Skills 和文件来源见 [docs/THIRD_PARTY.md](docs/THIRD_PARTY.md)及 `skills-lock.json`。
- 如复制新的第三方文件，必须在合并前记录来源、版本、许可证和本地修改。
