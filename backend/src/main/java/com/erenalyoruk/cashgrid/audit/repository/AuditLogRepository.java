package com.erenalyoruk.cashgrid.audit.repository;

import com.erenalyoruk.cashgrid.audit.model.AuditLog;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID>, AuditLogRepositoryCustom {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByPerformedBy(UUID performedBy, Pageable pageable);

    Page<AuditLog> findByCorrelationId(String correlationId, Pageable pageable);
}
