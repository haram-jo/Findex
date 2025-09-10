package com.codeit.findex.mapper;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.entity.SyncJob;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {

    SyncJobDto toDto(SyncJob syncJob);
}
