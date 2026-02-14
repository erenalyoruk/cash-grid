package com.erenalyoruk.cashgrid.payment.service;

import com.erenalyoruk.cashgrid.account.model.Account;
import com.erenalyoruk.cashgrid.account.repository.AccountRepository;
import com.erenalyoruk.cashgrid.audit.model.AuditAction;
import com.erenalyoruk.cashgrid.audit.service.AuditService;
import com.erenalyoruk.cashgrid.auth.model.User;
import com.erenalyoruk.cashgrid.auth.repository.UserRepository;
import com.erenalyoruk.cashgrid.common.dto.PageResponse;
import com.erenalyoruk.cashgrid.common.exception.BusinessException;
import com.erenalyoruk.cashgrid.common.exception.ResourceNotFoundException;
import com.erenalyoruk.cashgrid.limit.service.LimitService;
import com.erenalyoruk.cashgrid.payment.dto.*;
import com.erenalyoruk.cashgrid.payment.mapper.PaymentMapper;
import com.erenalyoruk.cashgrid.payment.model.Payment;
import com.erenalyoruk.cashgrid.payment.model.PaymentStatus;
import com.erenalyoruk.cashgrid.payment.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

        private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

        private final PaymentRepository paymentRepository;
        private final AccountRepository accountRepository;
        private final UserRepository userRepository;
        private final PaymentMapper paymentMapper;
        private final AuditService auditService;
        private final LimitService limitService;

        @Transactional
        public PaymentResponse create(CreatePaymentRequest request, String username) {
                // Idempotency check — return existing if same key
                Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
                if (existing.isPresent()) {
                        log.info("Idempotent request detected: {}", request.idempotencyKey());
                        return paymentMapper.toResponse(existing.get());
                }

                User maker = userRepository
                                .findByUsername(username)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("User", "username", username));

                String sourceIban = request.sourceIban().replaceAll("\\s", "").toUpperCase();
                String targetIban = request.targetIban().replaceAll("\\s", "").toUpperCase();

                if (sourceIban.equals(targetIban)) {
                        throw new BusinessException(
                                        "SAME_ACCOUNT", "Source and target accounts cannot be the same");
                }

                Account sourceAccount = accountRepository
                                .findByIban(sourceIban)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Account", "iban", sourceIban));

                Account targetAccount = accountRepository
                                .findByIban(targetIban)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Account", "iban", targetIban));

                if (!sourceAccount.getIsActive()) {
                        throw new BusinessException("ACCOUNT_INACTIVE", "Source account is inactive");
                }
                if (!targetAccount.getIsActive()) {
                        throw new BusinessException("ACCOUNT_INACTIVE", "Target account is inactive");
                }

                String currency = (request.currency() != null && !request.currency().isBlank())
                                ? request.currency().toUpperCase()
                                : "TRY";

                // Limit check
                limitService.checkLimits(maker.getRole(), currency, request.amount(), maker.getId());

                Payment payment = Payment.builder()
                                .idempotencyKey(request.idempotencyKey())
                                .sourceAccount(sourceAccount)
                                .targetAccount(targetAccount)
                                .amount(request.amount())
                                .currency(currency)
                                .description(request.description())
                                .status(PaymentStatus.PENDING)
                                .createdBy(maker)
                                .build();

                payment = paymentRepository.save(payment);

                log.info("Payment created: {} by {}", payment.getId(), username);

                auditService.log(
                                "PAYMENT",
                                payment.getId(),
                                AuditAction.PAYMENT_CREATED,
                                maker.getId(),
                                MDC.get("correlationId"),
                                String.format(
                                                "{\"amount\":%s,\"sourceIban\":\"%s\",\"targetIban\":\"%s\"}",
                                                payment.getAmount(), sourceIban, targetIban));

                return paymentMapper.toResponse(payment);
        }

        @Transactional
        public PaymentResponse approve(UUID paymentId, String checkerUsername) {
                Payment payment = paymentRepository
                                .findById(paymentId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Payment", "id", paymentId));

                User checker = userRepository
                                .findByUsername(checkerUsername)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User", "username", checkerUsername));

                // Maker-Checker: maker cannot approve own payment
                if (payment.getCreatedBy().getId().equals(checker.getId())) {
                        throw new BusinessException("SELF_APPROVAL", "Maker cannot approve their own payment");
                }

                if (!payment.getStatus().canTransitionTo(PaymentStatus.APPROVED)) {
                        throw new BusinessException(
                                        "INVALID_TRANSITION",
                                        String.format("Cannot transition from %s to APPROVED", payment.getStatus()));
                }

                payment.setStatus(PaymentStatus.APPROVED);
                payment.setApprovedBy(checker);
                payment = paymentRepository.save(payment);

                log.info("Payment approved: {} by {}", payment.getId(), checkerUsername);

                auditService.log(
                                "PAYMENT",
                                payment.getId(),
                                AuditAction.PAYMENT_APPROVED,
                                checker.getId(),
                                MDC.get("correlationId"),
                                null);

                // Trigger processing
                return processPayment(payment);
        }

        @Transactional
        public PaymentResponse reject(
                        UUID paymentId, RejectPaymentRequest request, String checkerUsername) {
                Payment payment = paymentRepository
                                .findById(paymentId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Payment", "id", paymentId));

                User checker = userRepository
                                .findByUsername(checkerUsername)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User", "username", checkerUsername));

                if (payment.getCreatedBy().getId().equals(checker.getId())) {
                        throw new BusinessException("SELF_REJECTION", "Maker cannot reject their own payment");
                }

                if (!payment.getStatus().canTransitionTo(PaymentStatus.REJECTED)) {
                        throw new BusinessException(
                                        "INVALID_TRANSITION",
                                        String.format("Cannot transition from %s to REJECTED", payment.getStatus()));
                }

                payment.setStatus(PaymentStatus.REJECTED);
                payment.setApprovedBy(checker);
                payment.setRejectionReason(request.reason());
                payment = paymentRepository.save(payment);

                log.info("Payment rejected: {} by {}", payment.getId(), checkerUsername);

                auditService.log(
                                "PAYMENT",
                                payment.getId(),
                                AuditAction.PAYMENT_REJECTED,
                                checker.getId(),
                                MDC.get("correlationId"),
                                String.format("{\"reason\":\"%s\"}", request.reason()));

                return paymentMapper.toResponse(payment);
        }

        @Transactional(readOnly = true)
        public PaymentResponse getById(UUID id) {
                Payment payment = paymentRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

                return paymentMapper.toResponse(payment);
        }

        @Transactional(readOnly = true)
        public PageResponse<PaymentResponse> list(int page, int size, String status, String username) {

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Payment> paymentPage;

                if (status != null && !status.isBlank()) {
                        try {
                                PaymentStatus ps = PaymentStatus.valueOf(status.toUpperCase());
                                paymentPage = paymentRepository.findByStatus(ps, pageable);
                        } catch (IllegalArgumentException e) {
                                throw new BusinessException("INVALID_STATUS", "Invalid status: " + status);
                        }
                } else if (username != null && !username.isBlank()) {
                        User user = userRepository
                                        .findByUsername(username)
                                        .orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "User", "username", username));
                        paymentPage = paymentRepository.findByCreatedById(user.getId(), pageable);
                } else {
                        paymentPage = paymentRepository.findAll(pageable);
                }

                return PageResponse.<PaymentResponse>builder()
                                .content(paymentPage.getContent().stream().map(paymentMapper::toResponse).toList())
                                .page(paymentPage.getNumber())
                                .size(paymentPage.getSize())
                                .totalElements(paymentPage.getTotalElements())
                                .totalPages(paymentPage.getTotalPages())
                                .last(paymentPage.isLast())
                                .build();
        }

        @Transactional(readOnly = true)
        public PageResponse<PaymentResponse> listPendingForChecker(
                        int page, int size, String checkerUsername) {

                User checker = userRepository
                                .findByUsername(checkerUsername)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User", "username", checkerUsername));

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                Page<Payment> paymentPage = paymentRepository.findPendingForChecker(
                                PaymentStatus.PENDING, checker.getId(), pageable);

                return PageResponse.<PaymentResponse>builder()
                                .content(paymentPage.getContent().stream().map(paymentMapper::toResponse).toList())
                                .page(paymentPage.getNumber())
                                .size(paymentPage.getSize())
                                .totalElements(paymentPage.getTotalElements())
                                .totalPages(paymentPage.getTotalPages())
                                .last(paymentPage.isLast())
                                .build();
        }

        private PaymentResponse processPayment(Payment payment) {
                payment.setStatus(PaymentStatus.PROCESSING);
                paymentRepository.save(payment);

                auditService.log(
                                "PAYMENT",
                                payment.getId(),
                                AuditAction.PAYMENT_PROCESSING,
                                payment.getApprovedBy().getId(),
                                MDC.get("correlationId"),
                                null);

                try {
                        // Lock accounts to prevent race conditions
                        Account source = accountRepository
                                        .findByIdForUpdate(payment.getSourceAccount().getId())
                                        .orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "Account",
                                                                        "id",
                                                                        payment.getSourceAccount().getId()));

                        Account target = accountRepository
                                        .findByIdForUpdate(payment.getTargetAccount().getId())
                                        .orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "Account",
                                                                        "id",
                                                                        payment.getTargetAccount().getId()));

                        // Insufficient balance check
                        if (source.getBalance().compareTo(payment.getAmount()) < 0) {
                                payment.setStatus(PaymentStatus.FAILED);
                                paymentRepository.save(payment);

                                auditService.log(
                                                "PAYMENT",
                                                payment.getId(),
                                                AuditAction.PAYMENT_FAILED,
                                                payment.getApprovedBy().getId(),
                                                MDC.get("correlationId"),
                                                "{\"reason\":\"Insufficient balance\"}");

                                return paymentMapper.toResponse(payment);
                        }

                        // Transfer
                        source.setBalance(source.getBalance().subtract(payment.getAmount()));
                        target.setBalance(target.getBalance().add(payment.getAmount()));

                        accountRepository.save(source);
                        accountRepository.save(target);

                        payment.setStatus(PaymentStatus.COMPLETED);
                        Payment savedPayment = paymentRepository.save(payment);

                        log.info("Payment completed: {}", savedPayment.getId());

                        auditService.log(
                                        "PAYMENT",
                                        savedPayment.getId(),
                                        AuditAction.PAYMENT_COMPLETED,
                                        savedPayment.getApprovedBy().getId(),
                                        MDC.get("correlationId"),
                                        String.format(
                                                        "{\"sourceBalance\":%s,\"targetBalance\":%s}",
                                                        source.getBalance(), target.getBalance()));

                        return paymentMapper.toResponse(savedPayment);

                } catch (Exception e) {
                        log.error("Payment processing failed: {}", payment.getId(), e);

                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);

                        auditService.log(
                                        "PAYMENT",
                                        payment.getId(),
                                        AuditAction.PAYMENT_FAILED,
                                        payment.getApprovedBy().getId(),
                                        MDC.get("correlationId"),
                                        String.format("{\"reason\":\"%s\"}", e.getMessage()));

                        return paymentMapper.toResponse(payment);
                }
        }
}
