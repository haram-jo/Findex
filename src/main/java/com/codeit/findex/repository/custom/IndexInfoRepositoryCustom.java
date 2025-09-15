package com.codeit.findex.repository.custom;
import com.codeit.findex.dto.request.IndexInfoSearchRequest;
import com.codeit.findex.entity.IndexInfo;
import java.util.List;

 /* IndexInfo 필터,정렬,커서 레포지토리
  *
  */

public interface IndexInfoRepositoryCustom {

  //조건별 목록 반환 메서드
  List<IndexInfo> findAllWithFilters(IndexInfoSearchRequest param);

 //조건별 갯수 응답용 메서드
  Long countWithFilters(IndexInfoSearchRequest param);

  void saveAllInBatch(List<IndexInfo> indexInfos);
}

