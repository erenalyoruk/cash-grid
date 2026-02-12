package com.erenalyoruk.cashgrid.account.dto;

import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(@Size(min = 2, max = 100) String customerName) {}
