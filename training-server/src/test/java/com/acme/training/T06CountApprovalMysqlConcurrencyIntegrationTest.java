package com.acme.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "mysql-verification")
class T06CountApprovalMysqlConcurrencyIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;

    @Test
    void concurrentIdenticalCreateSafelyReplaysOneDraft() throws Exception {
        String key="same-create-"+UUID.randomUUID();
        String body="{\"idempotencyKey\":\""+key+"\",\"warehouseId\":1,\"lines\":[{\"skuId\":303,\"locationId\":2}]}";
        CountDownLatch start=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<MvcResult> first=pool.submit(()->createAfter(start,body));
            Future<MvcResult> second=pool.submit(()->createAfter(start,body));
            start.countDown();
            MvcResult left=first.get(15,TimeUnit.SECONDS),right=second.get(15,TimeUnit.SECONDS);
            assertEquals(200,left.getResponse().getStatus(),left.getResponse().getContentAsString());
            assertEquals(200,right.getResponse().getStatus(),right.getResponse().getContentAsString());
            JsonNode leftData=json.readTree(left.getResponse().getContentAsString()).path("data");
            JsonNode rightData=json.readTree(right.getResponse().getContentAsString()).path("data");
            assertEquals(leftData,rightData);
            assertEquals("DRAFT",leftData.path("status").asText());
            assertEquals(1,leftData.path("version").asLong());
            JsonNode page=response(get("/api/wms/inventory-counts?countNo="+leftData.path("countNo").asText()));
            assertEquals(1,page.path("total").asLong());
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));
        }
    }

    @Test
    void concurrentReceiptAndApprovalNeverLoseInventory() throws Exception {
        String run = UUID.randomUUID().toString();
        long initial = balanceTotal(304,2);
        JsonNode submitted = submitted(run);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<MvcResult> approval = pool.submit(() -> approvalAfter(start, submitted, run));
            Future<MvcResult> receipt = pool.submit(() -> receiptAfter(start, run));
            start.countDown();
            int approvalStatus = approval.get(15, TimeUnit.SECONDS).getResponse().getStatus();
            assertEquals(200, receipt.get(15, TimeUnit.SECONDS).getResponse().getStatus());

            JsonNode count = response(get("/api/wms/inventory-counts/{id}", submitted.path("id").asLong()));
            JsonNode balance = response(get("/api/wms/inventory/balance?skuId=304&warehouseId=1&locationId=2"));
            if (approvalStatus == 200) {
                assertEquals("APPROVED", count.path("status").asText());
                assertEquals(3, balance.path("availableQuantity").asLong());
            } else {
                assertEquals(409, approvalStatus);
                assertEquals("SUBMITTED", count.path("status").asText());
                assertEquals(initial+1, balance.path("availableQuantity").asLong());
            }
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void concurrentShipmentAndApprovalSerializeWithoutPartialAdjustment() throws Exception {
        String run=UUID.randomUUID().toString();
        JsonNode shipment=response(post("/api/wms/shipments").header("X-Operator","shipper-c").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderNo\":\"SO-"+run+"\",\"skuId\":303,\"warehouseId\":1,\"locationId\":1,\"quantity\":1}"));
        response(post("/api/wms/shipments/{id}/reserve",shipment.path("id").asLong()).header("X-Operator","shipper-c").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"reserve-"+run+"\"}"));
        long total=balanceTotal(303,1);
        JsonNode submitted=submittedAt(run,303,1,total);
        MvcResult[] results=race(submitted,run,post("/api/wms/shipments/{id}/ship",shipment.path("id").asLong()).header("X-Operator","shipper-c")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"ship-"+run+"\"}"));
        assertEquals(200,results[1].getResponse().getStatus());
        assertEquals(total-1,balanceTotal(303,1));
        assertApprovedOrSubmitted(results[0],submitted);
    }

    @Test
    void concurrentTransferAndApprovalSerializeBothLocations() throws Exception {
        String run=UUID.randomUUID().toString();
        response(post("/api/wms/inventory/receive").header("X-Operator","receiver-c").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"seed-"+run+"\",\"referenceNo\":\"RC-"+run+"\",\"skuId\":304,\"warehouseId\":1,\"locationId\":1,\"quantity\":5}"));
        long sourceTotal=balanceTotal(304,1),targetTotal=balanceTotal(304,2);
        JsonNode submitted=submittedAt(run,304,1,sourceTotal);
        MvcResult[] results=race(submitted,run,post("/api/wms/inventory/transfer").header("X-Operator","mover-c").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"transfer-"+run+"\",\"transferNo\":\"TR-"+run+"\",\"skuId\":304,\"warehouseId\":1,\"sourceLocationId\":1,\"targetLocationId\":2,\"quantity\":1}"));
        assertEquals(200,results[1].getResponse().getStatus());
        assertEquals(sourceTotal-1,balanceTotal(304,1)); assertEquals(targetTotal+1,balanceTotal(304,2));
        assertApprovedOrSubmitted(results[0],submitted);
    }

    private MvcResult[] race(JsonNode submitted,String run,org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder inventoryAction) throws Exception {
        CountDownLatch start=new CountDownLatch(1); ExecutorService pool=Executors.newFixedThreadPool(2);
        try{
            Future<MvcResult> approval=pool.submit(()->approvalAfter(start,submitted,run));
            Future<MvcResult> action=pool.submit(()->{assertTrue(start.await(10,TimeUnit.SECONDS));return mockMvc.perform(inventoryAction).andReturn();});
            start.countDown(); return new MvcResult[]{approval.get(15,TimeUnit.SECONDS),action.get(15,TimeUnit.SECONDS)};
        }finally{pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }

    private void assertApprovedOrSubmitted(MvcResult approval,JsonNode submitted) throws Exception {
        int status=approval.getResponse().getStatus(); assertTrue(status==200||status==409,approval.getResponse().getContentAsString());
        JsonNode count=response(get("/api/wms/inventory-counts/{id}",submitted.path("id").asLong()));
        assertEquals(status==200?"APPROVED":"SUBMITTED",count.path("status").asText());
    }

    private long balanceTotal(long skuId,long locationId) throws Exception {
        JsonNode b=response(get("/api/wms/inventory/balance?skuId="+skuId+"&warehouseId=1&locationId="+locationId));
        return b.path("availableQuantity").asLong()+b.path("reservedQuantity").asLong();
    }

    private JsonNode submittedAt(String run,long skuId,long locationId,long countedTotal) throws Exception {
        JsonNode created=response(post("/api/wms/inventory-counts").header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"create-"+run+"\",\"warehouseId\":1,\"lines\":[{\"skuId\":"+skuId+",\"locationId\":"+locationId+"}]}"));
        JsonNode saved=response(put("/api/wms/inventory-counts/{id}",created.path("id").asLong()).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"save-"+run+"\",\"expectedVersion\":"+created.path("version").asLong()+",\"lines\":[{\"skuId\":"+skuId+",\"locationId\":"+locationId+",\"countedTotal\":"+countedTotal+"}]}"));
        return response(post("/api/wms/inventory-counts/{id}/transitions",created.path("id").asLong()).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\"submit-"+run+"\",\"expectedVersion\":"+saved.path("version").asLong()+"}"));
    }

    private JsonNode submitted(String run) throws Exception {
        JsonNode created = response(post("/api/wms/inventory-counts").header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"create-" + run + "\",\"warehouseId\":1,\"lines\":[{\"skuId\":304,\"locationId\":2}]}"));
        JsonNode saved = response(put("/api/wms/inventory-counts/{id}", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"save-" + run + "\",\"expectedVersion\":" + created.path("version").asLong() + ",\"lines\":[{\"skuId\":304,\"locationId\":2,\"countedTotal\":2}]}"));
        return response(post("/api/wms/inventory-counts/{id}/transitions", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\"submit-" + run + "\",\"expectedVersion\":" + saved.path("version").asLong() + "}"));
    }

    private MvcResult approvalAfter(CountDownLatch start, JsonNode submitted, String run) throws Exception {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        return mockMvc.perform(post("/api/wms/inventory-counts/{id}/approval", submitted.path("id").asLong())
                .header("X-Operator", "reviewer-b").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"approve-" + run + "\",\"expectedVersion\":" + submitted.path("version").asLong() + "}")).andReturn();
    }

    private MvcResult receiptAfter(CountDownLatch start, String run) throws Exception {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        return mockMvc.perform(post("/api/wms/inventory/receive").header("X-Operator", "receiver-c")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"receive-" + run + "\",\"referenceNo\":\"RC-" + run + "\",\"skuId\":304,\"warehouseId\":1,\"locationId\":2,\"quantity\":1}")).andReturn();
    }

    private MvcResult createAfter(CountDownLatch start,String body) throws Exception {
        assertTrue(start.await(10,TimeUnit.SECONDS));
        return mockMvc.perform(post("/api/wms/inventory-counts").header("X-Operator","counter-a")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    private JsonNode response(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        assertEquals(200, result.getResponse().getStatus(), result.getResponse().getContentAsString());
        return json.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
