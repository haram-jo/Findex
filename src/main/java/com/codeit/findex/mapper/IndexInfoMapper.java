package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.entity.IndexInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/* IndexInfo Entity <-> DTO 변환 담당
*/


@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  // CreateRequest → Entity
  @Mapping(target = "sourceType", expression = "java(com.codeit.findex.entity.SourceType.USER)") // User 고정
  IndexInfo toEntity(IndexInfoCreateRequest request);

  // Entity → Dto
  IndexInfoDto toDto(IndexInfo entity);
}

