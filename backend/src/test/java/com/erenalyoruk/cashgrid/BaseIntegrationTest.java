package com.erenalyoruk.cashgrid;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.erenalyoruk.cashgrid.common.filter.CorrelationIdFilter;
import com.erenalyoruk.cashgrid.common.filter.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("resource")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres;

    static {
        postgres =
                new PostgreSQLContainer<>("postgres:18-alpine")
                        .withDatabaseName("cashgrid_test")
                        .withUsername("test")
                        .withPassword("test");
        postgres.start();
    }

    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private CorrelationIdFilter correlationIdFilter;

    @Autowired private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUpBase() {
        this.mockMvc =
                webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .addFilters(correlationIdFilter, rateLimitFilter)
                        .build();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
