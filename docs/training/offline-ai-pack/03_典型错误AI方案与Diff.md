# 典型错误AI方案与Diff

> [预置负例]以下内容故意包含需求、所有权、事务、幂等、审计和测试问题。禁止直接应用到任何正确分支。

## 1. AI候选方案（负例）

> 需求很简单，可以直接实现。新增一个`/stocktakes/adjust`接口，接收SKU、仓库、库位和实盘数。Controller查询库存Repository后直接把可用量更新为实盘数，再返回成功。为了前端简单，失败统一返回HTTP 200和`success=false`。盘点由管理员操作，操作人可以固定写为`admin`。先实现，后续如果需要审批再增加状态。

## 2. 候选Diff（负例，仅供评审）

```diff
diff --git a/business-wms/src/main/java/com/acme/training/wms/web/StocktakeController.java b/business-wms/src/main/java/com/acme/training/wms/web/StocktakeController.java
new file mode 100644
--- /dev/null
+++ b/business-wms/src/main/java/com/acme/training/wms/web/StocktakeController.java
@@
+@RestController
+@RequestMapping("/api/wms/stocktakes")
+public class StocktakeController {
+    private final InventoryBalanceRepository inventoryRepository;
+    private final AuditPublisher auditPublisher;
+
+    @PostMapping("/adjust")
+    public ApiResponse<Void> adjust(@RequestBody StocktakeRequest request) {
+        InventoryBalance balance = inventoryRepository.find(
+                request.getSkuId(), request.getWarehouseId(), request.getLocationId());
+        balance.setAvailableQuantity(request.getCountedQuantity());
+        inventoryRepository.save(balance);
+        auditPublisher.publish(new AuditEvent("admin", "ADJUST", "STOCK"));
+        return ApiResponse.success(null);
+    }
+}
diff --git a/training-server/src/test/java/com/acme/training/StocktakeIntegrationTest.java b/training-server/src/test/java/com/acme/training/StocktakeIntegrationTest.java
new file mode 100644
--- /dev/null
+++ b/training-server/src/test/java/com/acme/training/StocktakeIntegrationTest.java
@@
+@Test
+void adjustsStocktake() throws Exception {
+    mockMvc.perform(post("/api/wms/stocktakes/adjust")
+            .contentType(APPLICATION_JSON)
+            .content("{\"skuId\":101,\"warehouseId\":1,\"locationId\":1,\"countedQuantity\":8}"))
+            .andExpect(status().isOk());
+}
```

## 3. 学员应找到的主要问题

| 类别 | 问题 | 影响 | 最小安全纠正 |
|---|---|---|---|
| 需求 | 未确认范围、实盘数含义、预占、审批和并发就直接实施 | 可能直接破坏已预占库存 | 先运行需求审核Skill，未知项未确认前`BLOCKED` |
| SPEC | 创建即直接调整库存，没有盘点单快照和审批 | 不符合T06确认规则 | 分离创建和审批；审批事务内校验快照 |
| SPEC | 把可用量设为物理实盘总量 | 忽略预占量，物理总量错误 | 可用量 = 实盘总量 - 预占量，实盘不得小于预占 |
| STANDARDS | Controller直接访问Repository | 绕过`InventoryOperations`、事务和领域不变式 | Controller只调用业务入口，库存修改统一进`InventoryOperations` |
| STANDARDS | 业务模块自定义平台错误行为 | 破坏UI和平台契约 | 复用`ApiResponse`和`GlobalExceptionHandler`，契约变更由平台/UI确认 |
| STANDARDS | 操作人硬编码为`admin` | 审计不可追溯 | 复用`CurrentOperator`和`AuditRecorder` |
| SPEC | 无幂等、快照过期、失败回滚 | 重复调整和部分提交风险 | 确认幂等语义，审批与库存调整保持同事务 |
| 测试 | 只断言HTTP 200 | 错误实现也可通过 | 断言创建不改库存、审批差异、回滚、幂等、流水和审计 |

## 4. 讲师用法

- 先只展示第1、2节，不展示问题表。
- 要求学员按`SPEC`和`STANDARDS`分类，每个问题必须有证据。
- 学员提交后再打开第3节对照。
- 收口时强调：问题不是“AI代码风格不好”，而是需求门、所有权和可执行证据被跳过。
