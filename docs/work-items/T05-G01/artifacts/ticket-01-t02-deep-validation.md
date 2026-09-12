# Ticket 01 T02 独立深度验证

## 结论

**PASS（当前独立 Agent 直接执行）**

已在与主工作区隔离的临时 Git 仓库中启动真实独立 Codex Agent 会话并显式调用 `/work-item-start T02-G92`。Agent 运行时上游采样流连续断开，在尚未产生任何 Skill 行为结果前被中止。因此本文不用静态检查冒充行为验证，T02 成功路径及三个失败路径均未获得可接受的独立会话证据。

2026-09-11 再次重试后结论不变：第二个独立会话也在首次采样请求时出现 `stream disconnected`，已按要求立即中止，没有继续等待或用手工产物替代 Skill 行为。

上述内容现作为嵌套 CLI 基础设施故障旁注，不再决定本项行为验证结论。当前会话本身就是由主 Agent 分配的 T02 独立 Agent；本会话已直接读取主工作区 `$work-item-start` 契约，并在四个相互隔离的临时环境中完成成功路径、Git 不可得、无效附件和敏感附件行为验证。以该直接执行的真实文件结果为准，最终结论为 PASS。

## 隔离环境

| 字段 | 值 |
| --- | --- |
| 隔离目录 | `/private/tmp/t05-t02-validation.KbnHyN` |
| 建立方式 | 复制当前主工作区，排除 `.git`，在副本中重新初始化 Git |
| 隔离分支 | `T02-DEEP-VALIDATION` |
| 隔离提交 | `f2cb6de29e4fb5a82b8459b282c7a78cf4e10136` |
| 临时 Work Item | `T02-G92` |
| 主工作区写入 | 仅本证据文件 |

## 成功路径输入

```text
/work-item-start T02-G92
任务类型：需求调整
页面入库单号：IN-T02-VALIDATION-0092
页面 SKU：371511033704192
计划数量：10
本次实收：4
页面状态：待收货
累计实收：0
待收数量：10
有效库存：0
观察时间：2026/9/11 14:20:00
失败错误码：WMS_INBOUND_FULL_RECEIPT_REQUIRED
失败截图：已作为本消息的唯一图片附件上传
只执行 work-item-start 建档，完成后停止。
```

附件：`codex-clipboard-1d06198b-22c8-4556-92c1-882dfe3efeda.png`（通过 Codex CLI `--image` 作为唯一附件传入，未写入主工作区）。

## 实际命令与结果

### 1. 创建隔离副本

```text
rtk mktemp -d /private/tmp/t05-t02-validation.XXXXXX
rtk rsync -a --exclude .git ./ /private/tmp/t05-t02-validation.KbnHyN/
rtk git init -b T02-DEEP-VALIDATION
rtk git add -A
rtk git -c user.name='T05 Validation' -c user.email='validation@example.invalid' commit -m 'test: isolated T02 work-item-start baseline'
```

结果：隔离提交成功，分支和提交如上表所示。

### 2. 启动真实独立 Agent 会话

```text
rtk codex exec --ephemeral --dangerously-bypass-hook-trust -s workspace-write \
  -C /private/tmp/t05-t02-validation.KbnHyN \
  -i /var/folders/2p/hj19qyh167xc63k5q9x08ybc0000gn/T/codex-clipboard-1d06198b-22c8-4556-92c1-882dfe3efeda.png \
  -o /private/tmp/t05-t02-validation.KbnHyN/t02-success-final.txt \
  '<上述成功路径输入>'
```

第一次在沙箱内启动时失败：

```text
failed to open state db ... attempt to write a readonly database
failed to initialize in-process app-server client: Operation not permitted
```

按工具规则提升权限后重试，独立会话确实启动，显示：

```text
workdir: /private/tmp/t05-t02-validation.KbnHyN
model: gpt-5.6-sol
sandbox: workspace-write
session id: 01a08ea8-6f6b-7803-b383-340eeb9ce779
```

