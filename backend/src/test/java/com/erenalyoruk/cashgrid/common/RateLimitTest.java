package com.erenalyoruk.cashgrid.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import com.erenalyoruk.cashgrid.common.config.RateLimitProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
            "app.rate-limit.auth-capacity=3",
            "app.rate-limit.auth-refill-tokens=3",
            "app.rate-limit.auth-refill-seconds=60"
        })
class RateLimitTest extends BaseIntegrationTest {

    @Autowired private RateLimitProperties rateLimitProperties;

    @Test
    @DisplayName("Should return X-Rate-Limit-Remaining header")
    void rateLimitHeader_present() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"ratelimit1\",\"email\":\"ratelimit1@test.com\","
                                            + "\"password\":\"Test1234!\",\"role\":\"MAKER\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Rate-Limit-Remaining"));
    }

    @Test
    @DisplayName("Should return 429 when auth rate limit exceeded")
    void rateLimitExceeded_returns429() throws Exception {
        int capacity = rateLimitProperties.getAuthCapacity();

        // Exhaust the bucket — use a unique IP via X-Forwarded-For so we get a fresh
        // bucket
        String uniqueIp = "10.99.99." + System.nanoTime() % 255;

        for (int i = 0; i < capacity; i++) {
            mockMvc.perform(
                    post("/api/v1/auth/login")
                            .header("X-Forwarded-For", uniqueIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"nonexistent\",\"password\":\"WrongPass!\"}"));
        }

        // Next request should be rate-limited
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .header("X-Forwarded-For", uniqueIp)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"nonexistent\",\"password\":\"WrongPass!\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Actuator endpoints should not be rate limited")
    void actuator_notRateLimited() throws Exception {
        // Actuator should never be filtered — call it many times
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                    "/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}
