package com.acme.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import java.util.*; import java.util.concurrent.*; import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest @AutoConfigureMockMvc
@EnabledIfSystemProperty(named="spring.profiles.active",matches="mysql-verification")
class T06CountScopeMysqlConcurrencyIntegrationTest {
    @Autowired private MockMvc mockMvc; @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @BeforeEach void clear(){
        jdbc.update("delete from wms_count_action_receipt");
        jdbc.update("delete from wms_count_approval_preview");
        jdbc.update("delete from wms_active_count_scope");
        jdbc.update("delete from wms_inventory_count_line");
        jdbc.update("delete from wms_inventory_count");
    }
    @Test void uniqueActiveScopeAllowsAtMostOneConcurrentDraft() throws Exception {
        CountDownLatch start=new CountDownLatch(1); ExecutorService pool=Executors.newFixedThreadPool(2);
        try{
            Future<MvcResult> a=pool.submit(()->createAfter(start,"mysql-count-a")); Future<MvcResult> b=pool.submit(()->createAfter(start,"mysql-count-b")); start.countDown();
            List<MvcResult> results=Arrays.asList(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
            List<Integer> statuses=results.stream().map(v->v.getResponse().getStatus()).sorted().collect(Collectors.toList());
            assertEquals(Arrays.asList(200,409),statuses);
            JsonNode winner=json.readTree(results.stream().filter(v->v.getResponse().getStatus()==200).findFirst().orElseThrow(AssertionError::new).getResponse().getContentAsString()).path("data");
            JsonNode conflict=json.readTree(results.stream().filter(v->v.getResponse().getStatus()==409).findFirst().orElseThrow(AssertionError::new).getResponse().getContentAsString());
            assertEquals("WMS_COUNT_SCOPE_CONFLICT",conflict.path("code").asText());
            assertEquals(winner.path("countNo").asText(),conflict.path("data").path("current").path("countNo").asText());
            assertEquals(0,conflict.path("data").path("lineIndexes").path(0).asInt(-1));
            assertEquals(1L,jdbc.queryForObject("select count(*) from wms_active_count_scope where sku_id=303 and warehouse_id=1 and location_id=1",Long.class));
        }finally{pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }
    private MvcResult createAfter(CountDownLatch start,String key)throws Exception{assertTrue(start.await(10,TimeUnit.SECONDS));return mockMvc.perform(post("/api/wms/inventory-counts").header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\""+key+"\",\"warehouseId\":1,\"lines\":[{\"skuId\":303,\"locationId\":1}]}" )).andReturn();}
}
