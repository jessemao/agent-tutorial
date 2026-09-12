# 02：建立按任务类型路由的发现入口

**状态：** ready-for-agent  
**Blocked by：** 01：建立安全、可重复的 Work Item 建档入口（含 T01/T02 深度验证）  
**Spec：** `docs/work-items/T05-G01/spec.md` v1.1（APPROVED）
**Standards：** `add14f73bf857604e5f738fe35111ab4ef1b3e05` 中的 Workflow、Deliverables、AI Security 与项目 Skills 约束

## 交付行为

已通过建档验收的 Work Item 显式调用 `work-item-discover` 后，系统根据任务类型选择既有专业发现能力，读取仓库事实，形成统一 Analysis 骨架，只把必须由所有者决定的问题交给人，并停在范围批准点。

## 修改边界

- 允许修改：`.agents/skills/work-item-discover/`、`skills-lock.json`、本 Work Item 的开发验证证据。
- 禁止修改：`work-item-start` 及其他已完成/未开始 Skill、第三方 Skill、业务代码与测试、任务卡、治理规范、Templates 和门禁脚本。
- Module `AGENTS.md`：Skill 运行时必须读取目标任务涉及的 Module 规则；本 Ticket 实现自身只适用根 `AGENTS.md`。

## 验收条件

- [ ] T01 Bug、T02 需求调整、T03 新增需求和 T04 业务重构能路由到适用专业能力，而不是复制专业方法。
- [ ] 只要求人回答业务、契约、平台或治理所有者才能决定的问题；仓库可证事实由 Agent 自主调查。
- [ ] 只生成/更新 `01_analysis.md` 和按 Workflow 顺序适用的当前阶段草案，不修改代码、测试或后续产物。
- [ ] 需要 Module Interface、Seam、职责边界或测试接缝设计时，下一入口必须是 `codebase-design`，待设计人工确认后才进入 `to-spec`；无需设计时必须记录 N/A 证据后才能直接进入 `to-spec`。
- [ ] 建档未验收、类型不明、关键事实冲突、所有权不清或出现受限数据时稳定停止。
- [ ] Skill 显式调用、项目来源和哈希登记通过门禁。

## 验证命令

```text
./scripts/validate-t05-skills.sh work-item-discover
./scripts/validate-ai-governance.sh
git diff --check
```

## 停止条件

- Ticket 01 或其深度验证未获人工验收。
- 路由需要复制或修改第三方专业 Skill。
- Skill 无法保证只写发现阶段产物，或试图自行批准范围。
- 发现已批准 Spec/Interface 需要变化。
