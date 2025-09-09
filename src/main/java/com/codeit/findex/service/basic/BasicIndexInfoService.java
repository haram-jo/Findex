package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.dto.request.IndexInfoUpdateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.IndexInfoMapper;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexInfoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/* 지수 정보 구현체
 * 등록,수정,삭제 기능 제공
 */

@Service
@RequiredArgsConstructor
public class BasicIndexInfoService implements IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final IndexInfoMapper indexInfoMapper;

  //등록
  @Override
  public IndexInfoDto createIndexInfo(IndexInfoCreateRequest request) {
    IndexInfo entity = indexInfoMapper.toEntity(request); // 1. 요청 request를 엔티티로 변환
    IndexInfo saved = indexInfoRepository.save(entity); //2. DB 저장
    return indexInfoMapper.toDto(saved); // 3. 엔티티를 응답dto로 변환
  }

  //수정
  @Override
  public IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest request) {

    // 1. 기존 엔티티 조회
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("지수 정보를 찾을 수 없음"));

    // 2. 값 수정
    if (request.employedItemsCount() != null)
      indexInfo.setEmployedItemsCount(request.employedItemsCount());
    if (request.basePointInTime() != null)
      indexInfo.setBasePointInTime(request.basePointInTime());
    if (request.baseIndex() != null)
      indexInfo.setBaseIndex(request.baseIndex());
    if (request.favorite() != null)
      indexInfo.setFavorite(request.favorite());

    // 3. DB에 저장
    IndexInfo updated = indexInfoRepository.save(indexInfo);

    // 4. DTO 변환해서 반환
    return indexInfoMapper.toDto(updated);
   }
   
   //삭제
  @Override
  @Transactional //여러 필드 중 하나라도 없으면 rollback
  public void deleteIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("지수 정보 찾을 수 없음"));
    indexInfoRepository.delete(indexInfo);
  }
}
