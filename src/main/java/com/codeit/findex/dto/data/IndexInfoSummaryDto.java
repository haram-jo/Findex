package com.codeit.findex.dto.data;

/* 지수 요약목록 조회
 * 응답 DTO
 * 지수 id, 분류명, 지수명
 */

public record IndexInfoSummaryDto(
    Long id,
    String indexClassification,
    String indexName
) {}
