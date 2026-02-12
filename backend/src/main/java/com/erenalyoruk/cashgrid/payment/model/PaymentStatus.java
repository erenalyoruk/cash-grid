package com.erenalyoruk.cashgrid.payment.model;

import java.util.Set;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PROCESSING,
    COMPLETED,
    FAILED;

    private static final java.util.Map<PaymentStatus, Set<PaymentStatus>> TRANSITIONS =
            java.util.Map.of(
                    PENDING, Set.of(APPROVED, REJECTED),
                    APPROVED, Set.of(PROCESSING),
                    PROCESSING, Set.of(COMPLETED, FAILED),
                    REJECTED, Set.of(),
                    COMPLETED, Set.of(),
                    FAILED, Set.of());

    public boolean canTransitionTo(PaymentStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
