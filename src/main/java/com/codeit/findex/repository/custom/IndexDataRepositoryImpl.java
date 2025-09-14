package com.codeit.findex.repository.custom;

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

    // JPQL 쿼리를 생성하고 실행하기 위한 EntityManager 의존성 주입
    private final EntityManager em;

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
        // JPQL 쿼리를 동적으로 구성하기 위해 StringBuilder를 사용
        // "WHERE 1=1"은 항상 참인 조건으로, 이후에 AND 조건들을 붙이기 용이하게 만드는 방법
        StringBuilder jpqlBuilder = new StringBuilder("SELECT d FROM IndexData d WHERE 1=1");

        // --- 1. 동적 WHERE 조건 생성 ---
        // 지수 ID 조건이 있으면 쿼리에 추가
        if (condition.indexInfoId() != null) {
            jpqlBuilder.append(" AND d.indexInfo.id = :indexInfoId");
        }
        // 시작 날짜 조건이 있으면 쿼리에 추가
        if (condition.startDate() != null) {
            jpqlBuilder.append(" AND d.baseDate >= :startDate");
        }
        // 종료 날짜 조건이 있으면 쿼리에 추가
        if (condition.endDate() != null) {
            jpqlBuilder.append(" AND d.baseDate <= :endDate");
        }

        // --- 2. 커서 기반 페이지네이션 조건 생성 ---
        // 해석된 커서 ID(이전 페이지의 마지막 항목 ID)를 가져온다.
        Long resolvedId = condition.getResolvedId();
        if (resolvedId != null) {
            // 정렬 방향에 따라 커서의 조회 조건을 다르게 설정
            if (condition.sortDirection().equalsIgnoreCase("DESC")) {
                // 내림차순일 경우, 마지막 ID보다 작은 항목들을 조회
                jpqlBuilder.append(" AND d.id < :resolvedId");
            } else {
                // 오름차순일 경우, 마지막 ID보다 큰 항목들을 조회
                jpqlBuilder.append(" AND d.id > :resolvedId");
            }
        }

        // --- 3. 동적 ORDER BY 조건 생성 ---
        String sortBy = condition.sortField();
        // 정렬 필드가 허용된 리스트에 있는지 확인하여 JPQL 인젝션을 방지
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("허용되지 않은 정렬 필드입니다: " + sortBy);
        }
        // 정렬 방향을 결정합니다. "DESC"가 아니면 모두 "ASC"로 처리
        String sortDirection = condition.sortDirection().equalsIgnoreCase("DESC") ? "DESC" : "ASC";
        // 1차 정렬 조건을 쿼리에 추가
        jpqlBuilder.append(" ORDER BY d.").append(sortBy).append(" ").append(sortDirection);

        // 커서 기반 페이지네이션의 안정성을 위해 고유값인 id를 2차 정렬 조건으로 추가
        // 1차 정렬 필드의 값이 중복되더라도 일관된 순서를 보장
        if (!sortBy.equals("id")) {
            jpqlBuilder.append(", d.id ").append(sortDirection);
        }

        // --- 4. 쿼리 생성 및 파라미터 바인딩 ---
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

        // --- 5. 결과 개수 제한 (페이지네이션) ---
        // 다음 페이지가 있는지 확인하기 위해 요청된 사이즈(size)보다 1개 더 많이 조회
        query.setMaxResults(condition.size() + 1);

        return query.getResultList();
    }

    @Override
    public long count(IndexDataSearchCondition condition) {
        // 전체 개수를 세는 쿼리이므로 "SELECT count(d)"로 시작
        StringBuilder jpqlBuilder = new StringBuilder("SELECT count(d) FROM IndexData d WHERE 1=1");

        // search 메소드와 동일하게 필터링 조건을 추가. (정렬, 페이지네이션 조건은 제외)
        if (condition.indexInfoId() != null) {
            jpqlBuilder.append(" AND d.indexInfo.id = :indexInfoId");
        }
        if (condition.startDate() != null) {
            jpqlBuilder.append(" AND d.baseDate >= :startDate");
        }
        if (condition.endDate() != null) {
            jpqlBuilder.append(" AND d.baseDate <= :endDate");
        }

        // Long 타입의 결과를 반환하는 쿼리를 생성
        TypedQuery<Long> query = em.createQuery(jpqlBuilder.toString(), Long.class);

        // 파라미터를 바인딩
        if (condition.indexInfoId() != null) {
            query.setParameter("indexInfoId", condition.indexInfoId());
        }
        if (condition.startDate() != null) {
            query.setParameter("startDate", condition.startDate());
        }
        if (condition.endDate() != null) {
            query.setParameter("endDate", condition.endDate());
        }

        // 단일 결과(전체 개수)를 반환
        return query.getSingleResult();
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
