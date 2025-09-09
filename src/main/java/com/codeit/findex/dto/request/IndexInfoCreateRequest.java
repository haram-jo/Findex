package com.codeit.findex.dto.request;

/* 지수 등록
 * 요청 DTO
 */

public record IndexInfoCreateRequest(
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    String basePointInTime, // YYYY-MM-DD
    Double baseIndex,
    Boolean favorite
) {}


