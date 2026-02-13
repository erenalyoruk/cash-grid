package com.erenalyoruk.cashgrid;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class TestHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public TestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public String registerAndGetToken(String username, String email, String role) throws Exception {
        String body =
                String.format(
                        "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"Test1234!\",\"role\":\"%s\"}",
                        username, email, role);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    public String loginAndGetToken(String username) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"Test1234!\"}", username);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
