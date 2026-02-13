package com.erenalyoruk.cashgrid.limit.service;

import com.erenalyoruk.cashgrid.auth.model.Role;
import com.erenalyoruk.cashgrid.common.exception.BusinessException;
import com.erenalyoruk.cashgrid.common.exception.ConflictException;
import com.erenalyoruk.cashgrid.common.exception.ResourceNotFoundException;
import com.erenalyoruk.cashgrid.limit.dto.*;
import com.erenalyoruk.cashgrid.limit.mapper.LimitMapper;
import com.erenalyoruk.cashgrid.limit.model.Limit;
import com.erenalyoruk.cashgrid.limit.repository.LimitRepository;
import com.erenalyoruk.cashgrid.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LimitService {

    private static final Logger log = LoggerFactory.getLogger(LimitService.class);

    private final LimitRepository limitRepository;
    private final PaymentRepository paymentRepository;
    private final LimitMapper limitMapper;

    @Transactional
    public LimitResponse create(CreateLimitRequest request) {
        Role role = parseRole(request.role());
        String currency = request.currency().toUpperCase();

        if (limitRepository.existsByRoleAndCurrency(role, currency)) {
            throw new ConflictException(
                    "LIMIT_EXISTS",
                    String.format(
                            "Limit already exists for role %s and currency %s", role, currency));
        }

        Limit limit =
                Limit.builder()
                        .role(role)
                        .maxSingleAmount(request.maxSingleAmount())
                        .maxDailyAmount(request.maxDailyAmount())
                        .currency(currency)
                        .build();

        limit = limitRepository.save(limit);

        log.info(
                "Limit created: {} {} - single:{} daily:{}",
                role,
                currency,
                limit.getMaxSingleAmount(),
                limit.getMaxDailyAmount());

        return limitMapper.toResponse(limit);
    }

    @Transactional
    public LimitResponse update(UUID id, UpdateLimitRequest request) {
        Limit limit =
                limitRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Limit", "id", id));

        if (request.maxSingleAmount() != null) {
            limit.setMaxSingleAmount(request.maxSingleAmount());
        }
        if (request.maxDailyAmount() != null) {
            limit.setMaxDailyAmount(request.maxDailyAmount());
        }

        limit = limitRepository.save(limit);

        log.info("Limit updated: {}", id);

        return limitMapper.toResponse(limit);
    }

    @Transactional(readOnly = true)
    public List<LimitResponse> listAll() {
        return limitRepository.findAll().stream().map(limitMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LimitResponse getById(UUID id) {
        Limit limit =
                limitRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Limit", "id", id));
        return limitMapper.toResponse(limit);
    }

    public void checkLimits(Role role, String currency, BigDecimal amount, UUID userId) {
        Limit limit =
                limitRepository.findByRoleAndCurrencyAndIsActiveTrue(role, currency).orElse(null);

        if (limit == null) {
            log.debug("No limit defined for role {} currency {}", role, currency);
            return;
        }

        // Single amount check
        if (amount.compareTo(limit.getMaxSingleAmount()) > 0) {
            throw new BusinessException(
                    "LIMIT_EXCEEDED_SINGLE",
                    String.format(
                            "Amount %s exceeds single transaction limit %s for role %s",
                            amount, limit.getMaxSingleAmount(), role));
        }

        // Daily amount check
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        BigDecimal dailySpent = paymentRepository.sumDailySpent(userId, currency, startOfDay);
        BigDecimal projectedTotal = dailySpent.add(amount);

        if (projectedTotal.compareTo(limit.getMaxDailyAmount()) > 0) {
            throw new BusinessException(
                    "LIMIT_EXCEEDED_DAILY",
                    String.format(
                            "Projected daily total %s exceeds daily limit %s for role %s (already"
                                    + " spent: %s)",
                            projectedTotal, limit.getMaxDailyAmount(), role, dailySpent));
        }

        log.debug(
                "Limit check passed: role={} currency={} amount={} dailySpent={} dailyLimit={}",
                role,
                currency,
                amount,
                dailySpent,
                limit.getMaxDailyAmount());
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_ROLE", "Invalid role: " + role);
        }
    }
}
