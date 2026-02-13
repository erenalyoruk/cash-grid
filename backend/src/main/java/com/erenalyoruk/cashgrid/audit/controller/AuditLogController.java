package com.erenalyoruk.cashgrid.audit.controller;

import com.erenalyoruk.cashgrid.audit.dto.AuditLogResponse;
import com.erenalyoruk.cashgrid.audit.mapper.AuditLogMapper;
import com.erenalyoruk.cashgrid.audit.repository.AuditLogRepository;
import com.erenalyoruk.cashgrid.common.dto.PageResponse;
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
}
