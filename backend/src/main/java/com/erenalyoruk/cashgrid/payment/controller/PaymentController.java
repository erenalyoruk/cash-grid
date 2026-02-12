package com.erenalyoruk.cashgrid.payment.controller;

import com.erenalyoruk.cashgrid.common.dto.PageResponse;
import com.erenalyoruk.cashgrid.payment.dto.*;
import com.erenalyoruk.cashgrid.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('MAKER')")
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(request, principal.getName()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CHECKER')")
    public ResponseEntity<PaymentResponse> approve(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(paymentService.approve(id, principal.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CHECKER')")
    public ResponseEntity<PaymentResponse> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectPaymentRequest request,
            Principal principal) {
        return ResponseEntity.ok(paymentService.reject(id, request, principal.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'CHECKER')")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'CHECKER')")
    public ResponseEntity<PageResponse<PaymentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy) {
        return ResponseEntity.ok(paymentService.list(page, size, status, createdBy));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('CHECKER')")
    public ResponseEntity<PageResponse<PaymentResponse>> listPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(
                paymentService.listPendingForChecker(page, size, principal.getName()));
    }
}
