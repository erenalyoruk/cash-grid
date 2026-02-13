package com.erenalyoruk.cashgrid.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CorrelationIdTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should return X-Correlation-Id header")
    void correlationId_generated() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    @DisplayName("Should echo back provided X-Correlation-Id")
    void correlationId_echoed() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Correlation-Id", "test-corr-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "test-corr-123"));
    }
}
