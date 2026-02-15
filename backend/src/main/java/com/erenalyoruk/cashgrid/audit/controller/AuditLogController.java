package com.erenalyoruk.cashgrid.audit.controller;

import com.erenalyoruk.cashgrid.audit.dto.AuditLogResponse;
import com.erenalyoruk.cashgrid.audit.mapper.AuditLogMapper;
import com.erenalyoruk.cashgrid.audit.model.AuditAction;
import com.erenalyoruk.cashgrid.audit.repository.AuditLogRepository;
import com.erenalyoruk.cashgrid.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var auditPage =
                auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);

        return ResponseEntity.ok(
                PageResponse.<AuditLogResponse>builder()
                        .content(
                                auditPage.getContent().stream()
                                        .map(auditLogMapper::toResponse)
                                        .toList())
                        .page(auditPage.getNumber())
                        .size(auditPage.getSize())
                        .totalElements(auditPage.getTotalElements())
                        .totalPages(auditPage.getTotalPages())
                        .last(auditPage.isLast())
                        .build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var auditPage = auditLogRepository.findByPerformedBy(userId, pageable);

        return ResponseEntity.ok(
                PageResponse.<AuditLogResponse>builder()
                        .content(
                                auditPage.getContent().stream()
                                        .map(auditLogMapper::toResponse)
                                        .toList())
                        .page(auditPage.getNumber())
                        .size(auditPage.getSize())
                        .totalElements(auditPage.getTotalElements())
                        .totalPages(auditPage.getTotalPages())
                        .last(auditPage.isLast())
                        .build());
    }

    @GetMapping("/correlation/{correlationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getByCorrelation(
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var auditPage = auditLogRepository.findByCorrelationId(correlationId, pageable);

        return ResponseEntity.ok(
                PageResponse.<AuditLogResponse>builder()
                        .content(
                                auditPage.getContent().stream()
                                        .map(auditLogMapper::toResponse)
                                        .toList())
                        .page(auditPage.getNumber())
                        .size(auditPage.getSize())
                        .totalElements(auditPage.getTotalElements())
                        .totalPages(auditPage.getTotalPages())
                        .last(auditPage.isLast())
                        .build());
    }

    @Operation(
            summary = "Search audit logs",
            description =
                    "Search audit logs with optional filters: action, performedBy, from, to,"
                            + " correlationId, entityType")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> search(
            @Parameter(in = ParameterIn.QUERY, description = "Audit action (e.g. PAYMENT_CREATED)")
                    @RequestParam(required = false)
                    String action,
            @Parameter(in = ParameterIn.QUERY, description = "Performed by user id (UUID)")
                    @RequestParam(required = false)
                    java.util.UUID performedBy,
            @Parameter(in = ParameterIn.QUERY, description = "From timestamp (ISO instant)")
                    @RequestParam(required = false)
                    java.time.Instant from,
            @Parameter(in = ParameterIn.QUERY, description = "To timestamp (ISO instant)")
                    @RequestParam(required = false)
                    java.time.Instant to,
            @Parameter(in = ParameterIn.QUERY, description = "Correlation ID")
                    @RequestParam(required = false)
                    String correlationId,
            @Parameter(in = ParameterIn.QUERY, description = "Entity type (e.g. PAYMENT)")
                    @RequestParam(required = false)
                    String entityType,
            @Parameter(in = ParameterIn.QUERY, description = "Page number")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(in = ParameterIn.QUERY, description = "Page size")
                    @RequestParam(defaultValue = "20")
                    int size) {

        java.time.Instant fromInst = from;
        java.time.Instant toInst = to;
        AuditAction audAction = null;
        if (action != null && !action.isBlank()) {
            try {
                audAction = AuditAction.valueOf(action);
            } catch (IllegalArgumentException e) {
                audAction = null;
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        var auditPage =
                auditLogRepository.search(
                        audAction,
                        performedBy,
                        fromInst,
                        toInst,
                        correlationId,
                        entityType,
                        pageable);

        return ResponseEntity.ok(
                PageResponse.<AuditLogResponse>builder()
                        .content(
                                auditPage.getContent().stream()
                                        .map(auditLogMapper::toResponse)
                                        .toList())
                        .page(auditPage.getNumber())
                        .size(auditPage.getSize())
                        .totalElements(auditPage.getTotalElements())
                        .totalPages(auditPage.getTotalPages())
                        .last(auditPage.isLast())
                        .build());
    }
}
