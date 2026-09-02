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
| `GOV-SKILL-01` | AUTOMATED | Clean Code Skills 存在、名称正确且哈希与锁定文件一致 | `scripts/validate-ai-governance.sh`、`skills.sha256` | 阻断交付 |
| `GOV-TERM-01` | AUTOMATED | 已废弃治理术语未重新进入现行治理文档 | `scripts/validate-ai-governance.sh` | 阻断交付 |
| `GOV-WORKITEM-01` | ASSISTED | Work Item ID、状态、必需产物和 `N/A` 理由与任务类型匹配 | Agent 对照 `deliverables.md`，人审核 | 缺失项阻断实施/交付 |
| `GOV-AGENT-01` | ASSISTED | 受影响 Module 的 `AGENTS.md` 已读取且未与根规则冲突 | Review/Impact 读取清单与人工核对 | 冲突未裁决时阻断 |
| `GOV-VERSION-01` | ASSISTED | Spec、Standards、代码起点和被审版本可还原 | Work Item 元数据与 Git 查询 | 缺失/不可还原时阻断 |
| `GOV-TRACE-01` | ASSISTED | Spec/规则到代码、测试、结果和决策的追溯 | `code-review.md`、Verification、Decision | 无证据的“通过”阻断 |
| `GOV-ARCH-01` | ASSISTED | 分层依赖、所有权、公开契约和事务边界 | Maven 依赖/代码搜索、架构评审、人工裁决 | MUST 违反时阻断 |
| `GOV-CODE-01` | ASSISTED | Clean Code Skills 对当前 Diff 生成逐条 Finding | Skill 输出和 Standards 矩阵 | 阻塞项阻断，预警交人裁决 |
| `GOV-TEST-01` | ASSISTED | 目标测试、Module 回归和全量验证对应被审代码 | Maven 命令与 `04_verification.md` | 失败、跳过或版本不对应时阻断 |
| `GOV-REVIEW-01` | ASSISTED | Spec 与 Standards 两张矩阵独立完整 | `code-review.md` 与人工核对 | 任一轴缺失时阻断 |
| `GOV-AI-SEC-01` | ASSISTED | 数据、不可信指令、工具权限和供应链符合 AI 安全标准 | 安全检查清单、Diff、外部操作记录与人工核对 | 泄密、越权或来源不明时阻断 |

当前仓库没有 CI 配置证明上述脚本已在远程自动执行，因此只能声明“本地校验通过”，不能声明“CI 门禁已接入”。

## 3. 固定执行顺序

1. 在仓库根目录运行 `scripts/validate-ai-governance.sh`。
2. 针对当前 Work Item 核对任务类型、产物适用性、状态和审批证据。
3. 执行目标测试、受影响 Module 回归和 `mvn clean verify`。
4. 执行架构、Clean Code 与 AI 安全检查，将结果写入 Standards 矩阵。
5. 核对 Spec 矩阵、Standards 矩阵、Decision 和 Delivery 是否引用同一代码版本。

## 4. 证据输出

- 治理脚本的原始输出保存到 Work Item `artifacts/`。
- 测试命令和结果写入 `04_verification.md`，必要时引用 `artifacts/` 日志。
- 架构、Clean Code、AI 安全和追溯检查写入 `code-review.md` 的 Standards 矩阵。
- 人工裁决写入 `06_decision.md`；例外另外登记到 `exceptions.md`。

## 5. 自动化变更门禁

将 `ASSISTED` 检查升级为 `AUTOMATED` 时，必须提供：实现脚本/插件、正反例、误报处理、本地与 CI 一致性、失败恢复、所有者批准和实际执行证据。只修改本文件的状态不构成自动化。
