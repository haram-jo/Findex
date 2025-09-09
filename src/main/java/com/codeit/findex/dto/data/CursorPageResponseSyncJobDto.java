package com.codeit.findex.dto.data;

import java.util.List;

public record CursorPageResponseSyncJobDto(
    List<SyncJobDto> content,
    String nextCursor,
    String nextIdAfter,
    Integer size,
    Integer totalElements,
    boolean hasNext
) {}
