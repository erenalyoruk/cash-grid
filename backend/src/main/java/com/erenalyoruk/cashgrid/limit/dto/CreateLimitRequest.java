package com.erenalyoruk.cashgrid.limit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateLimitRequest(
        @NotBlank String role,
        @NotNull @DecimalMin("0.01") BigDecimal maxSingleAmount,
        @NotNull @DecimalMin("0.01") BigDecimal maxDailyAmount,
        @NotBlank String currency) {}