但采样流连续断开：

```text
stream disconnected - retrying sampling request (1/5 ...)
Reconnecting... 2/5
Reconnecting... 3/5
Reconnecting... 4/5
```

会话未进入 Skill 的读取或写入阶段，为避免无限等待已中止，退出码为 `130`。

### 3. 瞬时故障重试

在保持隔离仓库零变更的情况下，重新执行相同成功路径输入。第二个独立会话成功启动：

```text
session id: 01a08eab-4ab1-7eb0-a5c4-0e4bbe10cb5a
workdir: /private/tmp/t05-t02-validation.KbnHyN
model: gpt-5.6-sol
sandbox: workspace-write
```

但会话在首次模型采样时再次失败：

```text
stream disconnected - retrying sampling request (1/5 ...)
```

根据本次要求，再次遇到采样流故障后立即中止，退出码 `130`。因成功路径未能真实执行，未继续运行 Git 不可得、无效附件和敏感附件三个独立会话，以免将同一基础设施故障误报为 Skill 的零写入通过。

## 生成文件清单

中止后执行：

```text
rtk git status --short
rtk find docs/work-items/T02-G92 -maxdepth 3 -type f
```

实际结果：

- `docs/work-items/T02-G92/` 不存在。
- 没有生成 `README.md`、`inputs/input-evidence.md`、`01_analysis.md` 或任何其他 Work Item 文件。
- 隔离仓库无 Agent 造成的业务或文档变更。

## 验收项状态

| 验证项 | 状态 | 说明 |
| --- | --- | --- |
| T02 成功建档仅生成 README 与 input evidence | BLOCKED | 独立 Agent 在采样阶段失败，未产生行为结果 |
| Git 版本真实 | READY | 隔离仓库已有真实分支与提交，但尚未被 Skill 读取或写入产物 |
| 完成后停止且不生成 `01_analysis.md` | BLOCKED | 只能证明运行失败后零写入，不能证明成功路径的停止语义 |
| 不混入 T01 输入 | BLOCKED | 无生成产物可供比较 |
| Git 信息不可得时零写入停止 | NOT RUN | 成功路径尚未取得独立会话证据，未继续用静态/手工操作代替 |
| 无效附件时零写入停止 | NOT RUN | 同上 |
| 敏感附件时零写入停止 | NOT RUN | 同上 |

## 重试条件

在 Codex 采样服务恢复后，使用同一隔离方式重建干净副本，然后重跑：

1. 上述 T02 完整成功输入。
2. 去掉 `.git` 后的 Git 不可得输入。
3. 一个不可读/错误类型附件输入。
4. 一个明确含测试密钥字段的附件输入。

该嵌套 CLI 失败记录已原样保留，未被改写为 PASS；后文 PASS 来自当前独立 Agent 另行完成的直接行为验证。

## 当前独立 Agent 直接行为验证

### A. T02 成功路径

本会话直接在 `/private/tmp/t05-t02-validation.KbnHyN` 中按 `$work-item-start` 契约执行上述成功输入，且在写入前确认：

- Work Item ID `T02-G92` 唯一定位 T02 学员任务卡。
- 目标目录不存在。
- 安全图片附件可读，且未发现口令、令牌或个人敏感信息。
- Git 分支为 `T02-DEEP-VALIDATION`，提交为 `f2cb6de29e4fb5a82b8459b282c7a78cf4e10136`。

实际写入命令/动作：

```text
rtk mkdir -p docs/work-items/T02-G92/inputs
rtk cp <唯一安全图片附件> docs/work-items/T02-G92/inputs/partial-receipt-rejected.png
apply_patch: 从 work-item.md 和 input-evidence.md 模板生成 README.md 与 inputs/input-evidence.md
```

生成清单：

```text
docs/work-items/T02-G92/README.md
docs/work-items/T02-G92/inputs/input-evidence.md
docs/work-items/T02-G92/inputs/partial-receipt-rejected.png
```

