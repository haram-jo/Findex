package com.codeit.findex.dto.data;

// 자동 연동 설정 한 건을 나타내는 레코드 DTO
public record AutoSyncConfigDto(
        Long id,                   // 자동 연동 설정 ID
        Long indexInfoId,          // 지수 정보 ID
        String indexClassification,// 지수 분류명 (예: "KOSPI시리즈")
        String indexName,          // 지수명 (예: "IT 서비스")
        boolean enabled            // 활성화 여부
) {}

