package com.codeit.findex.dto.request;

import com.codeit.findex.entity.JobType;

/**
 * 연동 작업 검색 조건 DTO (쿼리 파라미터 바인딩용)
 * - 기본값 처리 포함
 */
public record SyncJobSearchRequest(
        JobType jobType,        // 연동 작업 유형
        Long indexInfoId,      // 지수 정보 아이디
        String baseDateFrom,   // 대상 날짜(부터)
        String baseDateTo,     // 대상 날짜(까지)
        String worker,         // 작업자
        String jobTimeFrom,    // 작업 날짜(부터)
        String jobTimeTo,      // 작업 날짜(까지)
        String status,         // 작업 상태
        Long idAfter,          // 이전 페이지 마지막 요소 ID
        String cursor,         // 커서(다음 페이지 시작점)
        String sortField,      // 정렬 필드 (기본값: jobTime)
        String sortDirection,  // 정렬 방향 (기본값: desc)
        Integer size           // 페이지 크기 (기본값: 10)
) {
    public SyncJobSearchRequest {
        // 정렬 필드가 null 또는 공백이면 기본값 "jobTime"
        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "jobTime";
        }

        // 정렬 방향이 null 또는 공백이면 기본값 "desc"
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc";
        }

        // 페이지 크기가 null이면 기본값 10
        if (size == null) {
            size = 10;
        }
    }
}

