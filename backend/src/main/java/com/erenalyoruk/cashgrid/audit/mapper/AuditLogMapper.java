package com.erenalyoruk.cashgrid.audit.mapper;

import com.erenalyoruk.cashgrid.audit.dto.AuditLogResponse;
import com.erenalyoruk.cashgrid.audit.model.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "action", expression = "java(auditLog.getAction().name())")
    AuditLogResponse toResponse(AuditLog auditLog);
}
