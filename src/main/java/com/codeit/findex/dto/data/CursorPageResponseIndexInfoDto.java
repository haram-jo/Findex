package com.codeit.findex.dto.data;

import lombok.Builder;

import java.util.List;

@Builder
public record CursorPageResponseIndexInfoDto(
    List<IndexInfoDto> content, // 페이지 내용
    String nextCursor,          // 다음 페이지 커서
    Long nextIdAfter,           // 마지막 요소의 ID
    Integer size,               // 페이지 크기
    Long totalElements,         // 총 요소 수
    Boolean hasNext             // 다음 페이지 여부
) {}
