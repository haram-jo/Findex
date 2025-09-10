package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.AutoSyncConfigDto;
import com.codeit.findex.entity.AutoSync;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AutoSyncMapper {

    @Mapping(target = "indexInfoId",         source = "indexInfoId.id")
    @Mapping(target = "indexName",           source = "indexInfoId.indexName")
    @Mapping(target = "indexClassification", source = "indexInfoId.indexClassification")
    @Mapping(target = "enabled", expression = "java(Boolean.TRUE.equals(entity.getEnabled()))")
    AutoSyncConfigDto toDto(AutoSync entity);

    List<AutoSyncConfigDto> toDtoList(List<AutoSync> entities);
}
