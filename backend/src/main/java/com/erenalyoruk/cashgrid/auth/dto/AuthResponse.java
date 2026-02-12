package com.erenalyoruk.cashgrid.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken, String refreshToken, String tokenType, String username, String role) {}
