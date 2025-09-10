package com.codeit.findex.service;


import com.codeit.findex.dto.data.CursorPageResponseIndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.dto.request.IndexInfoUpdateRequest;

public interface IndexInfoService {

  //지수 등록
  IndexInfoDto createIndexInfo(IndexInfoCreateRequest request);

  //지수 수정
  IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest request);

  //지수 삭제
  void deleteIndexInfo(Long id);

  //지수 단건 조회
  IndexInfoDto getIndexInfo(Long id);

  //지수 목록 조회
  CursorPageResponseIndexInfoDto getIndexInfoList(
      String indexClassification,
      String indexName,
      Boolean favorite,
      Long idAfter,
      String cursor,
      String sortField,
      String sortDirection,
      Integer size
  );
}
