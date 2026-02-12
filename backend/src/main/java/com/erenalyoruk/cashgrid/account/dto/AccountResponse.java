package com.erenalyoruk.cashgrid.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AccountResponse(
        UUID id,
        String customerName,
        String iban,
        String currency,
        BigDecimal balance,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt) {}
