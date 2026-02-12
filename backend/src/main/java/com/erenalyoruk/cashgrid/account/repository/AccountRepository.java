package com.erenalyoruk.cashgrid.account.repository;

import com.erenalyoruk.cashgrid.account.model.Account;
import com.erenalyoruk.cashgrid.account.model.Currency;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIban(String iban);

    boolean existsByIban(String iban);

    Page<Account> findByIsActiveTrue(Pageable pageable);

    Page<Account> findByCurrencyAndIsActiveTrue(Currency currency, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.iban = :iban")
    Optional<Account> findByIbanForUpdate(@Param("iban") String iban);
}
