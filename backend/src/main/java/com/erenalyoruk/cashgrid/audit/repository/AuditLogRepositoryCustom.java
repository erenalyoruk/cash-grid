package com.erenalyoruk.cashgrid.audit.repository;

import com.erenalyoruk.cashgrid.audit.model.AuditAction;
import com.erenalyoruk.cashgrid.audit.model.AuditLog;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogRepositoryCustom {
    Page<AuditLog> search(
            AuditAction action,
            java.util.UUID performedBy,
            Instant from,
            Instant to,
            String correlationId,
            String entityType,
            Pageable pageable);
}
