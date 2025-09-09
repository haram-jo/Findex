package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IndexDataMapper {

    IndexData toEntity(IndexDataCreateRequest request);

    IndexDataDto toDto(IndexData entity);

    List<IndexDataDto> toDtoList(List<IndexData> entities);
}