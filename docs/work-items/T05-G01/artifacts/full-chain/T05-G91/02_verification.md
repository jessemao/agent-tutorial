# 02 Verification：项目级 Work Item 状态查询 Skill

## 元数据

| 字段 | 内容 |
| --- | --- |
| Work Item ID | T05-G91 |
| 阶段 | 第二轮 REWORK 开发验证完成并经开发负责人验收 |
| 代码起点 | `bfba7413ed5fc95a34b8cd0110fbc4da48b01795` |
| 被验证对象 | 初始候选 `e8e1d211e532f9d13d413b19eea964ec4aa44b24`；后续 REWORK 以 Skill、OpenAI 配置和锁文件内容 SHA-256 固定，候选提交由下一次 Review 解析 |
| Spec / Design / Interface | v1 / APPROVED / 2026-09-11 |
| 环境 | T05 隔离验收仓库（路径不作为可移植证据） |

## 修改范围

- 新增 `.agents/skills/work-item-status/SKILL.md`。
- 新增 `.agents/skills/work-item-status/agents/openai.yaml`，关闭隐式调用。
- 在 `skills-lock.json` 登记项目来源、文件和 SHA-256。
- 更新 T05-G91 当前阶段文档与开发验证 artifacts。
- 未增加脚本，未修改五个既有项目阶段 Skills、第三方 Skills、治理规范、Templates、课程卡或业务代码。

## Red–Green 证据

| 阶段 | 验证 | 结果 | 证据 |
| --- | --- | --- | --- |
| Red | 对尚不存在的 `.agents/skills/work-item-status` 执行 Skill Creator 校验 | exit 1：`SKILL.md not found` | `artifacts/development-validation.txt` |
| Green | 对完成后的 Skill 执行同一校验 | exit 0：`Skill is valid!` | `artifacts/development-validation.txt` |
| 前向 Red | 独立 Agent 查询审批状态尚未同步的 T05-G91 | 正确返回 `WORK_ITEM_STATUS_CONFLICT`，无下一入口 | `artifacts/development-forward-validation.md` |
| 前向 Green | 同步全部导航事实后由独立 Agent第三次查询 T05-G91 | PASS：无冲突，唯一下一动作是开发负责人验收当前单切片，查询前后 Git 状态一致 | `artifacts/development-forward-validation.md` |

## 自动验证

| 层级 | 命令 | 结果 | 摘要 |
| --- | --- | --- | --- |
| Skill 结构 | `uv run python .../quick_validate.py .agents/skills/work-item-status` | PASS | `Skill is valid!` |
| 供应链 | `shasum -a 256` 两个 Skill 文件并对照 `skills-lock.json` | PASS | 两个哈希完全一致 |
| 治理回归 | `./scripts/validate-ai-governance.sh` | PASS | AI governance 与既有项目 Skills 完整 |
| 课程回归 | `./scripts/classroom-verify.sh T05` | PASS | 五个既有阶段 Skill 与 T05 课程门禁通过 |
| 格式 | `git diff --check` | PASS | 无空白错误 |

原始结果见 `artifacts/development-validation.txt`。

## 验收映射

| AC | 开发证据 | 当前结论 |
| --- | --- | --- |
| AC-01 DISCOVERING/当前阶段报告 | Skill 固定输出、状态判断和下一动作顺序；最终独立查询返回完整 IMPLEMENTING 报告和唯一人工动作 | PASS（DISCOVERING 专项留给独立功能测试复核） |
| AC-02 等待人工裁决 | Skill 明确 Review Finding 后只能由人裁决；完整样本留给独立功能测试 | 开发契约已覆盖，独立测试待执行 |
| AC-03 版本/来源冲突 | 两次独立查询均正确识别文档状态冲突，不给 next_action | PASS |
| AC-04 稳定失败 | 不存在任务返回 `WORK_ITEM_NOT_FOUND`；其余三类已固定在 Interface | 部分开发验证通过，完整矩阵待独立测试 |
| AC-05 绝对只读 | 独立查询前后三次及复查前后 Git 状态一致 | PASS |
| AC-06 结构与来源 | quick_validate、哈希、治理及课程门禁 | PASS |

## 失败、跳过与剩余风险

- 第一次和第二次前向查询暴露的是 Work Item 导航事实未同步，不是 Skill 误判；均已保留并修复，第三次独立复查通过。
- 当前没有通用自动门禁识别第六个 `work-item-status` 的业务语义；结构由 Skill Creator 和锁哈希证明，行为必须依赖独立会话验收。
- 等待人工裁决、证据不完整和证据受限的完整场景尚未由测试工程师执行，不能用本开发验证代替 `functional-test.md`。
- QA 尚未开始，不能据此宣称整体交付通过。

## 开发交接

- 开发负责人验收：CONFIRMED；用户原话“验收完毕没问题。进入下一个动作”。
- 已授权形成固定候选提交；候选形成后的唯一入口为 `/work-item-review T05-G91`。
- 不得自动执行 Review，不得生成独立测试、QA、Decision 或推送。

## Review REWORK 验证

### 修复对象

