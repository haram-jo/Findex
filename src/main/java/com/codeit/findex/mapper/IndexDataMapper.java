package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IndexDataMapper {

  @Mapping(target = "sourceType", source = "sourceType")
  IndexData toEntity(IndexDataCreateRequest request, IndexInfo indexInfo, String sourceType);

  IndexData toEntity(IndexDataCreateRequest request);

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  IndexDataDto toDto(IndexData entity);

  List<IndexDataDto> toDtoList(List<IndexData> entities);
}