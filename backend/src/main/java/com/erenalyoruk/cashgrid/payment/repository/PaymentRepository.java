package com.erenalyoruk.cashgrid.payment.repository;

import com.erenalyoruk.cashgrid.payment.model.Payment;
import com.erenalyoruk.cashgrid.payment.model.PaymentStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByCreatedById(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = :status " + "AND p.createdBy.id <> :checkerId")
    Page<Payment> findPendingForChecker(
            @Param("status") PaymentStatus status,
            @Param("checkerId") UUID checkerId,
            Pageable pageable);
}
