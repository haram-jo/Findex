package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;

/* IndexInfo Entity <-> DTO 변환 담당
*/


public class IndexInfoMapper {

  // CreateRequest → Entity
  public static IndexInfo toEntity(IndexInfoCreateRequest request) {
    return IndexInfo.builder()
        .indexClassification(request.getIndexClassification())
        .indexName(request.getIndexName())
        .employedItemsCount(request.getEmployedItemsCount())
        .basePointInTime(request.getBasePointInTime())
        .baseIndex(request.getBaseIndex())
        .favorite(request.getFavorite())
        .sourceType(SourceType.USER) // User로 일단 고정, Open API 받아오고 연동 후 수정 예정

        .build();
  }

  // Entity → Dto
  public static IndexInfoDto toDto(IndexInfo entity) {
    return IndexInfoDto.builder()
        .id(entity.getId())
        .indexClassification(entity.getIndexClassification())
        .indexName(entity.getIndexName())
        .employedItemsCount(entity.getEmployedItemsCount())
        .basePointInTime(entity.getBasePointInTime())
        .baseIndex(entity.getBaseIndex())
        .sourceType(entity.getSourceType())
        .favorite(entity.getFavorite())
        .build();
  }
}

