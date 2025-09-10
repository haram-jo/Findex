package com.codeit.findex.dto.data;

import java.util.List;

public record CursorPageResponseIndexDataDto(
        List<IndexDataDto> content,
        String nextCursor,
        Long nextIdAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
}