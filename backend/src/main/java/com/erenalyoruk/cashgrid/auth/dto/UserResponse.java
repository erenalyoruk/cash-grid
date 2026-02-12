package com.erenalyoruk.cashgrid.auth.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserResponse(
        UUID id, String username, String email, String role, Boolean isActive, Instant createdAt) {}
