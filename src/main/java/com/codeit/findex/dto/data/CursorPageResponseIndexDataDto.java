package com.codeit.findex.dto.data;

import java.util.List;

public record CursorPageResponseIndexDataDto(
        List<IndexDataDto> content,
        String nextCursor,
        String nextIdAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}