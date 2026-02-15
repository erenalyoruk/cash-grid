package com.erenalyoruk.cashgrid.audit.repository;

import com.erenalyoruk.cashgrid.audit.model.AuditAction;
import com.erenalyoruk.cashgrid.audit.model.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class AuditLogRepositoryImpl implements AuditLogRepositoryCustom {

    private final EntityManager em;

    public AuditLogRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<AuditLog> search(
            AuditAction action,
            UUID performedBy,
            Instant from,
            Instant to,
            String correlationId,
            String entityType,
            Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<AuditLog> cq = cb.createQuery(AuditLog.class);
        Root<AuditLog> root = cq.from(AuditLog.class);

        List<Predicate> predicates = new ArrayList<>();
        if (action != null) predicates.add(cb.equal(root.get("action"), action));
        if (performedBy != null) predicates.add(cb.equal(root.get("performedBy"), performedBy));
        if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
        if (correlationId != null && !correlationId.isBlank())
            predicates.add(cb.equal(root.get("correlationId"), correlationId));
        if (entityType != null && !entityType.isBlank())
            predicates.add(cb.equal(root.get("entityType"), entityType));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        var query = em.createQuery(cq);

        // optimized count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AuditLog> countRoot = countQuery.from(AuditLog.class);
        List<Predicate> countPredicates = new ArrayList<>();
        if (action != null) countPredicates.add(cb.equal(countRoot.get("action"), action));
        if (performedBy != null)
            countPredicates.add(cb.equal(countRoot.get("performedBy"), performedBy));
        if (from != null)
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"), from));
        if (to != null) countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("createdAt"), to));
        if (correlationId != null && !correlationId.isBlank())
            countPredicates.add(cb.equal(countRoot.get("correlationId"), correlationId));
        if (entityType != null && !entityType.isBlank())
            countPredicates.add(cb.equal(countRoot.get("entityType"), entityType));
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long totalLong = em.createQuery(countQuery).getSingleResult();
        int total = totalLong != null ? totalLong.intValue() : 0;

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<AuditLog> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
