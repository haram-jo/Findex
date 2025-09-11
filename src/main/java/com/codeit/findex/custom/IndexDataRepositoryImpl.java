package com.codeit.findex.custom;
import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.entity.IndexData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

    private final EntityManager em;

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
        "baseDate", "marketPrice", "closingPrice", "highPrice", "lowPrice",
        "versus", "fluctuationRate", "tradingQuantity", "tradingPrice", "marketTotalAmount"
    );

    @Override
    public List<IndexData> search(IndexDataSearchCondition condition) {
        StringBuilder jpqlBuilder = new StringBuilder("SELECT d FROM IndexData d WHERE 1=1");

        if (condition.indexInfoId() != null) {
            jpqlBuilder.append(" AND d.indexInfo.id = :indexInfoId");
        }
        if (condition.startDate() != null) {
            jpqlBuilder.append(" AND d.baseDate >= :startDate");
        }
        if (condition.endDate() != null) {
            jpqlBuilder.append(" AND d.baseDate <= :endDate");
        }

        Long resolvedId = condition.getResolvedId();
        if (resolvedId != null) {
            if (condition.sortDirection().equalsIgnoreCase("DESC")) {
                jpqlBuilder.append(" AND d.id < :resolvedId");
            } else {
                jpqlBuilder.append(" AND d.id > :resolvedId");
            }
        }

        String sortBy = condition.sortField();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("허용되지 않은 정렬 필드입니다: " + sortBy);
        }
        String sortDirection = condition.sortDirection().equalsIgnoreCase("DESC") ? "DESC" : "ASC";
        jpqlBuilder.append(" ORDER BY d.").append(sortBy).append(" ").append(sortDirection);
        if (!sortBy.equals("id")) {
            jpqlBuilder.append(", d.id ").append(sortDirection);
        }

        TypedQuery<IndexData> query = em.createQuery(jpqlBuilder.toString(), IndexData.class);

        if (condition.indexInfoId() != null) {
            query.setParameter("indexInfoId", condition.indexInfoId());
        }
        if (condition.startDate() != null) {
            query.setParameter("startDate", condition.startDate());
        }
        if (condition.endDate() != null) {
            query.setParameter("endDate", condition.endDate());
        }
        if (resolvedId != null) {
            query.setParameter("resolvedId", resolvedId);
        }

        query.setMaxResults(condition.size() + 1);

        return query.getResultList();
    }

    @Override
    public long count(IndexDataSearchCondition condition) {
        StringBuilder jpqlBuilder = new StringBuilder("SELECT count(d) FROM IndexData d WHERE 1=1");

        if (condition.indexInfoId() != null) {
            jpqlBuilder.append(" AND d.indexInfo.id = :indexInfoId");
        }
        if (condition.startDate() != null) {
            jpqlBuilder.append(" AND d.baseDate >= :startDate");
        }
        if (condition.endDate() != null) {
            jpqlBuilder.append(" AND d.baseDate <= :endDate");
        }

        TypedQuery<Long> query = em.createQuery(jpqlBuilder.toString(), Long.class);

        if (condition.indexInfoId() != null) {
            query.setParameter("indexInfoId", condition.indexInfoId());
        }
        if (condition.startDate() != null) {
            query.setParameter("startDate", condition.startDate());
        }
        if (condition.endDate() != null) {
            query.setParameter("endDate", condition.endDate());
        }

        return query.getSingleResult();
    }
}
