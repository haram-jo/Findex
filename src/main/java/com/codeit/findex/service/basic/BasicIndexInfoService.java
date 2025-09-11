package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseIndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoSummaryDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.dto.request.IndexInfoUpdateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.IndexInfoMapper;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexInfoService;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 2. 값 수정
    if (request.employedItemsCount() != null)
      indexInfo.setEmployedItemsCount(request.employedItemsCount());
    if (request.basePointInTime() != null)
      indexInfo.setBasePointInTime(LocalDate.parse(request.basePointInTime(), formatter));
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


  //단건 조회
  @Override
  public IndexInfoDto getIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("지수 정보를 찾을 수 없음"));
    return indexInfoMapper.toDto(indexInfo);
  }

  //여러 목록 조회
  @Override
  public CursorPageResponseIndexInfoDto getIndexInfoList(
      String indexClassification,
      String indexName,
      Boolean favorite,
      Long idAfter,
      String cursor,
      String sortField,
      String sortDirection,
      Integer size
  ) {

    //1. 조건에 맞는 데이터만 꺼내기
    List<IndexInfo> entities = indexInfoRepository.findAllWithFilters(
        indexClassification, indexName, favorite,
        idAfter, cursor, sortField, sortDirection, size
    );

    //2. 엔티티 -> DTO로 변환
    List<IndexInfoDto> dtoList = entities.stream()
        .map(indexInfoMapper::toDto)
        .toList();

    //3. 전체 개수도 구하여 응답 (프론트: totalElements)
    Long totalElements = indexInfoRepository.countWithFilters(
        indexClassification, indexName, favorite
    );

    /* 커서 기반 페이징 처리에 필요한 계산
     * 지금 페이지의 마지막 id를 nextCursor(커서)로 넘기고,
     * 페이지가 끝났는지(hasNext)도 같이 알려줌
     */
    Long nextIdAfter = dtoList.isEmpty() //지금 page의 마지막 id
        ? null // 비었으면 null
        : dtoList.get(dtoList.size() - 1).id(); // 마지막 id 값을 cursor로 사용

    String nextCursor = nextIdAfter == null
        ? null //비었으면 null
        : String.valueOf(nextIdAfter); //아니면 String으로 변환해서 저장

    boolean hasNext = dtoList.size() == size; // 다음 page가 있는지 없는지의 여부

    //4. 프론트에서 원하는 응답으로 반환
    return new CursorPageResponseIndexInfoDto(
        dtoList,        // 내용
        nextCursor,     // 다음 페이지 커서
        nextIdAfter,    // 마지막 요소의 ID
        size,           // 페이지 크기
        totalElements,  // 전체 개수
        hasNext         // 다음 페이지 여부
    );
   }

   //요약 목록 조회
  @Override
  public List<IndexInfoSummaryDto> getIndexInfoSummaries() {
    return indexInfoRepository.findAllSummaries();
    }
  }

