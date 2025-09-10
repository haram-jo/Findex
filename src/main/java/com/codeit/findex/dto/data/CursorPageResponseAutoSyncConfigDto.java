package com.codeit.findex.dto.data;

import java.util.List;

// 커서 기반 페이지 응답 DTO
public record CursorPageResponseAutoSyncConfigDto(
        List<AutoSyncConfigDto> content, // 페이지 내용 (자동 연동 설정 아이템 리스트)
        String nextCursor,               // 다음 페이지 커서
        Long nextIdAfter,              // 마지막 요소의 ID
        Integer size,                    // 페이지 크기
        Long totalElements,              // 총 요소 수
        boolean hasNext                  // 다음 페이지 존재 여부
) {}

