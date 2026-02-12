package com.erenalyoruk.cashgrid.audit.service;

import com.erenalyoruk.cashgrid.audit.model.AuditAction;
import com.erenalyoruk.cashgrid.audit.model.AuditLog;
import com.erenalyoruk.cashgrid.audit.repository.AuditLogRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String entityType,
            UUID entityId,
            AuditAction action,
            UUID performedBy,
            String details) {

        AuditLog auditLog =
                AuditLog.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .action(action)
                        .performedBy(performedBy)
                        .correlationId(MDC.get("correlationId"))
                        .details(details)
                        .build();

        auditLogRepository.save(auditLog);

        log.debug("Audit log: {} {} on {}:{}", action, performedBy, entityType, entityId);
    }
}
