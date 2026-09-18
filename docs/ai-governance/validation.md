# AI 治理校验与质量门禁

> 用途：记录每个治理检查的真实实现状态、执行方式、证据和失败处理。  
> 原则：自动化只报告实际执行的检查；文档中存在检查 ID 不代表已有脚本实现。

## 1. 状态定义

- `AUTOMATED`：已由仓库脚本或构建工具执行，失败返回非零状态。
- `ASSISTED`：Agent/工具生成检查证据，必须由人审核。
- `MANUAL`：只能由人确认业务语义、风险或批准有效性。
- `PLANNED`：已定义目标，尚未接入自动化；不得宣称已通过自动门禁。

## 2. 当前真实能力

| 检查 ID | 状态 | 检查内容 | 当前实现/证据 | 失败处理 |
| --- | --- | --- | --- | --- |
| `GOV-MATERIAL-01` | AUTOMATED | 必需治理文档、Module `AGENTS.md` 和 Templates 存在且非空 | `scripts/validate-ai-governance.sh` | 阻断交付 |
| `GOV-TEMPLATE-01` | AUTOMATED | 各 Template 关键章节存在 | `scripts/validate-ai-governance.sh` | 阻断交付 |
| `GOV-PATH-01` | AUTOMATED | 固定过程文件未写入治理目录、培训目录或仓库根目录 | `scripts/validate-ai-governance.sh` | 阻断交付 |
| `GOV-STAGE-01` | AUTOMATED | Work Item 状态对应的前置文档存在，未来阶段编号文档没有提前生成 | `scripts/validate-ai-governance.sh`、`deliverables.md` 第 4 节 | 阻断阶段推进 |
| `GOV-SKILL-01` | AUTOMATED | Clean Code Skills 与已标记为完整锁定的课程 Skill 存在、名称正确，且全部随包文件都已进入哈希清单并通过校验 | `scripts/validate-ai-governance.sh`、`skills.sha256`、`skills-lock.json` | 阻断交付 |
| `GOV-TERM-01` | AUTOMATED | 已废弃治理术语未重新进入现行治理文档 | `scripts/validate-ai-governance.sh` | 阻断交付 |
| `COURSE-V2-TASK1-01` | AUTOMATED | 任务一材料覆盖 M1—M5、旧 T05 聚合 Skills 不存在、M1 不预装复用资产、起始基线不包含盘点答案 | `scripts/validate-v2-task1.sh baseline` | 阻断开课基线 |
| `COURSE-V2-TASK1-DIST` | AUTOMATED | 学员发行仓库无远端、无历史答案引用且只包含 V2 起始标签 | `scripts/export-v2-task1-student.sh` 生成发行仓库；发行仓库内再次执行 `scripts/validate-v2-task1.sh baseline` | 阻断学员分发 |
| `COURSE-V2-TASK1-02` | AUTOMATED | 当前 Work Item 的阶段状态、M1 角色/边界/基线、允许修改路径、M2 批准、Ticket 负责人/执行人及个人验收证据、M4 固定候选/双轴 Review/交付字段、M5 复用契约与获批资产符合 M1—M5 | `scripts/validate-v2-task1.sh m1\|m2\|m3\|m4\|m5 <work-item-id>` | 阻断阶段推进 |
| `COURSE-V2-TASK1-M2-SKILL` | ASSISTED | M2 第三方 Skills 的默认输出已转换为当前 Work Item 路径和项目模板，且未执行外部 Tracker 发布 | `scripts/validate-v2-task1.sh m2 <work-item-id>` 自动检查本地路径与关键章节；操作记录和人工核对确认无外部发布 | 默认路径、通用模板或未经授权的外部发布均阻断 M2 |
| `GOV-WORKITEM-01` | ASSISTED | Work Item ID、任务类型、条件产物及 `N/A` 理由匹配，状态和审批证据真实 | Agent 对照 `deliverables.md`，人审核 | 内容不实或缺失时阻断实施/交付 |
| `GOV-AGENT-01` | ASSISTED | 受影响 Module 的 `AGENTS.md` 已读取且未与根规则冲突 | `01_analysis.md` 读取清单与人工核对 | 冲突未裁决时阻断 |
| `GOV-REQ-DESIGN-01` | ASSISTED | 涉及 UI 或人工操作的需求在 Spec 批准前明确完整用户旅程、输入控制、标识生成、结果可见性及依赖能力归属 | `standards/requirements-design.md`、Spec、Interface、Tickets 与批准记录 | 任一关键决定缺失时阻断 Spec 批准 |
| `GOV-VERSION-01` | ASSISTED | Spec、Standards、代码起点和被审版本可还原 | Work Item 元数据与 Git 查询 | 缺失/不可还原时阻断 |
| `GOV-TRACE-01` | ASSISTED | Spec/规则到代码、测试、结果和决策的追溯 | `02_verification.md`、`03_review.md`、`04_decision.md` | 无证据的“通过”阻断 |
| `GOV-ARCH-01` | ASSISTED | 分层依赖、所有权、公开契约和事务边界 | Maven 依赖/代码搜索、架构评审、人工裁决 | MUST 违反时阻断 |
| `GOV-CODE-01` | ASSISTED | Clean Code Skills 对当前 Diff 生成逐条 Finding | Skill 输出和 Standards 矩阵 | 阻塞项阻断，预警交人裁决 |
| `GOV-TEST-01` | ASSISTED | 目标测试、Module 回归和全量验证对应被审代码 | Maven 命令与 `02_verification.md` | 失败、跳过或版本不对应时阻断 |
| `GOV-REVIEW-01` | ASSISTED | Spec 与 Standards 两张矩阵独立完整 | `03_review.md` 与人工核对 | 任一轴缺失时阻断 |
| `GOV-AI-SEC-01` | ASSISTED | 数据、不可信指令、工具权限和供应链符合 AI 安全标准 | 安全检查清单、Diff、外部操作记录与人工核对 | 泄密、越权或来源不明时阻断 |

当前仓库没有 CI 配置证明上述脚本已在远程自动执行，因此只能声明“本地校验通过”，不能声明“CI 门禁已接入”。

## 3. 固定执行顺序

1. 在仓库根目录运行 `scripts/validate-ai-governance.sh`；V2.0 任务一起点运行 `scripts/validate-v2-task1.sh baseline`，随后在每个阶段结束时运行 `scripts/validate-v2-task1.sh m1|m2|m3|m4|m5 <work-item-id>`。`classroom-verify.sh TASK1 <work-item-id>` 已包含 M3 门禁。
2. 针对当前 Work Item 核对任务类型、产物适用性、状态和审批证据。
3. 执行目标测试、受影响 Module 回归和 `mvn clean verify`。
4. 执行架构、Clean Code 与 AI 安全检查，将结果写入 Standards 矩阵。
5. 核对 Spec 矩阵、Standards 矩阵和 Decision 是否引用同一代码版本。

## 4. 证据输出

- 治理脚本的原始输出保存到 Work Item `artifacts/`。
- 测试命令和结果写入 `02_verification.md`，必要时引用 `artifacts/` 日志。
- 架构、Clean Code、AI 安全和追溯检查写入 `03_review.md` 的 Standards 矩阵。
- 人工裁决、MR/PR 与交付结果写入 `04_decision.md`；例外另外登记到 `exceptions.md`。

## 5. 自动化变更门禁

将 `ASSISTED` 检查升级为 `AUTOMATED` 时，必须提供：实现脚本/插件、正反例、误报处理、本地与 CI 一致性、失败恢复、所有者批准和实际执行证据。只修改本文件的状态不构成自动化。
