package com.codeit.findex.service;


import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;

public interface IndexInfoService {

  //지수 등록
  IndexInfoDto createIndexInfo(IndexInfoCreateRequest request);

}
