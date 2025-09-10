package com.codeit.findex.dto.data;

import com.codeit.findex.entity.JobType;
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
        String result
) {}
