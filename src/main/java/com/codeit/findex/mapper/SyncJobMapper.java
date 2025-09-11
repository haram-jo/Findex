package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.entity.ResultType;
import com.codeit.findex.entity.SyncJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {
    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    @Mapping(source = "result", target = "result")
    SyncJobDto toDto(SyncJob syncJob);

    default ResultType map(Boolean result) {
        return ResultType.fromBoolean(result);
    }

    default Boolean map(ResultType resultType) {
        return (resultType != null) ? resultType.toBoolean() : null;
    }
}
