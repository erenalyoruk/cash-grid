package com.erenalyoruk.cashgrid.limit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import com.erenalyoruk.cashgrid.TestHelper;
import com.erenalyoruk.cashgrid.account.model.Account;
import com.erenalyoruk.cashgrid.account.model.Currency;
import com.erenalyoruk.cashgrid.account.repository.AccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LimitIntegrationTest extends BaseIntegrationTest {

    @Autowired private AccountRepository accountRepository;

    private TestHelper helper;

    private static final String SOURCE_IBAN = "TR400006200000000000000001";
    private static final String TARGET_IBAN = "TR130006200000000000000002";

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mockMvc, objectMapper);
    }

    private String getAdminToken() throws Exception {
        try {
            return helper.loginAndGetToken("limitadmin");
        } catch (Throwable e) {
            return helper.registerAndGetToken("limitadmin", "limitadmin@test.com", "ADMIN");
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
                                                        .customerName("Limit Source")
                                                        .iban(SOURCE_IBAN)
                                                        .currency(Currency.TRY)
                                                        .build()));
        source.setBalance(new BigDecimal("10000000.00"));
        accountRepository.save(source);

        Account target =
                accountRepository
                        .findByIban(TARGET_IBAN)
                        .orElseGet(
                                () ->
                                        accountRepository.save(
                                                Account.builder()
                                                        .customerName("Limit Target")
                                                        .iban(TARGET_IBAN)
                                                        .currency(Currency.TRY)
                                                        .build()));
        target.setBalance(new BigDecimal("0.00"));
        accountRepository.save(target);
    }

    @Test
    @Order(1)
    @DisplayName("Create limit — ADMIN should succeed")
    void createLimit_success() throws Exception {
        String adminToken = getAdminToken();

        mockMvc.perform(
                        post("/api/v1/limits")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"role\":\"CHECKER\","
                                                + "\"maxSingleAmount\":50000.00,"
                                                + "\"maxDailyAmount\":200000.00,"
                                                + "\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CHECKER"))
                .andExpect(jsonPath("$.maxSingleAmount").value(50000.00))
                .andExpect(jsonPath("$.maxDailyAmount").value(200000.00));
    }

    @Test
    @Order(2)
    @DisplayName("List limits — should return all")
    void listLimits_success() throws Exception {
        String adminToken = getAdminToken();

        mockMvc.perform(get("/api/v1/limits").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("Exceed single limit — should return 422")
    void exceedSingleLimit() throws Exception {
        ensureAccountsExist();

        String adminToken = getAdminToken();

        // Create strict limit for GBP (not pre-seeded in V5 migration)
        mockMvc.perform(
                post("/api/v1/limits")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"role\":\"MAKER\","
                                        + "\"maxSingleAmount\":100.00,"
                                        + "\"maxDailyAmount\":500.00,"
                                        + "\"currency\":\"GBP\"}"));

        String makerToken;
        try {
            makerToken = helper.loginAndGetToken("limitmaker");
        } catch (Throwable e) {
            makerToken = helper.registerAndGetToken("limitmaker", "limitmaker@test.com", "MAKER");
        }

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + makerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        String.format(
                                                "{\"idempotencyKey\":\"idem-limit-single\","
                                                        + "\"sourceIban\":\"%s\","
                                                        + "\"targetIban\":\"%s\","
                                                        + "\"amount\":200.00,"
                                                        + "\"currency\":\"GBP\"}",
                                                SOURCE_IBAN, TARGET_IBAN)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("LIMIT_EXCEEDED_SINGLE"));
    }
}
