package com.erenalyoruk.cashgrid.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String correlationId,
        List<FieldError> fieldErrors) {

    public record FieldError(String field, String message) {}
}
