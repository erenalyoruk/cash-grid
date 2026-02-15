package com.erenalyoruk.cashgrid.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequest(@NotBlank @Email String newEmail) {}
