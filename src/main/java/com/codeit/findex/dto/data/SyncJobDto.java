package com.codeit.findex.dto.data;

import com.codeit.findex.entity.JobType;
import com.codeit.findex.service.basic.BasicSyncJobService;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

@Builder
public record SyncJobDto(
        Long id,
        JobType jobType,
        Long indexInfoId,
        LocalDate targetDate,
        String worker,
        Instant jobTime,
        BasicSyncJobService.ResultType result
) {}
