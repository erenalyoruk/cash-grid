package com.erenalyoruk.cashgrid.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import com.erenalyoruk.cashgrid.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AccountIntegrationTest extends BaseIntegrationTest {

    private TestHelper helper;

    @BeforeEach
    void setUp() throws Exception {
        helper = new TestHelper(mockMvc, objectMapper);
    }

    private String getAdminToken() throws Exception {
        try {
            return helper.loginAndGetToken("acctadmin");
        } catch (Throwable e) {
            return helper.registerAndGetToken("acctadmin", "acctadmin@test.com", "ADMIN");
        }
    }

    @Test
    @DisplayName("Create account — valid IBAN should succeed")
    void createAccount_success() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerName\":\"Test Corp\","
                                                + "\"iban\":\"TR330006100519786457841326\","
                                                + "\"currency\":\"TRY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Test Corp"))
                .andExpect(jsonPath("$.iban").value("TR330006100519786457841326"))
                .andExpect(jsonPath("$.currency").value("TRY"))
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    @DisplayName("Create account — invalid IBAN should return 400")
    void createAccount_invalidIban() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerName\":\"Bad IBAN Corp\","
                                                + "\"iban\":\"TR000000000000000000000000\","
                                                + "\"currency\":\"TRY\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Create account — duplicate IBAN should return 409")
    void createAccount_duplicateIban() throws Exception {
        String token = getAdminToken();

        String body =
                "{\"customerName\":\"Dup Corp\","
                        + "\"iban\":\"TR320010009999901234567890\","
                        + "\"currency\":\"TRY\"}";

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("IBAN_EXISTS"));
    }

    @Test
    @DisplayName("Create account — MAKER should be forbidden")
    void createAccount_makerForbidden() throws Exception {
        String makerToken;
        try {
            makerToken = helper.loginAndGetToken("acctmaker");
        } catch (Throwable e) {
            makerToken = helper.registerAndGetToken("acctmaker", "acctmaker@test.com", "MAKER");
        }

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .header("Authorization", "Bearer " + makerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerName\":\"No Access\","
                                                + "\"iban\":\"TR330006100519786457841326\","
                                                + "\"currency\":\"TRY\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("List accounts — should return paginated result")
    void listAccounts_success() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/accounts")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}
