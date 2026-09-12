# 03：建立受批准范围约束的执行入口

**状态：** ready-for-agent  
**Blocked by：** 02：建立按任务类型路由的发现入口  
**Spec：** `docs/work-items/T05-G01/spec.md` v1.1（APPROVED）
**Standards：** `add14f73bf857604e5f738fe35111ab4ef1b3e05` 中的 Workflow、Deliverables、TDD 和项目 Skills 约束

## 交付行为

用户显式调用 `work-item-execute` 并提供已批准 Work Item 或当前 Ticket 后，系统核对实施依据、对象版本和修改边界，调用既有 `implement`/`tdd` 能力只完成当前切片，记录开发验证，并在 Review 前停止。

## 修改边界

- 允许修改：`.agents/skills/work-item-execute/`、`skills-lock.json`、本 Work Item 的开发验证证据。
- 禁止修改：前序及后续 Skill、第三方 Skill、业务代码与测试、任务卡、治理规范、Templates 和门禁脚本。
- Module `AGENTS.md`：Skill 运行时必须读取当前 Ticket 涉及 Module 的规则；本 Ticket 实现自身只适用根 `AGENTS.md`。

## 验收条件

- [ ] 只有批准原文、对象版本、当前 Ticket 和可改/禁改范围完整时才允许进入实施。
- [ ] 只调用既有专业实施/TDD 能力，不复制 Red–Green–Refactor 或代码 Standards 内容。
- [ ] 只写当前 Ticket 批准范围与 `02_verification.md`/必要 artifacts；不生成 Review、独立测试、QA 或 Decision。
- [ ] 未批准、Ticket 被阻塞、测试失败、修改越界、Spec/契约/依赖/事务需要变化或版本漂移时停止。
- [ ] 不自动提交、推送、创建 MR/PR、Review 或进入下一 Ticket。
- [ ] Skill 显式调用、项目来源和哈希登记通过门禁。

## 验证命令

```text
./scripts/validate-t05-skills.sh work-item-execute
./scripts/validate-ai-governance.sh
git diff --check
```

## 停止条件

- Ticket 02 未验收。
- 无法区分开发 TDD、独立功能测试和 QA 审核。
- 实施权限无法限制到当前 Ticket，或需要修改已批准契约。
- 任一测试失败、跳过无理由或结果不能对应当前版本。
