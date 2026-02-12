package com.erenalyoruk.cashgrid.common.exception;

import com.erenalyoruk.cashgrid.common.dto.ErrorResponse;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .errorCode("RESOURCE_NOT_FOUND")
                        .message(ex.getMessage())
                        .correlationId(MDC.get("correlationId"))
                        .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                        .errorCode(ex.getErrorCode())
                        .message(ex.getMessage())
                        .correlationId(MDC.get("correlationId"))
                        .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflict: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.CONFLICT.value())
                        .errorCode(ex.getErrorCode())
                        .message(ex.getMessage())
                        .correlationId(MDC.get("correlationId"))
                        .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                fe ->
                                        new ErrorResponse.FieldError(
                                                fe.getField(), fe.getDefaultMessage()))
                        .toList();

        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .errorCode("VALIDATION_ERROR")
                        .message("Validation failed")
                        .correlationId(MDC.get("correlationId"))
                        .fieldErrors(fieldErrors)
                        .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .correlationId(MDC.get("correlationId"))
                        .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
