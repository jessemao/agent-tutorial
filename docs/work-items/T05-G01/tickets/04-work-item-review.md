# 04：建立固定 Diff 的双轴评审入口

**状态：** ready-for-agent  
**Blocked by：** 03：建立受批准范围约束的执行入口  
**Spec：** `docs/work-items/T05-G01/spec.md` v1.1（APPROVED）
**Standards：** `add14f73bf857604e5f738fe35111ab4ef1b3e05` 中的 Review、Deliverables 和项目 Skills 约束

## 交付行为

用户显式调用 `work-item-review` 后，系统从 Work Item 唯一解析固定起点和候选提交，调用既有 `code-review` 分别检查 Spec 与 Standards，将完整 Findings 落盘到 `03_review.md`，不改代码并停在人工裁决点。

## 修改边界

- 允许修改：`.agents/skills/work-item-review/`、`skills-lock.json`、本 Work Item 的开发验证证据。
- 禁止修改：前序及后续 Skill、第三方 `code-review`、业务代码与测试、任务卡、治理规范、Templates 和门禁脚本。
- Module `AGENTS.md`：Skill 运行时读取被审 Diff 涉及的所有 Module 规则；本 Ticket 实现自身只适用根 `AGENTS.md`。

## 验收条件

- [ ] 唯一解析并报告起点、候选提交、实际 Diff、Spec/Design/Interface/Tickets、Verification 和适用 Standards。
- [ ] 调用既有 `code-review`，生成 Spec 与 Standards 两轴矩阵、Findings、问题成因和防复发动作。
- [ ] 必须落盘 `03_review.md`；仅在对话中返回结论不算完成。
- [ ] 比较点无效、候选不基于起点、文档/验证版本漂移或任一评审轴缺失时停止。
- [ ] 不边审边改、不关闭 Findings、不生成 Decision、不代签测试/QA、不推送或操作 MR/PR。
- [ ] Skill 显式调用、项目来源和哈希登记通过门禁。

## 验证命令

```text
./scripts/validate-t05-skills.sh work-item-review
./scripts/validate-ai-governance.sh
git diff --check
```

## 停止条件

- Ticket 03 未验收或没有固定候选提交。
- 无法唯一解析 Review 范围或证据版本不一致。
- 需要修改第三方 Review 方法或让 Review 自动修复/裁决。
- 无法证明评审过程没有修改被审代码。
