package com.erenalyoruk.cashgrid.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PaymentResponse(
        UUID id,
        String idempotencyKey,
        String sourceIban,
        String targetIban,
        BigDecimal amount,
        String currency,
        String description,
        String status,
        String createdByUsername,
        String approvedByUsername,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt) {}
