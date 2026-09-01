# Clean Code 检查引导

> 定位：本文件是导航入口，不是 Clean Code 规则正文。  
> 具体规则、检查步骤、规则 ID 和停止条件由项目安装的 Clean Code Skills 统一维护和执行。

## 1. 什么时候使用

出现以下任一情况时，Agent 必须调用仓库指定的 Clean Code Skill：

- 新增或修改生产代码。
- 修复 Bug 时改变业务逻辑、状态或副作用。
- 新增类、方法、公开接口、模块或依赖。
- 重构职责、抽取复用逻辑或引入设计模式。
- `/code-review` 需要执行 Standards 轴检查。

只修改纯文本且不影响程序行为时，可以记录不适用理由后跳过。

## 2. 调用哪个 Skill

默认按变更内容调用以下项目级 Skills：

| Skill | 检查范围 |
| --- | --- |
| `.agents/skills/clean-names/` | 类、方法、变量和副作用命名 |
| `.agents/skills/clean-functions/` | 参数、职责、抽象层级和死代码 |
| `.agents/skills/clean-general/` | 重复、意图、魔法值、耦合和职责 |
| `.agents/skills/clean-comments/` | 冗余、过期、误导和注释掉的代码 |
| `.agents/skills/clean-tests/` | 边界、缺陷邻域、速度和覆盖证据 |

这些文件来自 `ertugrul-dmr/clean-code-skills`。上游没有 Java 语言主 Skill，因此本项目只安装五个专项 Skill，不安装 Python/TypeScript 主 Skill和编排器。用于 Java 时，以其中语言无关的 Clean Code 规则为准；Python 示例和 Python 专属建议不是 Java 验收标准。来源提交和文件哈希见 `skills-lock.json`。

Agent 必须读取所有适用 Skill 的 `SKILL.md`。若 Skill 不存在、无法读取、版本不明或与任务所用 Standards 冲突，停止检查并要求人工处理，不得临时在本文件中补写一套规则。

## 3. 调用前准备什么

至少提供：

1. 已批准 Spec 的文件路径与版本。
2. Standards ID/版本及已批准例外。
3. 固定的代码差异起点和被审版本。
4. 本次允许修改与禁止修改的目录、模块、类或方法。
5. 相关调用方、接口、测试和真实测试结果。
6. 新增公开接口、模块、依赖或设计模式的说明。

输入不足时，Skill 必须返回缺失项并停止，不得用推测补齐。

## 4. Agent 必须交付什么

Clean Code Skills 的输出至少能回答：

- 使用了哪个 Skill 版本和规则版本。
- 检查了哪些文件、类、方法和调用方。
- 每条发现对应的规则 ID、代码位置和证据是什么。
- 问题是阻塞项、预警、已批准例外还是不适用。
- 修改建议会不会改变行为、契约、事务或所有权边界。
- 哪些项目必须由人裁决。

输出应进入本任务的 Standards 评审记录，并由 `/code-review` 汇总；不得另造一套格式。

## 5. 人如何验收

人工只需要核对五件事：

1. Skill 和规则版本是否明确。
2. 检查范围是否覆盖本次实际 Diff 及关键调用方。
3. 每条发现是否有代码位置和证据，而不是泛泛建议。
4. 阻塞项、预警、例外和不适用是否分开。
5. 所有预警和例外是否已有明确裁决，且没有用“拆得更小”或“模式更多”代替质量证明。

任何一项缺失，都不能把 Standards 轴标记为通过。

## 6. 与 Skill 的维护关系

- 本文件只在入口、输入、输出或人工验收方式变化时修改。
- 具体规则变化通过更新上游 Clean Code Skills 及 `skills-lock.json` 完成。
- 本文件只引用 Skill 名称和版本，不复制 Skill 内的规则表。
- 自动校验从 Skill 读取具体设置，从本文件读取路由和必需输入要求。
- 两者冲突时停止执行，由 Standards 所有者决定应更新引导还是 Skill。
