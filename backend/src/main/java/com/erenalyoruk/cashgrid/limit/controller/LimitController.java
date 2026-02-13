package com.erenalyoruk.cashgrid.limit.controller;

import com.erenalyoruk.cashgrid.limit.dto.*;
import com.erenalyoruk.cashgrid.limit.service.LimitService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LimitResponse> create(@Valid @RequestBody CreateLimitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(limitService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LimitResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateLimitRequest request) {
        return ResponseEntity.ok(limitService.update(id, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LimitResponse>> listAll() {
        return ResponseEntity.ok(limitService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LimitResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(limitService.getById(id));
    }
}
