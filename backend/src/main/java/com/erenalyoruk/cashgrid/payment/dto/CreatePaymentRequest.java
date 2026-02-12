package com.erenalyoruk.cashgrid.payment.dto;

import com.erenalyoruk.cashgrid.common.validation.ValidIban;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @NotBlank @ValidIban String sourceIban,
        @NotBlank @ValidIban String targetIban,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String currency,
        @Size(max = 255) String description) {}
