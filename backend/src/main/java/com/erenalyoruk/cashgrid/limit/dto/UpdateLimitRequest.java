package com.erenalyoruk.cashgrid.limit.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record UpdateLimitRequest(
        @DecimalMin("0.01") BigDecimal maxSingleAmount,
        @DecimalMin("0.01") BigDecimal maxDailyAmount) {}
