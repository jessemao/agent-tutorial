package com.acme.training;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
@Transactional
class T06CountRecordAndSubmitIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper json;

    @Test void userSavesPartialCountsAndPreservesNullVersusZero() throws Exception {
        JsonNode count=create("t02-create-save"); long id=count.path("id").asLong(); long version=count.path("version").asLong();
        mockMvc.perform(put("/api/wms/inventory-counts/{id}",id).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"save-partial\",\"expectedVersion\":"+version+",\"note\":\"首轮\",\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":0},{\"skuId\":304,\"locationId\":2,\"countedTotal\":null}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(version+1))
                .andExpect(jsonPath("$.data.note").value("首轮")).andExpect(jsonPath("$.data.lines[0].countedTotal").value(0))
                .andExpect(jsonPath("$.data.lines[1].countedTotal").doesNotExist());
        mockMvc.perform(get("/api/wms/inventory-counts/{id}",id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.lines",hasSize(2))).andExpect(jsonPath("$.data.lines[0].countedTotal").value(0));
    }

    @Test void staleSaveReturnsCurrentVersionWithoutChangingServerData() throws Exception {
        JsonNode count=create("t02-create-stale"); long id=count.path("id").asLong(); long version=count.path("version").asLong();
        save(id,version,"save-current",3).andExpect(status().isOk());
        save(id,version,"save-stale",8).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_COUNT_VERSION_CONFLICT")).andExpect(jsonPath("$.data.current.version").value(version+1));
        mockMvc.perform(get("/api/wms/inventory-counts/{id}",id)).andExpect(jsonPath("$.data.lines[0].countedTotal").value(3));
    }

    @Test void incompleteSubmitKeepsDraftThenCompleteSubmitMakesItReadOnly() throws Exception {
        JsonNode count=create("t02-create-submit"); long id=count.path("id").asLong(); long v=count.path("version").asLong();
        String partial=save(id,v,"save-before-submit",4).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(); long saved=json.readTree(partial).path("data").path("version").asLong();
        submit(id,saved,"submit-incomplete").andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_COUNT_INCOMPLETE")).andExpect(jsonPath("$.data.lineIndexes[0]").value(1));
        String complete=saveBoth(id,saved,"save-complete",4,0).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(); long ready=json.readTree(complete).path("data").path("version").asLong();
        submit(id,ready,"submit-complete").andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("SUBMITTED")).andExpect(jsonPath("$.data.submittedBy").value("counter-a")).andExpect(jsonPath("$.data.submittedAt").isNotEmpty()).andExpect(jsonPath("$.data.lines[0].submittedBookTotal").value(10)).andExpect(jsonPath("$.data.lines[1].submittedBookTotal").value(0));
        saveBoth(id,ready+1,"save-after-submit",1,1).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_COUNT_INVALID_STATE"));
    }

    @Test void saveAndSubmitActionsAreIdempotent() throws Exception {
        JsonNode count=create("t02-create-idem"); long id=count.path("id").asLong(); long v=count.path("version").asLong();
        MvcResult first=saveBoth(id,v,"same-save",2,1).andExpect(status().isOk()).andReturn();
        long saved=json.readTree(first.getResponse().getContentAsString()).path("data").path("version").asLong();
        saveBoth(id,v,"same-save",2,1).andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(saved));
        saveBoth(id,v,"same-save",9,1).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));
        submit(id,saved,"same-submit").andExpect(status().isOk());
        submit(id,saved,"same-submit").andExpect(status().isOk());
        submit(id,saved,"new-submit").andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_COUNT_INVALID_STATE"));
    }

    private JsonNode create(String key)throws Exception{String body=mockMvc.perform(post("/api/wms/inventory-counts").header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\""+key+"\",\"warehouseId\":1,\"lines\":[{\"skuId\":303,\"locationId\":1},{\"skuId\":304,\"locationId\":2}]}" )).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();return json.readTree(body).path("data");}
    private ResultActions save(long id,long v,String key,long total)throws Exception{return mockMvc.perform(put("/api/wms/inventory-counts/{id}",id).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\""+key+"\",\"expectedVersion\":"+v+",\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":"+total+"},{\"skuId\":304,\"locationId\":2,\"countedTotal\":null}]}"));}
    private ResultActions saveBoth(long id,long v,String key,long a,long b)throws Exception{return mockMvc.perform(put("/api/wms/inventory-counts/{id}",id).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\""+key+"\",\"expectedVersion\":"+v+",\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":"+a+"},{\"skuId\":304,\"locationId\":2,\"countedTotal\":"+b+"}]}"));}
    private ResultActions submit(long id,long v,String key)throws Exception{return mockMvc.perform(post("/api/wms/inventory-counts/{id}/transitions",id).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\""+key+"\",\"expectedVersion\":"+v+"}"));}
}
