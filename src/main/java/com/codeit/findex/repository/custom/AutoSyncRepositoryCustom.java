package com.codeit.findex.repository.custom;

import com.codeit.findex.entity.AutoSync;
import org.springframework.data.domain.Slice;

public interface AutoSyncRepositoryCustom { // 확장용 커스텀 리포지토리 인터페이스
    Slice<AutoSync> findSlice( // 커서 기반 keyset + Slice 조합 조회 메서드
                               Long indexInfoId,   // 지수 필터 (nullable)
                               Boolean enabled,    // 활성화 필터 (nullable)
                               String sortField,   // "indexInfo.indexName" | "enabled"
                               boolean asc,        // 오름차순 여부
                               Object cursorValue, // 마지막 정렬값 (String or Boolean) – null이면 첫 페이지
                               Long idAfter,       // 마지막 ID – null이면 첫 페이지
                               int size            // 페이지 크기 (size+1 로 조회하여 hasNext 판정)
    );

    long countByFilters( // 동일 필터 조건으로 총 개수 조회 (Slice는 count가 없어서 별도 제공)
                         Long indexInfoId,   // 지수 필터
                         Boolean enabled     // 활성화 필터
    );
}
