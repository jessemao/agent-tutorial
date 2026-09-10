# 04 Decision：T03-G01

## 决策对象

- 被审分支/提交：`T03-G01` / `4057c2ca9e0aa1b7781071531327b0e183bfde6f`
- 评审后修复提交：`f95105bf4e70e111b91f6e12803d5e79fb12fc2b`
- 最终交付记录提交前版本：`0d984e5e4352d3018d3382e0404c91780d3be15b`
- Analysis：`01_analysis.md`
- Verification：`02_verification.md`
- Review：`03_review.md`
- 决策人/角色/时间：当前用户代理；业务所有者、UI/契约所有者及交付批准人；2026-09-10 12:52:36 CST

## 人工决定原文

> 很好，全部验证通过 approved
>
> 可以操作合并了，现在假设完成了，写入 decision

## Finding 裁决

| Finding | 决定 | 理由 | 处理结果/例外 | 负责人与期限 |
| --- | --- | --- | --- | --- |
| 第四次复审 P0：测试直接修改数据库 | ACCEPT | “全部验证通过 approved” | 已在 `f95105b` 删除测试 Controller、`JdbcTemplate`、DDL 和直接 INSERT；原子回滚改从公开 HTTP 入口配合已批准的审计 Adapter 验证 | 已完成 |
| 第四次复审 P1：AC-10 验收映射不完整 | EXCEPTION | “全部验证通过 approved” | `02_verification.md` 已如实标记 `PARTIAL`：同键不同载荷已自动化验证；预先存在单边流水没有合规构造入口，不再用违规夹具宣称 PASS | 后续如需自动化覆盖，由业务/测试所有者先批准接缝 |

## MR/PR 与交付

- 源分支/提交：`T03-G01` / `0d984e5e4352d3018d3382e0404c91780d3be15b`
- 目标分支：`main`
- MR/PR 链接：未提供；当前仓库未配置 Git remote，无法记录托管平台链接。
- 自动检查：目标回归 8 tests、0 failures/errors/skips；最终 `mvn clean verify` 52 tests、0 failures/errors、2 skipped、`BUILD SUCCESS`；完整输出见 `evidence/full-verify-20260910.log`。
- UI 测试/QA 复测：Ticket 01—03 均由当前用户代理人工验收；最终结论原文为“全部验证通过 approved”。
- 批准合并人/时间：当前用户代理；2026-09-10 12:52:36 CST。
- 实际合并结果：按人工指令“现在假设完成了”记录为已完成；本文件不伪造未提供的 MR/PR 编号、链接或托管平台时间。
- 回滚点与方式：回滚到 `s2-t03-start` / `fec0c890fb96b255fcbbbddcdfc45ed2b8c4bbe2`；若已产生移库流水，先停用移库入口并保全余额、流水与审计证据，再由业务和数据所有者批准补偿，不直接删除业务记录。
- 剩余风险与后续动作：AC-10 的“提交前已存在单边流水”分支有生产防御代码，但没有符合当前 Standards 的自动化造数接缝；若要求补齐，必须先由业务/测试所有者批准接缝或调整验收条件。两个 Profile 守卫测试按设计跳过，MySQL 专项并发证据沿用 Ticket 03 已完成记录。

## 最终决定

ACCEPTED
