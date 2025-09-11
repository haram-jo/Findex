package com.codeit.findex.repository.custom;

import com.codeit.findex.entity.IndexInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import org.springframework.stereotype.Repository;


/* 필터,정렬,커서 구현체
 * 원하는 조건 생성 및 한번에 받아오기
 * JPA 쿼리문으로 작성
 */

@Repository
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

  @PersistenceContext
  private EntityManager em; //DB 쿼리 직접 날릴 수 있는 객체

  //조건에 따른 지수 목록 조회 메서드 (필터 + 정렬 + 페이지네이션)
  @Override
  public List<IndexInfo> findAllWithFilters(String indexClassification, String indexName,
      Boolean favorite, Long idAfter, String cursor, String sortField, String sortDirection,
      Integer size) {


    // 1. JPA 쿼리문  (i = *과 같음, WHERE 1=1: 초기값 세팅)
    StringBuilder jpql = new StringBuilder("SELECT i FROM IndexInfo i WHERE 1=1 ");

    // 2. 조건
    if (indexClassification != null) { //값이 null이 아니면
      jpql.append("AND i.indexClassification = :indexClassification ");
    } // 쿼리에 그 값 추가
    if (indexName != null) { //값이 null이 아니면
      jpql.append("AND i.indexName LIKE CONCAT('%', :indexName, '%') ");
    }// 쿼리에 그 값 추가
    if (favorite != null) { //값이 null이 아니면
      jpql.append("AND i.favorite = :favorite ");
    } //쿼리에 그 값 추가
    if (idAfter != null) { //값이 null이 아니면
      jpql.append("AND i.id > :idAfter ");
    }
    //지수분류명과 지수명 오름차순,내림차순 정렬
    if (sortField != null && sortDirection != null) {
      jpql.append("ORDER BY i.").append(sortField).append(" ").append(sortDirection).append(" ");
    }


    // 3. 쿼리 객체 생성 (JPQL로 작성, 결과는 List<IndexInfo>로 응답)
    Query query = em.createQuery(jpql.toString(), IndexInfo.class);

    // 4. 쿼리에 요청받은 값 넣기
    if (indexClassification != null) {
      query.setParameter("indexClassification", indexClassification);
    }
    if (indexName != null) {
      query.setParameter("indexName", indexName);
    }
    if (favorite != null) {
      query.setParameter("favorite", favorite);
    }
    if (idAfter != null) {
      query.setParameter("idAfter", idAfter);
    }

    // 5. 한 페이지에 몇 개만 보여줄지 (기본값 :10)
    query.setMaxResults(size != null ? size : 10);

    // 7. 결과 반환 (목록)
    return query.getResultList(); //List<IndexInfo>로 응답
  }

  // 조건에 따른 전체 개수 응답용 메서드
  @Override
  public Long countWithFilters(
      String indexClassification, //종목
      String indexName, //지수 이름
      Boolean favorite //즐겨찾기 여부
  ) {
    // 1.JPQL 쿼리 조립
    StringBuilder jpql = new StringBuilder("SELECT COUNT(i) FROM IndexInfo i WHERE 1=1 ");

    if (indexClassification != null) {
      jpql.append("AND i.indexClassification = :indexClassification ");
    }
    if (indexName != null) {
      jpql.append("AND i.indexName LIKE CONCAT('%', :indexName, '%') ");
    }
    if (favorite != null) {
      jpql.append("AND i.favorite = :favorite ");
    }

    // 2. 쿼리 객체 생성
    Query query = em.createQuery(jpql.toString());

    // 3. 쿼리에 요청받은 값 넣기
    if (indexClassification != null) {
      query.setParameter("indexClassification", indexClassification);
    }
    if (indexName != null) {
      query.setParameter("indexName", indexName);
    }
    if (favorite != null) {
      query.setParameter("favorite", favorite);
    }

    // 4. 결과 반환
    return (Long) query.getSingleResult(); //List<IndexInfo>로 응답
  }
}
  
  
  

  




