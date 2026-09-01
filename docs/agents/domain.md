# Domain Docs

AIHero工程Skills在探索代码、编写Spec、拆分Ticket和评审代码前，应按以下规则读取领域文档。

## 读取顺序

1. 根目录`CONTEXT.md`：项目统一领域术语；不存在时继续执行，不提前生成空模板。
2. `docs/adr/`：读取与当前改动相关的架构决策；目录或相关ADR不存在时继续执行。
3. `docs/architecture.md`：模块所有权、依赖方向和仓储不变式。
4. 当前任务卡、UI静态契约、相关测试和源码。

## 单上下文布局

```text
training-wms/
├── CONTEXT.md             # 有术语被真正确认时按需创建
├── docs/adr/              # 有重要且难逆决定时按需创建
├── docs/architecture.md
└── business-wms/
```

## 使用规则

- Spec、Ticket、测试名和评审结果使用`CONTEXT.md`中已经确认的术语。
- 新词可能意味着业务概念缺失，也可能只是AI发明了同义词；必须先判断，不能自动写入。
- 如果方案与已有ADR冲突，明确指出冲突并交由人工决定是否重开该决策。
- `CONTEXT.md`只保存稳定术语，不存放单次需求、实现计划或临时讨论。
- 实施中新发现的长期知识，应进入`CONTEXT.md`、ADR或架构文档，而不是回写已经作为快照发布的Spec。
