package com.codeit.findex.service;


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
}
