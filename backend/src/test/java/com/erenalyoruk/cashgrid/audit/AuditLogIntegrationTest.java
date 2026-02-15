package com.erenalyoruk.cashgrid.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import com.erenalyoruk.cashgrid.TestHelper;
import com.erenalyoruk.cashgrid.account.model.Account;
import com.erenalyoruk.cashgrid.account.model.Currency;
import com.erenalyoruk.cashgrid.account.repository.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditLogIntegrationTest extends BaseIntegrationTest {

    @Autowired private AccountRepository accountRepository;

    private TestHelper helper;

    private static final String SOURCE_IBAN = "TR130006200000000000000099";
    private static final String TARGET_IBAN = "TR190006200000000000000088";

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mockMvc, objectMapper);
    }

    private String getAdminToken() throws Exception {
        try {
            return helper.loginAndGetToken("auditadmin");
        } catch (Throwable e) {
            return helper.registerAndGetToken("auditadmin", "auditadmin@test.com", "ADMIN");
        }
    }

    private String getMakerToken() throws Exception {
        try {
            return helper.loginAndGetToken("auditmaker");
        } catch (Throwable e) {
            return helper.registerAndGetToken("auditmaker", "auditmaker@test.com", "MAKER");
        }
    }

    private String getCheckerToken() throws Exception {
        try {
            return helper.loginAndGetToken("auditchecker");
        } catch (Throwable e) {
            return helper.registerAndGetToken("auditchecker", "auditchecker@test.com", "CHECKER");
        }
    }

    private void ensureAccountsExist() {
        Account source =
                accountRepository
                        .findByIban(SOURCE_IBAN)
                        .orElseGet(
                                () ->
                                        accountRepository.save(
                                                Account.builder()
                                                        .customerName("Audit Source")
                                                        .iban(SOURCE_IBAN)
                                                        .currency(Currency.TRY)
                                                        .build()));
        source.setBalance(new BigDecimal("1000000.00"));
        accountRepository.save(source);

        Account target =
                accountRepository
                        .findByIban(TARGET_IBAN)
                        .orElseGet(
                                () ->
                                        accountRepository.save(
                                                Account.builder()
                                                        .customerName("Audit Target")
                                                        .iban(TARGET_IBAN)
                                                        .currency(Currency.TRY)
                                                        .build()));
        target.setBalance(new BigDecimal("500000.00"));
        accountRepository.save(target);
    }

    @Test
    @Order(1)
    @DisplayName("Audit log — payment creates audit entries viewable by ADMIN")
    void paymentCreatesAuditEntries() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String checkerToken = getCheckerToken();
        String adminToken = getAdminToken();

        // Create a payment
        String createResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header("Authorization", "Bearer " + makerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                String.format(
                                                        "{\"idempotencyKey\":\"idem-audit-001\","
                                                                + "\"sourceIban\":\"%s\","
                                                                + "\"targetIban\":\"%s\","
                                                                + "\"amount\":1000.00,"
                                                                + "\"currency\":\"TRY\","
                                                                + "\"description\":\"Audit test\"}",
                                                        SOURCE_IBAN, TARGET_IBAN)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String paymentId = objectMapper.readTree(createResponse).get("id").asText();

        // Approve the payment
        mockMvc.perform(
                        post("/api/v1/payments/" + paymentId + "/approve")
                                .header("Authorization", "Bearer " + checkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Query audit logs by entity
        mockMvc.perform(
                        get("/api/v1/audit-logs/entity/PAYMENT/" + paymentId)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    @Order(2)
    @DisplayName("Audit log — query by user")
    void queryByUser() throws Exception {
        String adminToken = getAdminToken();
        String makerToken = getMakerToken();

        // Get maker user ID
        String meResponse =
                mockMvc.perform(
                                get("/api/v1/auth/me")
                                        .header("Authorization", "Bearer " + makerToken))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String userId = objectMapper.readTree(meResponse).get("id").asText();

        // Query audit logs by user
        mockMvc.perform(
                        get("/api/v1/audit-logs/user/" + userId)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("Audit log — query by correlation ID")
    void queryByCorrelationId() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String adminToken = getAdminToken();

        String correlationId = "test-audit-corr-" + System.nanoTime();

        // Create payment with specific correlation ID
        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + makerToken)
                                .header("X-Correlation-Id", correlationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        String.format(
                                                "{\"idempotencyKey\":\"idem-audit-corr-%s\","
                                                        + "\"sourceIban\":\"%s\","
                                                        + "\"targetIban\":\"%s\","
                                                        + "\"amount\":500.00,"
                                                        + "\"currency\":\"TRY\"}",
                                                System.nanoTime(), SOURCE_IBAN, TARGET_IBAN)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Query by correlation ID
        mockMvc.perform(
                        get("/api/v1/audit-logs/correlation/" + correlationId)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(4)
    @DisplayName("Audit log — non-ADMIN should be forbidden")
    void nonAdmin_forbidden() throws Exception {
        String makerToken = getMakerToken();

        mockMvc.perform(
                        get("/api/v1/audit-logs/entity/PAYMENT/"
                                        + "00000000-0000-0000-0000-000000000001")
                                .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @DisplayName("Audit log — rejected payment creates audit entry")
    void rejectedPaymentAudit() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String checkerToken = getCheckerToken();
        String adminToken = getAdminToken();

        String createResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header("Authorization", "Bearer " + makerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                String.format(
                                                        "{\"idempotencyKey\":\"idem-audit-reject-%s\","
                                                            + "\"sourceIban\":\"%s\","
                                                            + "\"targetIban\":\"%s\","
                                                            + "\"amount\":750.00,"
                                                            + "\"currency\":\"TRY\"}",
                                                        System.nanoTime(),
                                                        SOURCE_IBAN,
                                                        TARGET_IBAN)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String paymentId = objectMapper.readTree(createResponse).get("id").asText();

        // Reject
        mockMvc.perform(
                        post("/api/v1/payments/" + paymentId + "/reject")
                                .header("Authorization", "Bearer " + checkerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Audit test rejection\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // Verify audit trail includes rejection
        String auditResponse =
                mockMvc.perform(
                                get("/api/v1/audit-logs/entity/PAYMENT/" + paymentId)
                                        .header("Authorization", "Bearer " + adminToken)
                                        .param("page", "0")
                                        .param("size", "20"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode content = objectMapper.readTree(auditResponse).get("content");
        boolean hasRejection = false;
        for (JsonNode entry : content) {
            if ("PAYMENT_REJECTED".equals(entry.get("action").asText())) {
                hasRejection = true;
                break;
            }
        }
        Assertions.assertTrue(hasRejection, "Audit log should contain PAYMENT_REJECTED entry");
    }

    @Test
    @Order(6)
    @DisplayName("Audit log — search endpoint with filters returns results")
    void searchWithFilters() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String adminToken = getAdminToken();

        String correlationId = "test-filter-corr-" + System.nanoTime();

        // Create and approve a payment to generate audit entries
        String createResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header("Authorization", "Bearer " + makerToken)
                                        .header("X-Correlation-Id", correlationId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                String.format(
                                                        "{\"idempotencyKey\":\"idem-filter-%s\",\"sourceIban\":\"%s\",\"targetIban\":\"%s\",\"amount\":10.00,\"currency\":\"TRY\"}",
                                                        System.nanoTime(),
                                                        SOURCE_IBAN,
                                                        TARGET_IBAN)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        objectMapper.readTree(createResponse).get("id").asText();

        // Query /api/v1/audit-logs with correlationId and entityType
        mockMvc.perform(
                        get("/api/v1/audit-logs")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("correlationId", correlationId)
                                .param("entityType", "PAYMENT")
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // Also test action filter (PAYMENT_CREATED)
        mockMvc.perform(
                        get("/api/v1/audit-logs")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("action", "PAYMENT_CREATED")
                                .param("correlationId", correlationId)
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
