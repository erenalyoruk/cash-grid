package com.erenalyoruk.cashgrid.account.service;

import com.erenalyoruk.cashgrid.account.dto.*;
import com.erenalyoruk.cashgrid.account.mapper.AccountMapper;
import com.erenalyoruk.cashgrid.account.model.Account;
import com.erenalyoruk.cashgrid.account.model.Currency;
import com.erenalyoruk.cashgrid.account.repository.AccountRepository;
import com.erenalyoruk.cashgrid.common.dto.PageResponse;
import com.erenalyoruk.cashgrid.common.exception.BusinessException;
import com.erenalyoruk.cashgrid.common.exception.ConflictException;
import com.erenalyoruk.cashgrid.common.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (accountRepository.existsByIban(request.iban().replaceAll("\\s", "").toUpperCase())) {
            throw new ConflictException("IBAN_EXISTS", "Account with this IBAN already exists");
        }

        Account account = accountMapper.toEntity(request);
        account.setIban(request.iban().replaceAll("\\s", "").toUpperCase());

        if (request.currency() != null && !request.currency().isBlank()) {
            try {
                account.setCurrency(Currency.valueOf(request.currency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        "INVALID_CURRENCY", "Invalid currency: " + request.currency());
            }
        }

        account = accountRepository.save(account);

        log.info("Account created: {} ({})", account.getIban(), account.getId());

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        Account account =
                accountRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getByIban(String iban) {
        Account account =
                accountRepository
                        .findByIban(iban.replaceAll("\\s", "").toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "iban", iban));

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(int page, int size, String sortBy, String currency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        Page<Account> accountPage;

        if (currency != null && !currency.isBlank()) {
            try {
                Currency cur = Currency.valueOf(currency.toUpperCase());
                accountPage = accountRepository.findByCurrencyAndIsActiveTrue(cur, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("INVALID_CURRENCY", "Invalid currency: " + currency);
            }
        } else {
            accountPage = accountRepository.findByIsActiveTrue(pageable);
        }

        return PageResponse.<AccountResponse>builder()
                .content(accountPage.getContent().stream().map(accountMapper::toResponse).toList())
                .page(accountPage.getNumber())
                .size(accountPage.getSize())
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .last(accountPage.isLast())
                .build();
    }

    @Transactional
    public AccountResponse update(UUID id, UpdateAccountRequest request) {
        Account account =
                accountRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (request.customerName() != null && !request.customerName().isBlank()) {
            account.setCustomerName(request.customerName());
        }

        account = accountRepository.save(account);

        log.info("Account updated: {}", account.getId());

        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deactivate(UUID id) {
        Account account =
                accountRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        account.setIsActive(false);
        accountRepository.save(account);

        log.info("Account deactivated: {}", account.getId());
    }

    @Transactional
    public void activate(UUID id) {
        Account account =
                accountRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        account.setIsActive(true);
        accountRepository.save(account);

        log.info("Account activated: {}", account.getId());
    }
}
