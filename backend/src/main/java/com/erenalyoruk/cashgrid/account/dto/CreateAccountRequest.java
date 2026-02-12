package com.erenalyoruk.cashgrid.account.dto;

import com.erenalyoruk.cashgrid.common.validation.ValidIban;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Size(min = 2, max = 100) String customerName,
        @NotBlank @ValidIban String iban,
        String currency) {}
