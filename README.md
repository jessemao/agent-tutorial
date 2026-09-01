# Training WMS

这是“AI辅助研发试点外部培训 v0.7”的轻量仓储实操代码库。

项目采用 **B02（ruoyi-vue-pro）的工程边界和测试思想**，并参考 **Open-WMS 类项目的仓储术语和流程**，但代码为培训目的独立实现，不是两个仓库的源码拼接。

## 培训目标

统一工程标准入口：[STANDARDS.md](STANDARDS.md)。Spec规定本次需求/设计边界，Standards规定跨任务标准，AGENTS.md要求Agent同时读取并执行。交付必须同时有Spec与Standards两轴证据。

评审时复制[Code Review 模板](docs/ai-governance/templates/code-review.md)，固定 Spec、Standards 和代码版本后逐条填写；不得仅输出概括性“符合规范”。

工程结构和代码质量分别从 `docs/ai-governance/standards/architecture.md`、`docs/ai-governance/standards/clean-code.md` 进入；具体 Clean Code 规则由项目级 Skills 执行，不能只看测试是否通过。

学员需要在使用 AI Coding 时完成两个质量闭环：

1. 先审核需求：发现状态、库存维度、权限、并发、幂等和审计缺口。
2. 再审核代码：用自动化测试和人工评审证明实现符合需求。

## 最小业务闭环

```text
入库确认 → 指定库位形成库存 → 出库单占用库存
                                ↓
                确认出库 / 取消并释放库存
                                ↓
                         记录库存流水
```

## 模块与所有权

| 模块 | 所有者 | 职责 | 事业部是否可修改 |
|---|---|---|---|
| `platform-contracts` | 公共研发中心 | 操作人、审计契约 | 否 |
| `platform-web-starter` | 公共研发中心 | 统一响应、异常和默认技术组件 | 否 |
| `business-wms` | 仓储事业部 | 仓库、库区、库位、库存、出入库 | 是 |
| `training-server` | 培训组 | 组合装配和演示数据 | 需讲师确认 |

依赖方向固定为：

```text
training-server → business-wms → platform-contracts
       └────────→ platform-web-starter
```

`business-wms` 不允许直接依赖平台实现，也不允许 Controller 或外部模块直接访问库存 Repository。

## 环境

- Java 17（当前课堂版本；如甲方强制JDK8，需使用经过单独适配和实测的课堂包）
- Maven 3.8+
- Spring Boot 2.7.18
- H2（默认，无需安装数据库）

## 构建和运行

```bash
mvn clean verify
mvn -DskipTests clean install
mvn -f training-server/pom.xml spring-boot:run
```

主机不安装Java/Maven时，可使用预热的课堂镜像：

```bash
export CLASSROOM_UID=$(id -u)
export CLASSROOM_GID=$(id -g)
docker compose -f compose.classroom.yml run --rm classroom
```

镜像内置`s2-t01-start`课程初始代码，默认在断网模式执行`mvn -o clean verify`，会复现T01预置的两条失败测试。镜像不含Git历史和后续任务答案；后续代码以Git中的`s2-*-start`和`s3-*-answer`标签/分支为准。启动服务可使用镜像内的`training-wms-start`。构建、导出、导入和验收方法见`docs/training/10_Docker课堂环境使用与交付.md`。

启动后可访问：

- 健康检查：`GET http://localhost:8080/actuator/health`
- H2 控制台：`http://localhost:8080/h2-console`
- 仓库接口：`/api/wms/**`

演示环境通过请求头 `X-Operator` 传递当前操作人。它只是培训适配器，不能当成生产鉴权方案。

## 课堂入口

- `AGENTS.md`：全局 Agent 行为、读取顺序、人工批准点和完成标准；各 Maven Module 的 `AGENTS.md` 在此基础上补充局部边界。
- `docs/ai-governance/README.md`：治理体系总入口；链接 Workflow、产物、Standards 引导、Templates、Skills 和自动校验。
- `docs/ai-governance/skills.md`：项目级 Skills 清单、Clean Code 上游来源和 Java 适配边界。
- `.agents/skills/`：AIHero 主链路 Skills、TDD/领域建模能力和五个 Clean Code 专项 Skills。
- `scripts/validate-ai-governance.sh`：检查治理文件、Module 规则、模板章节、Skill 名称和上游文件哈希。
- `docs/api-examples.http`：入库、创建出库单、预占、取消和库存查询的完整演示。
- `docs/training/00_任务卡总目录与覆盖矩阵.md`：T01—T06 的唯一目录、能力覆盖与设计状态。
- `docs/training/02_T01-T06_任务定义卡.md`：六个任务的已冻结边界。
- `docs/training/03_两天课程映射与评分规则.md`：任务与两天时间表、评分权重和裁剪规则。
- `docs/training/04_两天讲师唯一执行稿_含演示与恢复.md`：正式授课的唯一逐时执行稿、演示脚本和异常恢复手册。
- `docs/training/06_UI静态契约包.md`：平台、UI和事业部共同审核的字段、状态、错误和兼容规则。
- `docs/training/07_T01-T06四方角色任务矩阵.md`：技术中台、UI、仓储事业部和QA/测试的分工与交接点。
- `docs/training/08_学员唯一入口手册.md`：环境、任务、Skill、证据、提交和恢复的学员唯一入口。
- `docs/training/09_T01-T06基础_进阶_备用任务包.md`：针对不同进度小组的分层截止点。
- `docs/training/offline-ai-pack/`：无AI、无网络、输出偏离和环境污染时的预置演示、人工裁决和恢复验收包。
- `docs/training/10_Docker课堂环境使用与交付.md`：课堂Java/Maven预热镜像的构建、断网使用、导出、导入和放行规则。
- `docs/training/T02_调整入库分批收货规则_学员任务卡.md`：分批收货需求调整场景；代码标签待同步。
- `docs/training/T02_调整入库分批收货规则_讲师参考.md`：T02 的数量/状态结论、验收矩阵与观察点。
- `docs/training/T01_取消出库未释放库存_学员任务卡.md`：T01 Bug 修复学员完整任务。
- `docs/training/T01_取消出库未释放库存_讲师参考.md`：T01 根因、信息投放和参考修复。
- `scenarios/T01/`：T01 可重复应用的 S1/S2 故障差异和 S3 答案差异。
- `scenarios/T03/`：T03 从零新建需求的 S2/S3 版本边界。
- `docs/architecture.md`：模块边界、核心不变式和代码所有权。

## 当前阶段

当前 T06 第二轮验证共有 18 条业务集成测试和 3 条平台契约测试通过。T01—T06 均已提供可直接切换的 `s2-*-start` 学员起点和 `s3-*-answer` 讲师答案标签；交付审核必须直接记录已批准 Spec、Standards 版本、代码差异起点和测试证据。

## 来源与许可

- 工程结构参考：[YunaiV/ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro)，MIT License。
- 仓储术语参考：[yiruantong2014/open-wms](https://github.com/yiruantong2014/open-wms)，以仓库根目录 Apache-2.0 `LICENSE` 为准。
- 本目录代码为独立培训实现；如后续复制第三方具体文件，必须在 `docs/THIRD_PARTY.md` 逐项记录。
