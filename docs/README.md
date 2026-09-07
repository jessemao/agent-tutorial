# 项目文档导航

> 用途：区分现行规范、课程执行材料、离线恢复材料和历史证据。  
> 限制：本页只提供导航，不复制各文档规则；发生冲突时按根 `AGENTS.md` 与 `STANDARDS.md` 规定停止并裁决。

## 1. 现行项目事实

- `architecture.md`：四模块边界、关键库存入口和业务不变式。
- `api-examples.http`：当前演示 API 请求样例。
- `THIRD_PARTY.md`：第三方来源和许可证登记。

这些文档必须与当前代码保持一致。接口、依赖或行为变化时，相关代码变更不得在文档更新前交付。

## 2. AI Coding 治理

- `ai-governance/README.md`：治理体系总览。
- `ai-governance/workflow.md`：按任务类型执行的统一流水线。
- `ai-governance/deliverables.md`：标准交付物关系。
- `ai-governance/roles-and-approvals.md`：业务、技术中台、事业部、UI、QA 和治理角色的决策权。
- `ai-governance/validation.md`：自动、辅助、人工和待接入门禁的真实状态。
- `ai-governance/change-management.md`：治理文档、Standards、Templates、Skills 和门禁的变更与回滚。
- `ai-governance/exceptions.md`：Standards 例外的有效期台账。
- `ai-governance/standards/`：架构、Clean Code、AI 安全、测试和文档检查入口。
- `ai-governance/templates/`：Review、Impact、Spec、Verification、Decision 和 Delivery 等固定格式。
- `agents/`：项目级领域文档与本地 issue tracker 约定。

治理文档不能代替任务 Spec、真实代码证据和人工批准。具体 Clean Code 规则只由锁定的项目级 Skills 维护。

## 3. 任务过程文档

- `work-items/<work-item-id>/`：单个需求、Bug、重构或复用任务的 Spec、Tickets、Design、Interface、Review、Verification、Decision 和 Delivery。
- `work-items/README.md`：过程目录的命名、边界和使用说明。

过程文档不得写入 `ai-governance/` 或 `training/evidence/`。具体输出结构以 `ai-governance/standards/documentation.md` 为准。

## 4. 课程执行材料

- `training/00_任务卡总目录与覆盖矩阵.md`：六类任务唯一导航。
- `training/03_两天课程映射与评分规则.md`：时间和评分。
- `training/04_两天讲师唯一执行稿_含演示与恢复.md`：讲师/助教使用，含答案和恢复顺序。
- `training/08_学员唯一入口手册.md`：学员环境、流程和提交入口。
- `training/T01_*` 至 `training/T06_*`：学员卡、讲师参考和评分细则。

T06 采用受控信息差考核。内部仓库可以保存讲师参考和历史证据，但制作学员分发包时必须排除 T06 讲师参考、历史 evidence、离线标准答案和答案 Git refs；具体分发检查由仓库外的 `培训教程/讲师材料/` 维护。

讲师参考不得提前整份发给学员；T02 使用新的分批收货起点 `s2-t02-start`，答案只在本轮实操完成并评审后建立。

## 5. 离线与异常恢复

- `training/offline-ai-pack/`：固定输入、预置输出、负例、人工裁决和恢复验收。
- `training/10_Docker课堂环境使用与交付.md`：课堂镜像构建和断网使用。

预置输出只用于教学对照。服务恢复后必须针对当前代码重新运行 Agent、测试和评审。

## 6. 历史证据

- `training/evidence/`：特定日期、代码版本、镜像或 Skill 的执行记录。

Evidence 只读保存，不是现行操作指令。不得修改历史结果以适配当前规则，也不得用历史日志证明新代码通过。

## 7. 文档维护门禁

- 新文档先确认归属，避免创建第二份 README、Standards、Workflow 或任务定义。
- 可执行文档必须标明用途、对象/状态、限制、完成信号和相关权威来源。
- 历史文档必须标明日期、版本和不可复用限制。
- 删除或移动文档前搜索并修复所有引用。
- 合并前运行 `scripts/validate-ai-governance.sh`、链接检查和 `git diff --check`。
