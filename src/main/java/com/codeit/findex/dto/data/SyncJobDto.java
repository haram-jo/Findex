package com.codeit.findex.dto.data;

public record SyncJobDto(
        Integer id,
        String jobType,
        Integer indexInfoId,
        String worker,
        String jobTime,
        String result
) {}