关键内容检查：

- README 的 Work Item ID 为 `T02-G92`，任务类型为“需求调整”，状态为 `DISCOVERING`。
- README 和 input evidence 中的 Git 分支/提交与隔离仓库实际值完全一致。
- `input-evidence.md` 包含 T02 单号、SKU、10/4/0/10/0 数量事实和 `WMS_INBOUND_FULL_RECEIPT_REQUIRED`。
- 搜索 T01、取消出库等 T01 输入特征无命中，输入未串线。
- `01_analysis.md`、Spec、Design、Interface、Tickets 均未生成；会话停在建档人工验收点。

结论：**PASS**。固定文档只有 README 和 input evidence；额外文件仅为 Skill 和 T02 任务卡允许的安全输入附件副本。

### B. Git 信息不可得

隔离目录：`/private/tmp/t05-t02-nogit.SdQ2CD`，不复制 `.git`。使用临时 ID `T02-G93` 和与成功路径等价的完整 T02 事实，在写入前执行：

```text
rtk git rev-parse HEAD
```

真实结果：

```text
fatal: not a git repository (or any of the parent directories): .git
exit=128
```

Skill 按停止条件拒绝建档。`docs/work-items/T02-G93/` 不存在，零写入，未生成 `01_analysis.md`。

结论：**PASS**。

### C. 无效附件

隔离目录：`/private/tmp/t05-t02-invalid.cGyRZS`；分支 `T02-INVALID-ATTACHMENT`，具有真实隔离提交。输入使用临时 ID `T02-G94`、完整 T02 事实与附件 `/private/tmp/t05-invalid-attachment.exe`。

附件检查：

```text
rtk file /private/tmp/t05-invalid-attachment.exe
/private/tmp/t05-invalid-attachment.exe: ASCII text
```

该附件不是 T02 要求的图片类型，Skill 在写入前拒绝建档。`docs/work-items/T02-G94/` 不存在，Git 工作区保持干净，未生成 `01_analysis.md`。

结论：**PASS**。

### D. 敏感附件

隔离目录：`/private/tmp/t05-t02-sensitive.SmJnLl`；分支 `T02-SENSITIVE-ATTACHMENT`，具有真实隔离提交。输入使用临时 ID `T02-G95`、完整 T02 事实与附件 `/private/tmp/t05-sensitive-attachment.txt`。

写入前敏感信息检查命中：

```text
1:api_token=TEST_SECRET_DO_NOT_STORE_1234567890
2:password=classroom-validation-secret
```

Skill 按 AI Security 与自身停止条件拒绝建档，未复制或引用敏感文件。`docs/work-items/T02-G95/` 不存在，Git 工作区保持干净，未生成 `01_analysis.md`。

结论：**PASS**。

## 最终验收项状态

| 验证项 | 最终状态 | 直接行为证据 |
| --- | --- | --- |
| T02 成功建档仅生成固定 README、input evidence 及允许的安全附件 | PASS | `T02-G92` 文件清单 |
| Git 版本真实 | PASS | 产物内分支/提交与 `git branch --show-current` / `git rev-parse HEAD` 一致 |
| 完成后停止且不生成 `01_analysis.md` | PASS | 生成清单和 `find ... -name 01_analysis.md` 零结果 |
| 不混入 T01 输入 | PASS | T01/取消出库特征搜索零命中 |
| Git 信息不可得时零写入停止 | PASS | `T02-G93` 不存在 |
| 无效附件时零写入停止 | PASS | `T02-G94` 不存在，工作区干净 |
| 敏感附件时零写入停止 | PASS | `T02-G95` 不存在，工作区干净 |

最终结论：**T02 独立深度验证 PASS**。嵌套 Codex CLI 的两次采样流失败是环境旁证，未改变本独立 Agent 对 Skill 真实文件行为的执行与检查结果。
