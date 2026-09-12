# Ticket 01：T01 独立会话深度验证

## 结论

**PASS**。本文件由独立 Agent 会话 `/root/t05_t01_deep_validation` 生成。Agent 在隔离的临时 Git 副本中显式读取并执行项目级 `$work-item-start`，没有把静态检查冒充行为验证。

本次以 T01 Bug 为样本验证了一个成功路径和两个失败路径：

- 成功路径仅创建 `README.md`、`inputs/input-evidence.md` 和经检查的唯一截图附件，状态停在 `DISCOVERING`。
- 再次使用已占用的 Work Item ID 时停止，原文件哈希不变，没有覆盖。
- 缺少 T01 任务卡必要事实和附件时停止，目标目录没有创建。
- 所有场景均未生成 `01_analysis.md`、Spec、Design、Interface、Tickets、测试、代码或提交。

T01 任务卡的“课堂中手写 Prompt、不调用预置 Skill”规则未被改变。本次调用是已批准 T05 Spec AC-01/AC-02 对新 Skill 的隔离验证，不是 T01 课堂执行方式变更。

## 验证对象

| 字段 | 内容 |
| --- | --- |
| 主工作区 | `/Users/captainmao/Desktop/Personal_Project/培训讲解/training-wms` |
| 隔离副本 | `/tmp/t05-t01-work-item-start.8GM1D9/repo` |
| Agent 会话 | `/root/t05_t01_deep_validation` |
| 临时成功 ID | `T01-G91` |
| 临时缺事实 ID | `T01-G92` |
| 分支 | `T05-G01` |
| Git 提交 | `add14f73bf857604e5f738fe35111ab4ef1b3e05` |
| Skill | `.agents/skills/work-item-start/SKILL.md` |
| 验证日期 | 2026-09-11 CST |

隔离副本从主仓库克隆，随后只把当前待验收的 `work-item-start` Skill 包复制进隔离副本。除本证据文件外，主工作区未被本会话修改。

## 读取材料

本会话在执行前读取：

- 根 `AGENTS.md`
- `docs/work-items/T05-G01/tickets/01-work-item-start.md`
- `docs/work-items/T05-G01/spec.md`
- `.agents/skills/work-item-start/SKILL.md`
- `.agents/skills/init-work-item/SKILL.md`
- `docs/training/T01_取消出库未释放库存_学员任务卡.md`
- `docs/ai-governance/templates/work-item.md`
- `docs/ai-governance/templates/input-evidence.md`

## 场景一：T01 Bug 成功建档

### 显式输入

```text
/work-item-start T01-G91
任务类型：Bug 修复
本轮页面生成的单据号：SO-T01-8265CA2BF96B
本轮页面生成的 SKU：219770897155104
观察时间：2026-09-11 11:58:00 CST
页面事实：新增演示单准备总库存 10 并预占 6；取消后状态为 CANCELLED，可用量 4、预占量 6。
预期：取消后状态为 CANCELLED，可用量 10、预占量 0。
失败截图：唯一附件 browser-failure.png。
```

唯一附件复用仓库内已有且已经记录脱敏检查的 T01 课堂失败截图：

```text
docs/work-items/T01-G01/inputs/browser-failure.png
```

附件与输入中的单据号、SKU、数量和状态来自该截图的既有输入记录，未生成或改写图片。

### 执行命令

```text
rtk git clone --no-hardlinks /Users/captainmao/Desktop/Personal_Project/培训讲解/training-wms /tmp/t05-t01-work-item-start.8GM1D9/repo
rtk rsync -a /Users/captainmao/Desktop/Personal_Project/培训讲解/training-wms/.agents/skills/work-item-start/ /tmp/t05-t01-work-item-start.8GM1D9/repo/.agents/skills/work-item-start/
rtk git rev-parse --abbrev-ref HEAD
rtk git rev-parse HEAD
rtk find docs/work-items/T01-G91 -maxdepth 3 -type f
rtk shasum -a 256 docs/work-items/T01-G91/README.md docs/work-items/T01-G91/inputs/input-evidence.md docs/work-items/T01-G91/inputs/browser-failure.png
rtk git diff --check
```

### Git 结果

```text
T05-G01
add14f73bf857604e5f738fe35111ab4ef1b3e05
```

