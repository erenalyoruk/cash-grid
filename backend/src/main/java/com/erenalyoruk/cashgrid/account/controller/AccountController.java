package com.erenalyoruk.cashgrid.account.controller;

import com.erenalyoruk.cashgrid.account.dto.*;
import com.erenalyoruk.cashgrid.account.service.AccountService;
import com.erenalyoruk.cashgrid.common.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'CHECKER')")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @GetMapping("/iban/{iban}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'CHECKER')")
    public ResponseEntity<AccountResponse> getByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getByIban(iban));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'CHECKER')")
    public ResponseEntity<PageResponse<AccountResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(accountService.list(page, size, sortBy, currency));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        accountService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        accountService.activate(id);
        return ResponseEntity.noContent().build();
    }
}
