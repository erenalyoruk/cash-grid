package com.erenalyoruk.cashgrid.limit.repository;

import com.erenalyoruk.cashgrid.auth.model.Role;
import com.erenalyoruk.cashgrid.limit.model.Limit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitRepository extends JpaRepository<Limit, UUID> {

    Optional<Limit> findByRoleAndCurrencyAndIsActiveTrue(Role role, String currency);

    boolean existsByRoleAndCurrency(Role role, String currency);
}
