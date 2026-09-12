# 05：建立人工决定的保真记录入口

**状态：** ready-for-agent  
**Blocked by：** 04：建立固定 Diff 的双轴评审入口  
**Spec：** `docs/work-items/T05-G01/spec.md` v1.1（APPROVED）
**Standards：** `add14f73bf857604e5f738fe35111ab4ef1b3e05` 中的 Roles、Decision、Deliverables 和项目 Skills 约束

## 交付行为

用户显式调用 `work-item-decision` 并提供有权人员已经作出的最终决定原文后，系统从当前会话、Git、Work Item 和企业项目管理事实系统解析交付事实，不改变原意地记录 `04_decision.md`，保持开发、Review、适用质量阶段、业务验收和最终放行可追溯，并永远停在有权限的人合并之前。

## 修改边界

- 允许修改：`.agents/skills/work-item-decision/`、`skills-lock.json`、本 Work Item 的开发验证证据。
- 禁止修改：所有前序 Skill、第三方 Skill、业务代码与测试、任务卡、治理规范、Templates 和门禁脚本。
- Module `AGENTS.md`：仅根 `AGENTS.md` 适用；本入口不得修改业务 Module。

## 验收条件

- [ ] 最小人工输入只包括 Work Item ID 与人的最终决定原文；决策人/角色/时间、Finding 裁决、被审提交、适用质量阶段、业务验收、MR/PR、目标分支、回滚和风险证据优先从事实系统解析，不强制用户重复输入。
- [ ] 非阻塞元数据不可得时明确记录“未配置 / 尚未创建 / N/A”及查询来源；证据冲突或 Finding 未裁决时仍须停止。
- [ ] 只按模板记录 `04_decision.md`，不得改变、补充或替代人的结论。
- [ ] 最终决定原文缺失、必要 Finding 未裁决或关键证据冲突时停止；仅有身份、时间、目标分支、MR/PR、回滚或风险等非阻塞元数据不可得时如实标注，不拒绝记录决定。
- [ ] 不从脚本 PASS 推导 ACCEPTED，不代签任何角色，不批准 MR/PR，不自动合并、强推、发布或打答案标签。
- [ ] `REWORK` 返回受影响阶段；`ACCEPTED` 只提示有权限的人审核与合并。
- [ ] Skill 显式调用、项目来源和哈希登记通过门禁。
- [ ] 五个入口完成后，临时 Work Item 的完整短命令链和最终 T05 门禁通过。

## 验证命令

```text
./scripts/validate-t05-skills.sh work-item-decision
./scripts/validate-ai-governance.sh
./scripts/classroom-verify.sh T05
git diff --check
```

## 停止条件

- Ticket 04 未验收，或没有有效 Review、独立测试、QA 和业务验收证据。
- 人工最终决定原话缺失，或对象版本/关键证据互相冲突；仅非阻塞元数据来源未配置不构成停止条件。
- 实现试图自动批准、合并、推送或创建平台事实。
- 完整短命令链发生越阶段、输入串线、版本漂移或历史业务答案泄漏。
