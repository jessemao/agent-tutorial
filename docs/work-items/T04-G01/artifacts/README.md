# T04-G01 候选验证原始证据

- 被验代码提交：`f49448795a114b32d242442f15c66db2da66c27e`
- 固定起点：`ae996fe51322f76dd4605de66c6a47cd84ee230b`
- 执行时间：2026-09-10 21:09—21:14 +0800
- 说明：执行期间 `HEAD` 为上述候选提交；未提交内容仅包含 `03_review.md` 和本次证据文件，`candidate-and-zero-reference-f494487.log` 证明生产与测试目录相对候选提交无差异。

| 文件 | 内容 | SHA-256 |
| --- | --- | --- |
| `candidate-and-zero-reference-f494487.log` | 候选 SHA、生产/测试目录零 Diff、旧 Seam Java 零引用 | `7b67d92c92af1343689e10e304c8a0481f7eae6371aeed2a766828a03c835ff5` |
| `t04-baseline-f494487.log` | T04 baseline：35 tests | `7070fd1e3b5f01e0cc0a714389fb37c586b1d82453b842ef0ffdff4bd70cc9ba` |
| `module-regression-f494487.log` | business-wms 受影响 Module 回归：3 tests | `0328a611c7d8aa7a1d8b049cd485c06eb3aaae8be85dec7e966d3dc487dc664b` |
| `frontend-build-f494487.log` | TypeScript 与 Vite 生产构建 | `8df0c8eff7728c34ebb759628b51e39ae214db6b3e667d8e1b20637f7cb5c6b1` |
| `mvn-clean-verify-f494487.log` | 项目全量 Maven 验证 | `74e51bb5d5433db6000f1b559721c5cbadd0dcdc4eb062e67e214ae2efd1495d` |
| `health-f494487.log` | 服务健康响应 | `8ffe8caecfa049409598f5b98f0112360a4d5ca73e8267e6843cc6cd8e9aac92` |
| `functional-test-http-ui-f494487.log` | 独立测试工程师的公开 HTTP 与课堂 UI 功能测试：12 个接口场景及 4 个页面观察 | `11b808ab0e042d46670519125611b91e6c2ada08cf96140565fefc09bfaa3c3d` |

原始日志中的 SQL ERROR 来自并发测试主动触发并断言的冲突场景；测试汇总和构建结果以每份日志末尾为准。全量验证的 2 个跳过项是未启用 `mysql-verification` Profile 时的条件跳过，不代表 MySQL 专项已执行。

独立功能测试于 2026-09-10 21:25 +0800 由用户本轮指定的 Codex 测试工程师执行，使用独立 SKU、公开 HTTP 和课堂 UI，不直接访问 Repository 或数据库。该证据与开发验证分开；MySQL 锁语义未作为独立测试执行，剩余风险见 `../functional-test.md`。
