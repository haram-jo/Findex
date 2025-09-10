package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.AutoSyncConfigDto;
import com.codeit.findex.entity.AutoSync;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING, // MapStruct가 생성하는 구현체를 Spring Bean으로 등록
        unmappedTargetPolicy = ReportingPolicy.IGNORE // 매핑되지 않은 필드가 있어도 에러 대신 무시
)
public interface AutoSyncMapper {

    // AutoSync 엔티티 → AutoSyncConfigDto 변환
    @Mapping(target = "indexInfoId",         source = "indexInfoId.id") // 엔티티의 연관 객체 IndexInfo.id → DTO의 indexInfoId
    @Mapping(target = "indexName",           source = "indexInfoId.indexName") // IndexInfo.indexName → DTO의 indexName
    @Mapping(target = "indexClassification", source = "indexInfoId.indexClassification") // IndexInfo.indexClassification → DTO의 indexClassification
    @Mapping(target = "enabled", expression = "java(Boolean.TRUE.equals(entity.getEnabled()))") // entity.getEnabled() 값이 null일 수 있으므로 Boolean.TRUE.equals 로 안전하게 true/false 변환 → DTO의 enabled
    AutoSyncConfigDto toDto(AutoSync entity);

    List<AutoSyncConfigDto> toDtoList(List<AutoSync> entities);

}