生成的 `README.md` 与 `input-evidence.md` 均记录了上述真实分支和提交。

### 生成文件清单

```text
docs/work-items/T01-G91/README.md
docs/work-items/T01-G91/inputs/input-evidence.md
docs/work-items/T01-G91/inputs/browser-failure.png
```

未出现：

```text
docs/work-items/T01-G91/01_analysis.md
docs/work-items/T01-G91/spec.md
docs/work-items/T01-G91/design.md
docs/work-items/T01-G91/interface.md
docs/work-items/T01-G91/tickets/
```

### 关键内容摘要

- Work Item ID：`T01-G91`
- 任务类型：`Bug 修复`
- 状态：`DISCOVERING`
- 当前分支：`T05-G01`
- 当前提交：`add14f73bf857604e5f738fe35111ab4ef1b3e05`
- 实际：`CANCELLED`、可用量 4、预占量 6
- 预期：`CANCELLED`、可用量 10、预占量 0
- 根因、修复方案、修改范围和具名负责人均保留为待确认
- 下一入口仅作为提示记录为 `/work-item-discover T01-G91`；当前会话没有执行该入口

### 文件哈希

```text
65196001f2c5e496b0e2c259eef741883da2b099a3a9f3005514ceac2a330fa9  docs/work-items/T01-G91/README.md
70f8cab67a33970a354ded17eaf880822208d7fb79e0d8308dc71fd250534fc5  docs/work-items/T01-G91/inputs/input-evidence.md
71000c73328afd9c72374a6d6df1ea8be472587820348f254c3fb13edd203474  docs/work-items/T01-G91/inputs/browser-failure.png
```

`git diff --check` 无输出，退出状态为 0。

### 停止结果

Skill 完成固定产物后停止。未进入调查、设计、实施、测试、提交、推送或 MR/PR 阶段。

## 场景二：目标目录已占用

### 显式输入

在场景一产物仍存在时，再次提交同一完整输入：

```text
/work-item-start T01-G91
任务类型：Bug 修复
单据号、SKU、观察时间、页面事实、预期和唯一附件与场景一相同。
```

### 行为结果

读取目标目录状态后命中停止条件：`docs/work-items/T01-G91/` 已存在。Agent 没有覆盖、合并或修补任何文件，也没有继续加载后续阶段。

### 无覆盖证据

停止后再次计算哈希，结果与场景一完全一致：

```text
65196001f2c5e496b0e2c259eef741883da2b099a3a9f3005514ceac2a330fa9  docs/work-items/T01-G91/README.md
70f8cab67a33970a354ded17eaf880822208d7fb79e0d8308dc71fd250534fc5  docs/work-items/T01-G91/inputs/input-evidence.md
71000c73328afd9c72374a6d6df1ea8be472587820348f254c3fb13edd203474  docs/work-items/T01-G91/inputs/browser-failure.png
```

文件清单仍只有场景一的三个文件，不存在 `01_analysis.md`。

## 场景三：缺少必要事实

### 显式输入

```text
/work-item-start T01-G92
任务类型：Bug 修复
```

### 行为结果

Agent 根据 T01 学员任务卡识别出缺少以下必要输入并停止：

- 页面真实出库单号
- 页面真实 SKU
- 观察时间
- 取消后的页面状态与库存数量事实
- 预期库存数量
- 唯一且可读取、已完成敏感信息检查的失败截图附件

没有用旧会话的 `T01-G91` 内容补齐，也没有使用 `N/A` 掩盖缺失信息。

### 零写入证据

```text
ls: docs/work-items/T01-G92: No such file or directory
```

因此没有 README、input evidence、附件、`01_analysis.md` 或任何后续产物。

## 输入隔离检查

- `T01-G91` 的单号、SKU、观察时间和附件只出现在自己的临时目录。
- `T01-G92` 缺事实时没有继承 `T01-G91` 的值，也没有创建目录。
- 本次验证未读取或修改 T02 临时会话目录；T02 由另一独立 Agent 会话验证。

## 主工作区影响

本会话只新增当前证据文件：

```text
docs/work-items/T05-G01/artifacts/ticket-01-t01-deep-validation.md
```

未修改 Skill、`skills-lock.json`、Templates、治理规范、任务卡、业务代码、测试或其他 Work Item，未创建提交。
