# 01：建立安全、可重复的 Work Item 建档入口

**状态：** ready-for-agent  
**Blocked by：** None（可立即开始）  
**Spec：** `docs/work-items/T05-G01/spec.md` v1.1（APPROVED）
**Standards：** `add14f73bf857604e5f738fe35111ab4ef1b3e05` 中的 AI Governance、Templates、AI Security 与项目 Skills 约束

## 交付行为

用户显式调用 `work-item-start` 并提供 Work Item ID、任务类型、必要事实和可选附件后，系统能在新会话中定位任务卡，安全创建标准 README 与 input evidence，记录真实 Git 版本，并停在建档人工验收点。目录冲突、缺材料、Git 未知和无效/敏感附件必须无覆盖地停止。

## 修改边界

- 允许修改：`.agents/skills/work-item-start/`、`skills-lock.json`、本 Work Item 的 `02_verification.md` 与必要 `artifacts/`。
- 禁止修改：其他 Skill、所有第三方 Skill、业务代码与测试、T01～T05 任务卡、治理规范、Templates、门禁脚本和其他 Work Item。
- Module `AGENTS.md`：仅根 `AGENTS.md` 适用；本 Ticket 不进入业务 Module。

## 验收条件

- [ ] `SKILL.md` 明确输入、读取顺序、允许写入、固定产物、停止条件、禁止动作、下一入口和验收。
- [ ] `agents/openai.yaml` 禁止隐式调用，Skill 以项目来源和正确内容哈希登记到 `skills-lock.json`。
- [ ] T01 Bug 与 T02 需求调整使用两个临时 ID、两个独立 Agent 会话，生成结构一致且输入隔离的 README 与 `inputs/input-evidence.md`。
- [ ] 已占用目录、缺必要事实、Git 信息不可得和无效/敏感附件均停止，不覆盖文件、不生成 `01_analysis.md` 或后续产物。
- [ ] 不复制需求分析方法，不修改 T01 手写教学方式或第三方 Skill。
- [ ] 深度验证日志和对应版本写入 `02_verification.md`/`artifacts/`，通过人工验收后才能开始 Ticket 02。

## 验证命令

```text
./scripts/validate-t05-skills.sh work-item-start
./scripts/validate-ai-governance.sh
git diff --check
```

## 停止条件

- 需要修改第三方 Skill、Templates、门禁、任务卡或业务代码。
- 无法用临时 Work Item 隔离成功/失败验证，或验证会覆盖已有任务。
- 任一失败路径产生越界文件、状态推进或虚构字段。
- 当前 Ticket 的实施需要改变已批准 Spec、Design 或 Interface。
