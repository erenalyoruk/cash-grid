package com.erenalyoruk.cashgrid.audit.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AuditLogResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        UUID performedBy,
        String correlationId,
        String details,
        Instant createdAt) {}
