package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.request.IndexInfoSearchRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.QIndexInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/* 필터,정렬,커서 구현체
 * 원하는 조건 생성 및 한번에 받아오기
 * JPA 쿼리문으로 작성
 */

@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

  @PersistenceContext
  private EntityManager em; //DB 쿼리 직접 날릴 수 있는 객체
  private final JPAQueryFactory queryFactory;

  private final JdbcTemplate jdbcTemplate;


  //조건에 따른 지수 목록 조회 메서드 (필터 + 정렬 + 페이지네이션)
  @Override
  public List<IndexInfo> findAllWithFilters(IndexInfoSearchRequest param) {

    QIndexInfo indexInfo = QIndexInfo.indexInfo;
    BooleanBuilder where = new BooleanBuilder(); // where 조건 빌드

    if(param.indexClassification() != null) where.and(indexInfo.indexClassification.eq(param.indexClassification()));
    if (param.indexName() != null) where.and(indexInfo.indexName.eq(param.indexName()));
    if (param.favorite() != null) where.and(indexInfo.favorite.eq(param.favorite()));
    if (param.idAfter() != null)  where.and(indexInfo.id.gt(param.idAfter()));

    Order order = "desc".equalsIgnoreCase(param.sortDirection()) ? Order.DESC : Order.ASC;
    OrderSpecifier<?> orderSpecifier;

    switch (param.sortDirection()) {
      case "indexName" -> orderSpecifier = new OrderSpecifier<>(order, indexInfo.indexName);
      case "indexClassification" -> orderSpecifier = new OrderSpecifier<>(order, indexInfo.indexClassification);
      default -> orderSpecifier = new OrderSpecifier<>(Order.ASC, indexInfo.id); // fallback
    }

    return queryFactory.selectFrom(indexInfo)
            .where(where)
            .orderBy(orderSpecifier)
            .limit(param.size() != null ? param.size() : 10)
            .fetch();
  }

  // 조건에 따른 전체 개수 응답용 메서드
  @Override
  public Long countWithFilters(IndexInfoSearchRequest param) {

    QIndexInfo indexInfo = QIndexInfo.indexInfo;
    BooleanBuilder where = new BooleanBuilder(); // where 조건 빌드

    if (param.indexClassification() != null) where.and(indexInfo.indexClassification.eq(param.indexClassification()));
    if (param.indexName() != null) where.and(indexInfo.indexName.eq(param.indexName()));
    if (param.favorite() != null) where.and(indexInfo.favorite.eq(param.favorite()));

    return Optional.ofNullable(queryFactory
            .select(indexInfo.id.countDistinct())
            .from(indexInfo)
            .where(where)
            .fetchOne()).orElse(0L);
  }

  @Override
  public void saveAllInBatch(List<IndexInfo> indexInfos) {
    StringBuilder query = new StringBuilder();

    query.append("INSERT INTO index_infos ")
            .append("(index_classification, index_name, employed_items_count, base_point_in_time, base_index, source_type) ")
            .append("VALUES ");

    for (int i = 0; i < indexInfos.size(); i++) {
      IndexInfo info = indexInfos.get(i);

      query.append("(")
              .append("'").append(info.getIndexClassification()).append("', ")
              .append("'").append(info.getIndexName()).append("', ")
              .append(info.getEmployedItemsCount()).append(", ")
              .append("'").append(info.getBasePointInTime()).append("', ")
              .append(info.getBaseIndex()).append(", ")
              .append("'").append(info.getSourceType().name()).append("'")
              .append(")");

      if (i < indexInfos.size() - 1) {
        query.append(", ");
      }
    }

    em.createNativeQuery(query.toString()).executeUpdate();
  }

}
  
  
  

  




