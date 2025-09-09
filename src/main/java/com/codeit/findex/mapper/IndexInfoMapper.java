package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;

/* IndexInfo Entity <-> DTO 변환 담당
 * @MapStruct로 수정해야함
*/

public class IndexInfoMapper {

  // CreateRequest → Entity
  public static IndexInfo toEntity(IndexInfoCreateRequest request) {
    return IndexInfo.builder()
        .indexClassification(request.indexClassification())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .favorite(request.favorite())
        .sourceType(SourceType.USER) // User가 직접 지수 등록하는 걸로 고정
        .build();
  }

  // Entity → Dto
  public static IndexInfoDto toDto(IndexInfo entity) {
    return new IndexInfoDto(
        entity.getId(),
        entity.getIndexClassification(),
        entity.getIndexName(),
        entity.getEmployedItemsCount(),
        entity.getBasePointInTime(),
        entity.getBaseIndex(),
        entity.getSourceType(),
        entity.getFavorite()
    );
  }
}

