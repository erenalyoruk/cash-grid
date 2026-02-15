package com.erenalyoruk.cashgrid.payment;

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
class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    private TestHelper helper;

    private static final String SOURCE_IBAN = "TR330006100519786457841326";
    private static final String TARGET_IBAN = "TR320010009999901234567890";

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mockMvc, objectMapper);
    }

    private void ensureAccountsExist() {
        Account source = accountRepository
                .findByIban(SOURCE_IBAN)
                .orElseGet(
                        () -> accountRepository.save(
                                Account.builder()
                                        .customerName("Source Corp")
                                        .iban(SOURCE_IBAN)
                                        .currency(Currency.TRY)
                                        .build()));
        source.setBalance(new BigDecimal("1000000.00"));
        accountRepository.save(source);

        Account target = accountRepository
                .findByIban(TARGET_IBAN)
                .orElseGet(
                        () -> accountRepository.save(
                                Account.builder()
                                        .customerName("Target Corp")
                                        .iban(TARGET_IBAN)
                                        .currency(Currency.TRY)
                                        .build()));
        target.setBalance(new BigDecimal("500000.00"));
        accountRepository.save(target);
    }

    private String getMakerToken() throws Exception {
        try {
            return helper.loginAndGetToken("paymaker");
        } catch (Throwable e) {
            return helper.registerAndGetToken("paymaker", "paymaker@test.com", "MAKER");
        }
    }

    private String getCheckerToken() throws Exception {
        try {
            return helper.loginAndGetToken("paychecker");
        } catch (Throwable e) {
            return helper.registerAndGetToken("paychecker", "paychecker@test.com", "CHECKER");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create payment — MAKER should succeed")
    void createPayment_success() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();

        mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format(
                                        "{\"idempotencyKey\":\"idem-001\","
                                                + "\"sourceIban\":\"%s\","
                                                + "\"targetIban\":\"%s\","
                                                + "\"amount\":5000.00,"
                                                + "\"currency\":\"TRY\","
                                                + "\"description\":\"Test payment\"}",
                                        SOURCE_IBAN, TARGET_IBAN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.createdByUsername").value("paymaker"));
    }

    @Test
    @Order(2)
    @DisplayName("Idempotency — same key should return existing payment")
    void createPayment_idempotent() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();

        String body = String.format(
                "{\"idempotencyKey\":\"idem-dup-001\","
                        + "\"sourceIban\":\"%s\","
                        + "\"targetIban\":\"%s\","
                        + "\"amount\":1000.00,"
                        + "\"currency\":\"TRY\","
                        + "\"description\":\"Idempotent test\"}",
                SOURCE_IBAN, TARGET_IBAN);

        // First call
        String response1 = mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Second call — same idempotency key
        String response2 = mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json1 = objectMapper.readTree(response1);
        JsonNode json2 = objectMapper.readTree(response2);

        Assertions.assertEquals(json1.get("id").asText(), json2.get("id").asText());
    }

    @Test
    @Order(3)
    @DisplayName("Same account — should return 422")
    void createPayment_sameAccount() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();

        mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format(
                                        "{\"idempotencyKey\":\"idem-same-001\","
                                                + "\"sourceIban\":\"%s\","
                                                + "\"targetIban\":\"%s\","
                                                + "\"amount\":100.00,"
                                                + "\"currency\":\"TRY\"}",
                                        SOURCE_IBAN, SOURCE_IBAN)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("SAME_ACCOUNT"));
    }

    @Test
    @Order(4)
    @DisplayName("Approve — CHECKER should approve and complete payment")
    void approvePayment_success() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String checkerToken = getCheckerToken();

        // Create payment
        String createResponse = mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format(
                                        "{\"idempotencyKey\":\"idem-approve-001\","
                                                + "\"sourceIban\":\"%s\","
                                                + "\"targetIban\":\"%s\","
                                                + "\"amount\":2000.00,"
                                                + "\"currency\":\"TRY\"}",
                                        SOURCE_IBAN, TARGET_IBAN)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(createResponse).get("id").asText();

        // Approve
        mockMvc.perform(
                post("/api/v1/payments/" + paymentId + "/approve")
                        .header("Authorization", "Bearer " + checkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.approvedByUsername").value("paychecker"));
    }

    @Test
    @Order(5)
    @DisplayName("Self approval — MAKER approving own payment should fail")
    void approvePayment_selfApproval() throws Exception {
        ensureAccountsExist();

        // Register a user with both MAKER token
        String makerToken;
        try {
            makerToken = helper.loginAndGetToken("selfapprove");
        } catch (Throwable e) {
            makerToken = helper.registerAndGetToken("selfapprove", "selfapprove@test.com", "MAKER");
        }

        mockMvc.perform(
                post("/api/v1/payments/00000000-0000-0000-0000-000000000001/approve")
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("Reject — CHECKER should reject payment")
    void rejectPayment_success() throws Exception {
        ensureAccountsExist();
        String makerToken = getMakerToken();
        String checkerToken = getCheckerToken();

        String createResponse = mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format(
                                        "{\"idempotencyKey\":\"idem-reject-001\","
                                                + "\"sourceIban\":\"%s\","
                                                + "\"targetIban\":\"%s\","
                                                + "\"amount\":3000.00,"
                                                + "\"currency\":\"TRY\"}",
                                        SOURCE_IBAN, TARGET_IBAN)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(
                post("/api/v1/payments/" + paymentId + "/reject")
                        .header("Authorization", "Bearer " + checkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Suspicious transaction\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Suspicious transaction"));
    }

    @Test
    @Order(7)
    @DisplayName("List pending — CHECKER should see pending payments excluding own")
    void listPending_success() throws Exception {
        String checkerToken = getCheckerToken();

        mockMvc.perform(
                get("/api/v1/payments/pending")
                        .header("Authorization", "Bearer " + checkerToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("CHECKER cannot create payment")
    void createPayment_checkerForbidden() throws Exception {
        String checkerToken = getCheckerToken();

        mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + checkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"idempotencyKey\":\"idem-forbidden\","
                                        + "\"sourceIban\":\"TR330006100519786457841326\","
                                        + "\"targetIban\":\"TR320010009999901234567890\","
                                        + "\"amount\":100.00}"))
                .andExpect(status().isForbidden());
    }
}
