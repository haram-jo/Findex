package com.codeit.findex.dto.data;

import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.ResultType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record SyncJobDto(
        Long id,
        JobType jobType,
        Long indexInfoId,
        LocalDate targetDate,
        String worker,
        LocalDateTime jobTime,
        ResultType result
) {}