- 初始被审候选：`e8e1d211e532f9d13d413b19eea964ec4aa44b24`。
- 人工指令：`帮我修复这7项`，授权修复 `03_review.md` 的 SPEC-01～02、STD-01～05；旧 Review Findings 保持 OPEN，等待新候选重新评审。
- 修复后的 Skill SHA-256：`3defd0cddf3fe863c3e006f4fc0211ab05dece833ca0fb378d7c3edfcbd9bc93`。
- OpenAI 配置 SHA-256：`b80c0eb119c6cb4ee0b672f29937ca02ca88285685a0385d32ed0841f3dc6b76`。

### 七项修复映射

| Finding | 修复 | 验证 |
| --- | --- | --- |
| SPEC-01 / STD-01 | README 不再把基线伪装成当前 HEAD；实施对象由内容哈希固定，候选由 Review 时解析 | 文档一致性检查；新候选形成后再次查询自身 |
| SPEC-02 | ID 限定为单一路径段，规范化后必须仍在 Work Item 根目录；越界返回 NOT_FOUND | `../../AGENTS.md` 在任何越界读取前被拒绝 |
| STD-02 | 补 DISCOVERING、等待裁决、INCOMPLETE、EVIDENCE_BLOCKED 和越界五类同接缝行为验证 | `artifacts/rework-behavior-validation.md` 全部 PASS |
| STD-03 | 锁记录增加 `sourceVersion=T05-G91-v1` 与 `license=project-internal-course-material`，保留逐文件哈希 | 锁字段与 SHA-256 检查 |
| STD-04 | 删除 Verification 与原始日志中的本机绝对路径 | 文本搜索与 `git diff --check` |
| STD-05 | 删除 17 步重复 Workflow；只保留最早未满足关口的推导原则并引用 Workflow/Skills 契约 | Skill 文本审查；治理回归 |

### 补充行为结果

| 场景 | 结果 |
| --- | --- |
| DISCOVERING | PASS：唯一入口 `/work-item-discover T05-DISCOVER` |
| 等待人工裁决 | PASS：只有人工裁决 Finding，无 Skill 下一入口 |
| 状态证据不完整 | PASS：`WORK_ITEM_STATUS_INCOMPLETE` |
| 证据受限 | PASS：`WORK_ITEM_EVIDENCE_BLOCKED`，未读取受限正文 |
| ID 路径越界 | PASS：读取前返回 `WORK_ITEM_NOT_FOUND` |
| 全部场景零写入 | PASS：查询前后 Git 状态一致 |

### REWORK 后停止点

- 7 项修复与开发验证已完成；开发负责人验收原话：`验收这次 REWORK`。
- 用户显式调用 `/work-item-review T05-G91`，已授权形成新候选；旧 `03_review.md` 作为候选内审计记录保留，Findings 只能由新候选重新 Review 后关闭。

## 第二轮 REWORK：剩余三项

### 授权与对象

- 人工指令：`帮我完成剩余3项`。
- 来源 Review：`03_review.md` 的 REWORK 后复评，SPEC-01、STD-01、STD-02。
- 修复起点：`db7ccb386267b1741b1dddebe742c1ca92e1dcee`。
- 被验证实现内容：Skill SHA-256 `3defd0cddf3fe863c3e006f4fc0211ab05dece833ca0fb378d7c3edfcbd9bc93`；OpenAI 配置 SHA-256 `b80c0eb119c6cb4ee0b672f29937ca02ca88285685a0385d32ed0841f3dc6b76`；锁文件 SHA-256 `b56d799e70b956f1689f2ab5345c6753a9744ca9b42f90f8d30c297637af9090`。
- 执行时间：2026-09-11 16:59:54 CST。

### 可复现行为夹具

- 夹具位于 `artifacts/fixtures/`，分别表达 DISCOVERING、等待 Finding 裁决、状态证据不完整和证据受限；路径越界使用输入 `../../AGENTS.md`，不创建越界夹具。
- 复现方式：在基于候选提交的隔离检出中，把每个夹具目录复制为 `docs/work-items/<fixture-id>/`，显式调用当前 Skill 的 `/work-item-status <fixture-id>`，并在每次调用前后执行 `git status --short`。
- Agent 调用命令、Git 命令、时间、版本、逐场景原始输出和前后状态见 `artifacts/rework-round-2-raw-output.txt`。

### 结果

| Finding | 修复与证据 | 当前开发结论 |
| --- | --- | --- |
| SPEC-01 | README 和 Verification 统一表达第二轮 REWORK 已完成、待验收；独立 Agent 自查询当前工作树 | PASS：stage=REWORK、无冲突、唯一动作=开发负责人验收；形成候选后仍须重新查询并复评 |
| STD-01 | 当前阶段、产物表、下一动作和待确认表使用同一 REWORK 事实 | PASS |
| STD-02 | 纳入四组可复现夹具，记录真实命令、时间、对象哈希和逐场景原始输出 | PASS |

### 第二轮停止点

- 开发负责人验收：CONFIRMED；用户原话“验收完成 /work-item-review T05-G91”。
- 已授权形成第二轮固定候选并执行 Review；旧 Review Findings 不由实施阶段自动关闭，只能由新候选复评关闭。
