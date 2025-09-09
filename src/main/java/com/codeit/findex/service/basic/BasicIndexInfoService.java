package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.IndexInfoMapper;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicIndexInfoService implements IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final IndexInfoMapper indexInfoMapper;

  //생성
  @Override
  public IndexInfoDto createIndexInfo(IndexInfoCreateRequest request) {
    IndexInfo entity = indexInfoMapper.toEntity(request); //DTO ->Entity
    IndexInfo saved = indexInfoRepository.save(entity); //DB 저장
    return indexInfoMapper.toDto(saved);
  }
}
