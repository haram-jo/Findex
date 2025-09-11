package com.codeit.findex.repository.custom;
import com.codeit.findex.entity.IndexInfo;
import java.util.List;

 /* IndexInfo 필터,정렬,커서 레포지토리
  *
  */

public interface IndexInfoRepositoryCustom {

  //조건별 목록 반환 메서드
  List<IndexInfo> findAllWithFilters(
      String indexClassification,
      String indexName,
      Boolean favorite,
      Long idAfter,
      String cursor,
      String sortField,
      String sortDirection,
      Integer size
  );

 //조건별 갯수 응답용 메서드
  Long countWithFilters(
      String indexClassification,
      String indexName,
      Boolean favorite
  );
}

