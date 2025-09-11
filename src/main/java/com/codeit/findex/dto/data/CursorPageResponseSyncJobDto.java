package com.codeit.findex.dto.data;

import lombok.Builder;

import java.util.List;

@Builder
public record CursorPageResponseSyncJobDto(
    List<SyncJobDto> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {}
