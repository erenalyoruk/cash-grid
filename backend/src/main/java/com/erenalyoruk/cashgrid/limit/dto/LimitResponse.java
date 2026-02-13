package com.erenalyoruk.cashgrid.limit.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record LimitResponse(
        UUID id,
        String role,
        BigDecimal maxSingleAmount,
        BigDecimal maxDailyAmount,
        String currency,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt) {}
