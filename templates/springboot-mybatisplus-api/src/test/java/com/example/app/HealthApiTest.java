package com.example.app;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 探针与请求上下文切片测试，不依赖真实数据库。
 */
@WebMvcTest(HealthController.class)
class HealthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthReturnsUpWithoutDatabaseAccess() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"up\"}"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void readyReturnsUpWhenDatabaseReachable() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"up\"}"));
    }

    @Test
    void readyReturnsUnavailableWhenDatabaseFails() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("secret"));
        mockMvc.perform(get("/ready"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().json("{\"status\":\"unavailable\"}"));
    }

    @Test
    void missingPathReturnsErrorEnvelopeAndEchoesRequestId() throws Exception {
        mockMvc.perform(get("/missing").header("X-Request-ID", "trace-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(header().string("X-Request-ID", "trace-123"));
    }

    @Test
    void invalidRequestIdIsReplaced() throws Exception {
        mockMvc.perform(get("/health").header("X-Request-ID", "bad value"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID",
                        org.hamcrest.Matchers.matchesPattern("[0-9a-f]{32}")));
    }

}
