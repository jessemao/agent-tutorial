# 项目工程标准（Standards）

- Standards ID：`STD-WMS-0.7-02`
- 适用范围：`training-wms` 的新增和修改代码、测试、文档及评审。
- 状态：课程工作版，不代表甲方企业标准。
- 所有者：技术中台协调通用规则；事业部、UI、QA 分别确认业务、契约和验收规则。

本文件只保存项目级强制标准和质量入口，不复制 Clean Code Skills 的具体规则。Spec 定义本次交付边界；Standards 定义跨任务质量要求；`AGENTS.md` 规定 Agent 如何执行。

## 1. 规则级别

- `MUST`：违反且没有有效人工例外时阻止交付。
- `REVIEW`：必须提供证据并由人裁决，不自动判错。
- `N/A`：必须说明为什么不适用。

个人偏好不能被包装成 `MUST`。测试通过不能抵消 Standards 违反。

## 2. 项目级规则

| ID | 级别 | 标准 | 主要证据 |
| --- | --- | --- | --- |
| STD-ARCH-01 | MUST | Controller 不直接访问 Repository；库存变化必须经过 `InventoryOperations`；`training-server` 不承载业务规则 | 调用链、注入依赖、Module `AGENTS.md` |
| STD-ARCH-02 | MUST | 不引入循环依赖；平台不得反向依赖事业部；跨团队修改先取得相关所有者批准 | Maven 依赖、import、批准记录 |
| STD-OWN-01 | MUST | 只能修改任务批准范围和对应所有者负责的代码；局部 `AGENTS.md` 只能收紧根规则 | Impact、Module 规则、Diff |
| STD-API-01 | MUST | 未经批准不改变公开 HTTP 路径、字段、状态码、错误码或平台契约 | Spec、UI/API 契约、兼容测试 |
| STD-JAVA-01 | MUST | 语言特性和依赖必须符合 `pom.xml`；不得顺手升级 JDK、Spring Boot 或引入未批准依赖 | POM Diff、编译和依赖说明 |
| STD-ERR-01 | MUST | 不吞异常、不把失败伪装成成功、不泄漏内部堆栈；按公开错误契约返回 | 异常映射、错误测试、日志 |
| STD-DATA-01 | MUST | 单据状态、库存、流水和需同步落库的审计保持批准的事务语义；失败不得留下部分成功 | 事务入口、回滚测试、最终状态 |
| STD-DATA-02 | MUST | 重复请求区分安全重放与冲突；并发库存操作使用批准的锁顺序并保持库存不变式 | 幂等、并发、冲突及失败状态测试 |
| STD-SEC-01 | MUST | 不硬编码密钥，不记录令牌/敏感数据，不拼接不可信 SQL，不绕过权限 | 代码、配置、日志和权限测试 |
| STD-TEST-01 | MUST | 核心行为先建立能暴露问题或表达验收条件的测试；测试验证公开行为与最终状态 | 红灯证据、断言和修复结果 |
| STD-TEST-02 | MUST | 运行目标测试、相关 Module 回归和 `mvn clean verify`；记录真实命令、代码版本和结果 | Verification 与原始输出 |
| STD-DOC-01 | MUST | 契约或规则变化时同步文档；不覆盖已批准 Spec；冲突必须标记所有者和处理结论 | Spec 路径/版本、变更记录 |
| STD-DELIVERY-01 | MUST | Spec 与 Standards 分开评审；Review 与 Decision 分开记录；最终交付可追溯到代码和测试 | 两张矩阵、Decision、Delivery |

## 3. 质量入口

| 质量领域 | 必读入口 | 具体执行来源 |
| --- | --- | --- |
| 架构、依赖和所有权 | `docs/ai-governance/standards/architecture.md` | 根及受影响 Module 的 `AGENTS.md`、架构文档和代码 |
| Clean Code | `docs/ai-governance/standards/clean-code.md` | `clean-names`、`clean-functions`、`clean-general`、`clean-comments`、`clean-tests` |
| 测试 | `docs/ai-governance/standards/testing.md` | Spec 验收条件、Module 测试入口和真实执行结果 |
| 文档与追溯 | `docs/ai-governance/standards/documentation.md` | Templates、Spec 审批、Review、Decision 和 Delivery |

Clean Code 的规则 ID、判断步骤和示例只由下载的 Skills 维护。本文件不再维护另一套类/方法规模、复杂度、SOLID 或设计模式清单。上游规则不适合 Java 或与项目规则冲突时，记录为待人工裁决，不得静默套用。

## 4. 设计模式与复用

- 模式不是评分目标。引入 Strategy、Factory、Adapter、Facade、State 等模式时，必须说明当前问题、最简单替代、真实调用方、新增复杂度以及事务/异常影响。
- 新增 Module、公开 Interface 或依赖必须说明所有者、职责、当前调用方和现有能力不足的证据。
- 平台复用技术机制，事业部保留业务语义；相似代码不自动等于可上收平台。
- 不得通过机械拆 Helper、增加透传层或创建万能 Service 掩盖耦合。

这些要求进入 Spec 设计决定和 Standards 评审矩阵，不单独维护另一份教学规范。

## 5. 版本、例外与冲突

- 任务直接记录 Standards ID、文件路径/提交以及已批准例外，不建立额外中间包。
- 规则变化时更新 Standards ID；在途任务是否升级由人确认。
- 例外必须记录规则 ID、范围、理由、风险、替代措施、批准人、有效期和整改负责人。
- 密钥泄漏、权限绕过、库存/事务正确性破坏、伪造证据和静默篡改批准内容不得作为课堂例外。
- Standards、Spec、Module 规则或代码事实冲突时，停止受影响工作并由相关所有者裁决。

## 6. 评审与放行

使用 `docs/ai-governance/templates/code-review.md` 生成两张矩阵：

- `Spec 条目 → 代码符号 → 测试 → 真实结果 → 结论`
- `Standards 规则 ID / Clean Code Skill 规则 ID → 代码符号 → 检查证据 → 人工裁决 → 结论`

Review 提供事实和建议；正式接受、退回、例外或回滚写入 `decision.md`；最终交付写入 `delivery.md`。测试全绿、AI 输出 PASS 或文件完整都不能单独构成人工放行。
