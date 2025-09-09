package com.codeit.findex.dto.data;

import java.util.List;

public record CursorPageResponseIndexDataDto(
        List<IndexDataDto> content,
        Long nextCursor,
        String nextIdAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}