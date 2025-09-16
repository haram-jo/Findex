package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.QIndexData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

    // JPQL 쿼리를 생성하고 실행하기 위한 EntityManager 의존성 주입
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    /**
     * JPQL 인젝션 공격을 방지하기 위한 정렬 가능 필드 화이트리스트
     * 클라이언트로부터 받은 정렬 필드명이 이 리스트에 포함된 경우에만 쿼리에 사용
     */
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
        "baseDate", "marketPrice", "closingPrice", "highPrice", "lowPrice",
        "versus", "fluctuationRate", "tradingQuantity", "tradingPrice", "marketTotalAmount"
    );

    @Override
    public List<IndexData> search(IndexDataSearchCondition condition) {

        QIndexData indexData = QIndexData.indexData;
        BooleanBuilder where = new BooleanBuilder(); // where 조건 빌드

        if (condition.indexInfoId() != null) {
            where.and(indexData.indexInfo.id.eq(condition.indexInfoId()));
        }
        // 시작 날짜 조건이 있으면 쿼리에 추가
        if (condition.startDate() != null) {
            where.and(indexData.baseDate.goe(condition.startDate()));
        }
        // 종료 날짜 조건이 있으면 쿼리에 추가
        if (condition.endDate() != null) {
            where.and(indexData.baseDate.loe(condition.endDate()));
        }
        if (condition.idAfter() != null) where.and(indexData.id.gt(condition.idAfter()));

        Order order = "desc".equalsIgnoreCase(condition.sortDirection()) ? Order.DESC : Order.ASC;
        OrderSpecifier<?> orderSpecifier;

        switch (condition.sortDirection()) {
            case "baseDate" -> orderSpecifier = new OrderSpecifier<>(order, indexData.baseDate );
            case "marketPrice" -> orderSpecifier = new OrderSpecifier<>(order, indexData.marketPrice );
            case "closingPrice" -> orderSpecifier = new OrderSpecifier<>(order, indexData.closingPrice );
            case "highPrice" -> orderSpecifier = new OrderSpecifier<>(order, indexData.highPrice );
            case "lowPrice" -> orderSpecifier = new OrderSpecifier<>(order, indexData.lowPrice );
            case "versus" -> orderSpecifier = new OrderSpecifier<>(order, indexData.versus);
            case "fluctuationRate" -> orderSpecifier = new OrderSpecifier<>(order, indexData.fluctuationRate);
            case "tradingQuantity" -> orderSpecifier = new OrderSpecifier<>(order, indexData.tradingQuantity);
            case "tradingPrice" -> orderSpecifier = new OrderSpecifier<>(order, indexData.tradingPrice);
            case "marketTotalAmount" -> orderSpecifier = new OrderSpecifier<>(order, indexData.marketTotalAmount);
            default -> orderSpecifier = new OrderSpecifier<>(Order.DESC, indexData.baseDate); // fallback
        }

        return queryFactory.selectFrom(indexData)
                .where(where)
                .orderBy(orderSpecifier)
                .limit(condition.size() != null ? condition.size() : 0)
                .fetch();
    }

    @Override
    public long count(IndexDataSearchCondition condition) {

        QIndexData indexData = QIndexData.indexData;
        BooleanBuilder where = new BooleanBuilder(); // where 조건 빌드

        if (condition.indexInfoId() != null) {
            where.and(indexData.indexInfo.id.eq(condition.indexInfoId()));
        }
        // 시작 날짜 조건이 있으면 쿼리에 추가
        if (condition.startDate() != null) {
            where.and(indexData.baseDate.goe(condition.startDate()));
        }
        // 종료 날짜 조건이 있으면 쿼리에 추가
        if (condition.endDate() != null) {
            where.and(indexData.baseDate.loe(condition.endDate()));
        }

        return Optional.ofNullable(queryFactory
                .select(indexData.id.countDistinct())
                .from(indexData)
                .where(where)
                .fetchOne()).orElse(0L);
    }

    // CSV Export를 위해 페이지네이션 없이 모든 데이터를 필터링하고 정렬하여 조회하는 findAllByCondition 메소드
    @Override
    public List<IndexData> findAllByCondition(IndexDataSearchCondition condition) {
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

        String sortBy = condition.sortField();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("허용되지 않은 정렬 필드입니다: " + sortBy);
        }
        String sortDirection = condition.sortDirection().equalsIgnoreCase("DESC") ? "DESC" : "ASC";
        jpqlBuilder.append(" ORDER BY d.").append(sortBy).append(" ").append(sortDirection);

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

        return query.getResultList();
    }

    @Override
    public void saveAllInBatch(List<IndexData> indexDataList, Long indexInfoId) {
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO index_data ")
            .append("(index_info_id, base_date, source_type, market_price, closing_price, high_price, " +
                    "low_price, versus, fluctuation_rate, trading_quantity, trading_price, market_total_amount) ")
            .append("VALUES ");

        for (int i = 0; i < indexDataList.size(); i++) {
            IndexData data = indexDataList.get(i);

            query.append("(")
                .append("'").append(indexInfoId).append("', ")
                .append("'").append(data.getBaseDate()).append("', ")
                .append("'").append(data.getSourceType()).append("', ")
                .append(data.getMarketPrice()).append(", ")
                .append(data.getClosingPrice()).append(", ")
                .append(data.getHighPrice()).append(", ")
                .append(data.getLowPrice()).append(", ")
                .append(data.getVersus()).append(", ")
                .append(data.getFluctuationRate()).append(", ")
                .append(data.getTradingQuantity()).append(", ")
                .append(data.getTradingPrice()).append(", ")
                .append(data.getMarketTotalAmount())
                .append(")");

            if (i < indexDataList.size() - 1) {
                query.append(", ");
            }
        }

        em.createNativeQuery(query.toString()).executeUpdate();
    }
}
