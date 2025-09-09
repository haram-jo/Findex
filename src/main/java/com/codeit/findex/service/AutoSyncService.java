package com.codeit.findex.service;

import com.codeit.findex.dto.data.CursorPageResponseAutoSyncConfigDto;
import org.springframework.lang.Nullable;

public interface AutoSyncService {

    CursorPageResponseAutoSyncConfigDto list(
            @Nullable Long indexInfoId,      // 지수 정보 ID (null=전체)
            @Nullable Boolean enabled,       // 활성화 여부 (null=전체)
            @Nullable Long idAfter,          // 이전 페이지 마지막 요소 ID
            @Nullable String cursor,         // 커서(있으면 idAfter보다 우선)
            @Nullable String sortField,      // "indexInfo.indexName" | "enabled"
            @Nullable String sortDirection,  // "asc" | "desc"
            @Nullable Integer size           // 페이지 크기(기본 10, cap 100)
    );
}
