package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.entity.SyncJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {
    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    @Mapping(target = "result", expression = "java(syncJob.isResult() ? \"SUCCESS\" : \"FAIL\")")
    SyncJobDto toDto(SyncJob syncJob);
}
